package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.*;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.PassType;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.BookingRepository;
import com.rudra.quickpass.repository.EventRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventAnalyticsService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventAnalyticsService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    public EventAnalyticsDto getEventAnalytics(Long eventId, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateOwnership(event, currentUserId, isAdmin);

        List<Booking> bookings = bookingRepository.findByEventIdOrderByBookedAtDesc(eventId);
        return buildEventAnalytics(event, bookings);
    }

    @Transactional
    public ScanTicketResponse scanTicketForEvent(Long eventId, String ticketInput, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateScannerAccess(event, currentUserId, isAdmin);

        String cleanCode = extractTicketCode(ticketInput);
        if (cleanCode.isEmpty()) {
            return ScanTicketResponse.invalid("Invalid ticket code format.");
        }

        Booking booking = bookingRepository.findByEventIdAndTicketCode(eventId, cleanCode)
                .orElse(null);

        if (booking == null) {
            boolean existsElsewhere = bookingRepository.existsByTicketCode(cleanCode);
            if (existsElsewhere) {
                return ScanTicketResponse.invalid("Ticket code '" + cleanCode + "' belongs to a DIFFERENT event, not this one!");
            }
            return ScanTicketResponse.invalid("Ticket code '" + cleanCode + "' not found in the QuickPass system.");
        }

        List<Booking> allEventBookings = bookingRepository.findByEventIdOrderByBookedAtDesc(eventId);
        int totalSold = allEventBookings.stream().mapToInt(Booking::getQuantity).sum();
        int totalScanned = allEventBookings.stream().filter(Booking::isUsed).mapToInt(Booking::getQuantity).sum();

        String attendeeName = booking.getAttendee() != null ? booking.getAttendee().getFullName() : "Guest";
        String attendeeEmail = booking.getAttendee() != null ? booking.getAttendee().getEmail() : "N/A";

        if (booking.isUsed()) {
            return ScanTicketResponse.alreadyScanned(
                    "Warning: This ticket was ALREADY checked in on " +
                            (booking.getScannedAt() != null ? booking.getScannedAt().toString() : "an earlier scan") +
                            ". Possible duplicate entry attempt!",
                    booking.getTicketCode(),
                    attendeeName,
                    attendeeEmail,
                    booking.getPassType().name(),
                    booking.getQuantity(),
                    event.getTitle(),
                    booking.getScannedAt(),
                    totalScanned,
                    totalSold
            );
        }

        // Mark as checked in
        booking.setUsed(true);
        booking.setScannedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        totalScanned += booking.getQuantity();

        return ScanTicketResponse.success(
                "Check-in verified successfully! Welcome " + attendeeName + " (" + booking.getQuantity() + "x " + booking.getPassType() + ")",
                booking.getTicketCode(),
                attendeeName,
                attendeeEmail,
                booking.getPassType().name(),
                booking.getQuantity(),
                event.getTitle(),
                booking.getScannedAt(),
                totalScanned,
                totalSold
        );
    }

    @Transactional
    public boolean toggleBookingCheckIn(Long eventId, Long bookingId, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateScannerAccess(event, currentUserId, isAdmin);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (!booking.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Booking does not belong to this event");
        }

        boolean newStatus = !booking.isUsed();
        booking.setUsed(newStatus);
        booking.setScannedAt(newStatus ? LocalDateTime.now() : null);
        bookingRepository.save(booking);

        return newStatus;
    }

    public EventAnalyticsDto buildEventAnalytics(Event event, List<Booking> bookings) {
        EventAnalyticsDto dto = new EventAnalyticsDto();
        dto.setEventId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setCategory(event.getCategory());
        dto.setVenue(event.getVenue());
        dto.setEventDateTime(event.getEventDateTime());
        dto.setBookingStartDate(event.getBookingStartDate());
        dto.setBookingEndDate(event.getBookingEndDate());
        dto.setStatus(event.getStatus());
        dto.setOrganizerName(event.getOrganizer() != null ? event.getOrganizer().getFullName() : "Organizer");
        dto.setTotalCapacity(event.getTotalCapacity());

        int normalSold = 0, vipSold = 0, vvipSold = 0;
        int normalScanned = 0, vipScanned = 0, vvipScanned = 0;
        BigDecimal normalRevenue = BigDecimal.ZERO;
        BigDecimal vipRevenue = BigDecimal.ZERO;
        BigDecimal vvipRevenue = BigDecimal.ZERO;

        List<AttendeeBookingDto> bookingDtos = new ArrayList<>();

        for (Booking b : bookings) {
            int qty = b.getQuantity();
            BigDecimal amount = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
            boolean scanned = b.isUsed();

            if (b.getPassType() != null) {
                switch (b.getPassType()) {
                    case NORMAL -> {
                        normalSold += qty;
                        normalRevenue = normalRevenue.add(amount);
                        if (scanned) normalScanned += qty;
                    }
                    case VIP -> {
                        vipSold += qty;
                        vipRevenue = vipRevenue.add(amount);
                        if (scanned) vipScanned += qty;
                    }
                    case VVIP -> {
                        vvipSold += qty;
                        vvipRevenue = vvipRevenue.add(amount);
                        if (scanned) vvipScanned += qty;
                    }
                }
            }

            String attendeeName = b.getAttendee() != null ? b.getAttendee().getFullName() : "Guest";
            String attendeeEmail = b.getAttendee() != null ? b.getAttendee().getEmail() : "N/A";

            bookingDtos.add(new AttendeeBookingDto(
                    b.getId(),
                    b.getTicketCode(),
                    attendeeName,
                    attendeeEmail,
                    b.getPassType(),
                    b.getQuantity(),
                    b.getUnitPrice(),
                    b.getTotalAmount(),
                    b.isUsed(),
                    b.getScannedAt(),
                    b.getBookedAt()
            ));
        }

        dto.setBookings(bookingDtos);
        dto.setTotalOrders(bookings.size());

        PassTierAnalyticsDto normalDto = new PassTierAnalyticsDto(
                PassType.NORMAL,
                event.getNormalPrice(),
                event.getNormalCapacity(),
                normalSold,
                event.getNormalAvailable(),
                normalRevenue,
                normalScanned
        );

        PassTierAnalyticsDto vipDto = new PassTierAnalyticsDto(
                PassType.VIP,
                event.getVipPrice(),
                event.getVipCapacity(),
                vipSold,
                event.getVipAvailable(),
                vipRevenue,
                vipScanned
        );

        PassTierAnalyticsDto vvipDto = new PassTierAnalyticsDto(
                PassType.VVIP,
                event.getVvipPrice(),
                event.getVvipCapacity(),
                vvipSold,
                event.getVvipAvailable(),
                vvipRevenue,
                vvipScanned
        );

        dto.setNormal(normalDto);
        dto.setVip(vipDto);
        dto.setVvip(vvipDto);

        int totalSold = normalSold + vipSold + vvipSold;
        BigDecimal totalRevenue = normalRevenue.add(vipRevenue).add(vvipRevenue);
        int totalScanned = normalScanned + vipScanned + vvipScanned;
        int totalAvailable = event.getNormalAvailable() + event.getVipAvailable() + event.getVvipAvailable();

        dto.setTotalSold(totalSold);
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalScanned(totalScanned);
        dto.setTotalAvailable(totalAvailable);
        dto.setRemainingAttendees(Math.max(0, totalSold - totalScanned));

        if (bookings.size() > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            dto.setAverageOrderValue(totalRevenue.divide(BigDecimal.valueOf(bookings.size()), 2, RoundingMode.HALF_UP));
        }

        int cap = event.getTotalCapacity();
        dto.setSoldPercentage(cap > 0 ? ((double) totalSold / cap) * 100.0 : 0.0);
        dto.setCheckInPercentage(totalSold > 0 ? ((double) totalScanned / totalSold) * 100.0 : 0.0);

        // Staff Crew Roster
        List<StaffMemberDto> staffDtoList = new ArrayList<>();
        if (event.getStaffMembers() != null) {
            for (User s : event.getStaffMembers()) {
                staffDtoList.add(new StaffMemberDto(s.getId(), s.getFullName(), s.getEmail(), s.getRole().name()));
            }
        }
        dto.setStaffMembers(staffDtoList);

        return dto;
    }

    public void validateScannerAccess(Event event, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return; // Platform admin has universal scanner access
        if (event.getOrganizer() != null && event.getOrganizer().getId().equals(currentUserId)) {
            return; // Event organizer has scanner access
        }
        if (event.getStaffMembers() != null && event.getStaffMembers().stream().anyMatch(u -> u.getId().equals(currentUserId))) {
            return; // Assigned staff member has scanner access
        }
        throw new AccessDeniedException("Access denied: You are neither the organizer nor an assigned staff member for this event.");
    }

    private void validateOwnership(Event event, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return; // Platform admin can view analytics
        if (event.getOrganizer() == null || !event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied: You are not the organizer of this event.");
        }
    }

    private String extractTicketCode(String raw) {
        if (raw == null) return "";
        raw = raw.trim();
        // If raw is barcode/QR content e.g. QUICKPASS|CODE:QP-12345678|EVENT:1|...
        if (raw.startsWith("QUICKPASS|") || raw.contains("|CODE:")) {
            for (String part : raw.split("\\|")) {
                if (part.startsWith("CODE:")) {
                    return part.substring(5).trim();
                }
            }
        }
        return raw;
    }
}

