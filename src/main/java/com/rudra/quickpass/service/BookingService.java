package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.BookPassRequest;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.BookingRepository;
import com.rudra.quickpass.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final QrCodeService qrCodeService;

    public BookingService(BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          QrCodeService qrCodeService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
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

        BigDecimal unitPrice;
        int qty = req.getQuantity();

        switch (req.getPassType()) {
            case NORMAL -> {
                if (event.getNormalAvailable() < qty) {
                    throw new IllegalArgumentException("Not enough Normal passes available. Remaining: " + event.getNormalAvailable());
                }
                unitPrice = event.getNormalPrice();
                event.setNormalAvailable(event.getNormalAvailable() - qty);
            }
            case VIP -> {
                if (event.getVipAvailable() < qty) {
                    throw new IllegalArgumentException("Not enough VIP passes available. Remaining: " + event.getVipAvailable());
                }
                unitPrice = event.getVipPrice();
                event.setVipAvailable(event.getVipAvailable() - qty);
            }
            case VVIP -> {
                if (event.getVvipAvailable() < qty) {
                    throw new IllegalArgumentException("Not enough VVIP passes available. Remaining: " + event.getVvipAvailable());
                }
                unitPrice = event.getVvipPrice();
                event.setVvipAvailable(event.getVvipAvailable() - qty);
            }
            default -> throw new IllegalArgumentException("Invalid pass type: " + req.getPassType());
        }

        eventRepository.save(event);

        // Generate unique ticket code
        String ticketCode = "QP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Generate QR code data
        String qrPayload = String.format("QUICKPASS|CODE:%s|EVENT:%d|USER:%s|TYPE:%s|QTY:%d",
                ticketCode, event.getId(), attendee.getEmail(), req.getPassType(), qty);

        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(qrPayload, 260, 260);

        Booking booking = new Booking();
        booking.setTicketCode(ticketCode);
        booking.setAttendee(attendee);
        booking.setEvent(event);
        booking.setPassType(req.getPassType());
        booking.setQuantity(qty);
        booking.setUnitPrice(unitPrice);
        booking.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(qty)));
        booking.setQrCodeBase64(qrCodeBase64);

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

