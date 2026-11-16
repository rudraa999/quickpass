package com.rudra.quickpass.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.rudra.quickpass.config.RazorpayConfig;
import com.rudra.quickpass.dto.BookPassRequest;
import com.rudra.quickpass.dto.CreateOrderResponse;
import com.rudra.quickpass.dto.VerifyPaymentRequest;
import com.rudra.quickpass.entity.*;
import com.rudra.quickpass.repository.BookingRepository;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.PaymentRepository;
import com.rudra.quickpass.repository.TicketTierRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;
    private final RazorpayConfig razorpayConfig;
    private final RazorpayClient razorpayClient;

    public PaymentService(EventRepository eventRepository,
                          TicketTierRepository ticketTierRepository,
                          BookingRepository bookingRepository,
                          PaymentRepository paymentRepository,
                          QrCodeService qrCodeService,
                          RazorpayConfig razorpayConfig,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) RazorpayClient razorpayClient) {
        this.eventRepository = eventRepository;
        this.ticketTierRepository = ticketTierRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.qrCodeService = qrCodeService;
        this.razorpayConfig = razorpayConfig;
        this.razorpayClient = razorpayClient;
    }

    /**
     * Step 1: Initiate Razorpay Order, reserve inventory, and save PENDING booking & payment records.
     */
    @Transactional
    public CreateOrderResponse createPaymentOrder(BookPassRequest req, User attendee) {
        Event event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getStatus() != EventStatus.APPROVED) {
            throw new IllegalArgumentException("This event is not currently open for bookings.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getBookingStartDate())) {
            throw new IllegalArgumentException("Booking has not opened yet for this event.");
        }
        if (now.isAfter(event.getBookingEndDate())) {
            throw new IllegalArgumentException("Booking for this event has already closed.");
        }

        int qty = req.getQuantity();
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1. Resolve Ticket Tier & Acquire Pessimistic Lock
        TicketTier tier = null;
        if (req.getTierId() != null) {
            tier = ticketTierRepository.findByIdForUpdate(req.getTierId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected ticket tier not found"));
            if (!tier.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException("Ticket tier does not belong to this event");
            }
        } else if (req.getPassType() != null) {
            String targetName = switch (req.getPassType()) {
                case NORMAL -> "Normal Pass";
                case VIP -> "VIP Pass";
                case VVIP -> "VVIP Pass";
            };
            tier = ticketTierRepository.findByEventIdAndName(event.getId(), targetName)
                    .orElseGet(() -> {
                        List<TicketTier> tiers = ticketTierRepository.findByEventIdOrderByIdAsc(event.getId());
                        if (!tiers.isEmpty()) return tiers.get(0);
                        throw new IllegalArgumentException("No ticket tier found matching " + req.getPassType());
                    });
            // Re-fetch with lock
            tier = ticketTierRepository.findByIdForUpdate(tier.getId()).orElse(tier);
        } else {
            List<TicketTier> tiers = ticketTierRepository.findByEventIdOrderByIdAsc(event.getId());
            if (!tiers.isEmpty()) {
                tier = ticketTierRepository.findByIdForUpdate(tiers.get(0).getId()).orElse(tiers.get(0));
            } else {
                throw new IllegalArgumentException("No ticket tiers available for this event");
            }
        }

        // 2. Check Capacity & Hold Inventory
        if (tier.getAvailableCapacity() < qty) {
            throw new IllegalArgumentException("Not enough " + tier.getName() + " passes available. Remaining: " + tier.getAvailableCapacity());
        }
        tier.setAvailableCapacity(tier.getAvailableCapacity() - qty);
        ticketTierRepository.save(tier);

        BigDecimal unitPrice = tier.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(qty));
        long amountInPaise = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        // 3. Create Booking Envelope (Status: PENDING_PAYMENT)
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Booking booking = new Booking();
        booking.setOrderNumber(orderNumber);
        booking.setAttendee(attendee);
        booking.setEvent(event);
        booking.setTier(tier);
        booking.setTotalQuantity(qty);
        booking.setTotalAmount(totalAmount);
        booking.setStatus("PENDING_PAYMENT");
        booking = bookingRepository.save(booking);

        // 4. Create Razorpay Gateway Order
        String razorpayOrderId;
        if (razorpayClient != null && razorpayConfig.isConfigured()) {
            try {
                JSONObject orderReq = new JSONObject();
                orderReq.put("amount", amountInPaise);
                orderReq.put("currency", "INR");
                orderReq.put("receipt", orderNumber);

                JSONObject notes = new JSONObject();
                notes.put("bookingId", booking.getId());
                notes.put("eventId", event.getId());
                notes.put("attendeeEmail", attendee.getEmail());
                orderReq.put("notes", notes);

                Order rzpOrder = razorpayClient.orders.create(orderReq);
                razorpayOrderId = rzpOrder.get("id");
                log.info("Razorpay Order created: {} for booking #{}", razorpayOrderId, booking.getId());
            } catch (Exception e) {
                log.error("Failed to create Razorpay Order: {}", e.getMessage(), e);
                // Roll back held capacity
                tier.setAvailableCapacity(tier.getAvailableCapacity() + qty);
                ticketTierRepository.save(tier);
                throw new IllegalStateException("Payment provider error: " + e.getMessage());
            }
        } else {
            // Simulated Test Mode (when real API keys are not yet configured in launch.json)
            razorpayOrderId = "order_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("Simulated Razorpay Order created: {} for booking #{}", razorpayOrderId, booking.getId());
        }

        // 5. Create Payment Record (Status: PENDING)
        Payment payment = new Payment(booking, razorpayOrderId, totalAmount);
        payment = paymentRepository.save(payment);
        booking.addPayment(payment);

        // 6. Return response for frontend Razorpay checkout popup
        CreateOrderResponse res = new CreateOrderResponse();
        res.setBookingId(booking.getId());
        res.setOrderNumber(booking.getOrderNumber());
        res.setRazorpayOrderId(razorpayOrderId);
        res.setAmountInPaise(amountInPaise);
        res.setAmountInRupees(totalAmount);
        res.setCurrency("INR");
        res.setRazorpayKeyId(razorpayConfig.getKeyId());
        res.setEventTitle(event.getTitle());
        res.setTierName(tier.getName());
        res.setQuantity(qty);
        res.setAttendeeName(attendee.getFullName());
        res.setAttendeeEmail(attendee.getEmail());

        return res;
    }

    /**
     * Step 2: Cryptographic Signature Verification & Ticket Issuance (Idempotent).
     */
    @Transactional
    public Booking verifyPayment(VerifyPaymentRequest req, User attendee) {
        // 1. Acquire Pessimistic Lock on Payment Row
        Payment payment = paymentRepository.findByRazorpayOrderIdForUpdate(req.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for order: " + req.getRazorpayOrderId()));

        Booking booking = payment.getBooking();

        // 2. Idempotency Check: if already verified and tickets generated, return immediately!
        if (payment.getStatus() == PaymentStatus.SUCCESS && "CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            log.info("Payment {} already successfully confirmed. Returning existing booking #{}",
                    req.getRazorpayOrderId(), booking.getId());
            return booking;
        }

        // 3. Cryptographic Signature Verification
        String payload = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
        boolean isSignatureValid = false;

        if (razorpayConfig.isConfigured()) {
            isSignatureValid = verifyHmacSha256(payload, req.getRazorpaySignature(), razorpayConfig.getKeySecret());
        } else {
            // In simulated test mode, verify structure or simulated hash
            isSignatureValid = req.getRazorpaySignature() != null && !req.getRazorpaySignature().isBlank();
        }

        if (!isSignatureValid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new SecurityException("Cryptographic payment signature mismatch! Transaction rejected.");
        }

        // 4. Update Payment & Booking Status
        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        booking.setStatus("CONFIRMED");

        // 5. Issue Individual Tickets & Generate QR Codes
        TicketTier tier = booking.getTier();
        Event event = booking.getEvent();
        int qty = booking.getTotalQuantity();
        List<String> attendeeNames = req.getAttendeeNames();

        // Avoid duplicate ticket generation if tickets already exist
        if (booking.getTickets() == null || booking.getTickets().isEmpty()) {
            for (int i = 1; i <= qty; i++) {
                String ticketCode = "QP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                String ticketAttendeeName = attendee.getFullName();
                if (attendeeNames != null && attendeeNames.size() >= i && attendeeNames.get(i - 1) != null && !attendeeNames.get(i - 1).trim().isEmpty()) {
                    ticketAttendeeName = attendeeNames.get(i - 1).trim();
                } else if (qty > 1) {
                    ticketAttendeeName = attendee.getFullName() + (i == 1 ? " (Primary)" : " (Guest " + i + ")");
                }

                String qrPayload = String.format("QUICKPASS|CODE:%s|EVENT:%d|USER:%s|TIER:%s|TICKET:%d/%d",
                        ticketCode, event.getId(), attendee.getEmail(), tier != null ? tier.getName() : "Pass", i, qty);
                String qrCodeBase64 = qrCodeService.generateQrCodeBase64(qrPayload, 260, 260);

                Ticket ticket = new Ticket(booking, tier, event, ticketCode, ticketAttendeeName, qrCodeBase64);
                booking.addTicket(ticket);
            }
        }

        return bookingRepository.save(booking);
    }

    /**
     * Step 3: Handle Payment Cancellation / Modal Dismissal (Restores held inventory).
     */
    @Transactional
    public void cancelPayment(String razorpayOrderId) {
        paymentRepository.findByRazorpayOrderIdForUpdate(razorpayOrderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                Booking booking = payment.getBooking();
                if ("PENDING_PAYMENT".equals(booking.getStatus())) {
                    booking.setStatus("PAYMENT_FAILED");
                    bookingRepository.save(booking);

                    // Restore held inventory back to tier
                    if (booking.getTier() != null) {
                        TicketTier tier = booking.getTier();
                        tier.setAvailableCapacity(tier.getAvailableCapacity() + booking.getTotalQuantity());
                        ticketTierRepository.save(tier);
                        log.info("Restored {} seats to tier '{}' due to cancelled payment for booking #{}",
                                booking.getTotalQuantity(), tier.getName(), booking.getId());
                    }
                }
            }
        });
    }

    /**
     * Computes HMAC-SHA256 signature to verify authenticity against Razorpay secret.
     */
    public static boolean verifyHmacSha256(String data, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Error computing HMAC-SHA256: {}", e.getMessage());
            return false;
        }
    }
}

