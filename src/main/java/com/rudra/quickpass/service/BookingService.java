package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.BookPassRequest;
import com.rudra.quickpass.entity.*;
import com.rudra.quickpass.repository.BookingRepository;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.TicketRepository;
import com.rudra.quickpass.repository.TicketTierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;

    public BookingService(BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          TicketTierRepository ticketTierRepository,
                          TicketRepository ticketRepository,
                          QrCodeService qrCodeService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.ticketTierRepository = ticketTierRepository;
        this.ticketRepository = ticketRepository;
        this.qrCodeService = qrCodeService;
    }

    @Transactional
    public Booking bookPass(BookPassRequest req, User attendee) {
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

        // 1. Resolve Ticket Tier
        TicketTier tier = null;
        if (req.getTierId() != null) {
            tier = ticketTierRepository.findById(req.getTierId())
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
        } else {
            List<TicketTier> tiers = ticketTierRepository.findByEventIdOrderByIdAsc(event.getId());
            if (!tiers.isEmpty()) {
                tier = tiers.get(0);
            } else {
                throw new IllegalArgumentException("No ticket tiers available for this event");
            }
        }

        // 2. Check Capacity & Decrement
        if (tier.getAvailableCapacity() < qty) {
            throw new IllegalArgumentException("Not enough " + tier.getName() + " passes available. Remaining: " + tier.getAvailableCapacity());
        }
        tier.setAvailableCapacity(tier.getAvailableCapacity() - qty);
        ticketTierRepository.save(tier);

        BigDecimal unitPrice = tier.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(qty));

        // 3. Create Booking (Order Container)
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Booking booking = new Booking();
        booking.setOrderNumber(orderNumber);
        booking.setAttendee(attendee);
        booking.setEvent(event);
        booking.setTotalQuantity(qty);
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");

        // 4. Generate N distinct Individual Ticket passes
        List<String> attendeeNames = req.getAttendeeNames();

        for (int i = 1; i <= qty; i++) {
            String ticketCode = "QP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            String ticketAttendeeName = attendee.getFullName();
            if (attendeeNames != null && attendeeNames.size() >= i && attendeeNames.get(i - 1) != null && !attendeeNames.get(i - 1).trim().isEmpty()) {
                ticketAttendeeName = attendeeNames.get(i - 1).trim();
            } else if (qty > 1) {
                ticketAttendeeName = attendee.getFullName() + (i == 1 ? " (Primary)" : " (Guest " + i + ")");
            }

            String qrPayload = String.format("QUICKPASS|CODE:%s|EVENT:%d|USER:%s|TIER:%s|TICKET:%d/%d",
                    ticketCode, event.getId(), attendee.getEmail(), tier.getName(), i, qty);
            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(qrPayload, 260, 260);

            Ticket ticket = new Ticket(booking, tier, event, ticketCode, ticketAttendeeName, qrCodeBase64);
            booking.addTicket(ticket);
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings(Long attendeeId) {
        return bookingRepository.findByAttendeeIdOrderByBookedAtDesc(attendeeId);
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    public List<Booking> getEventBookings(Long eventId) {
        return bookingRepository.findByEventIdOrderByBookedAtDesc(eventId);
    }
}
