package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.*;
import com.rudra.quickpass.entity.*;
import com.rudra.quickpass.repository.*;
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
    private final TicketRepository ticketRepository;
    private final TicketTierRepository ticketTierRepository;
    private final UserRepository userRepository;

    public EventAnalyticsService(EventRepository eventRepository,
                                 BookingRepository bookingRepository,
                                 TicketRepository ticketRepository,
                                 TicketTierRepository ticketTierRepository,
                                 UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.ticketTierRepository = ticketTierRepository;
        this.userRepository = userRepository;
    }

    public EventAnalyticsDto getEventAnalytics(Long eventId, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateOwnership(event, currentUserId, isAdmin);

        List<Booking> bookings = bookingRepository.findByEventIdOrderByBookedAtDesc(eventId);
        List<Ticket> tickets = ticketRepository.findByEventIdWithDetails(eventId);
        List<TicketTier> tiers = ticketTierRepository.findByEventIdOrderByIdAsc(eventId);

        return buildEventAnalytics(event, bookings, tickets, tiers);
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

        User scannerStaff = userRepository.findById(currentUserId).orElse(null);
        String scannerName = scannerStaff != null ? scannerStaff.getFullName() : "Gate Crew";

        // Try finding individual Ticket
        Ticket ticket = ticketRepository.findByTicketCode(cleanCode).orElse(null);

        long totalSold = ticketRepository.countByEventId(eventId);
        long totalScanned = ticketRepository.countByEventIdAndIsUsedTrue(eventId);

        if (ticket == null) {
            boolean existsElsewhere = ticketRepository.existsByTicketCode(cleanCode);
            if (existsElsewhere) {
                return ScanTicketResponse.invalid("Ticket code '" + cleanCode + "' belongs to a DIFFERENT event, not this event!");
            }
            return ScanTicketResponse.invalid("Ticket code '" + cleanCode + "' not found in the QuickPass system.");
        }

        if (!ticket.getEvent().getId().equals(eventId)) {
            return ScanTicketResponse.invalid("Ticket code '" + cleanCode + "' belongs to a DIFFERENT event (" + ticket.getEvent().getTitle() + "), not this event!");
        }

        String attendeeName = ticket.getAttendeeName();
        String attendeeEmail = ticket.getBooking() != null && ticket.getBooking().getAttendee() != null
                ? ticket.getBooking().getAttendee().getEmail() : "N/A";
        String tierName = ticket.getTicketTier() != null ? ticket.getTicketTier().getName() : "Standard";

        if (ticket.isUsed()) {
            String previousStaff = ticket.getScannedBy() != null ? " by " + ticket.getScannedBy().getFullName() : "";
            return ScanTicketResponse.alreadyScanned(
                    "Warning: This ticket was ALREADY checked in on " +
                            (ticket.getScannedAt() != null ? ticket.getScannedAt().toString() : "an earlier scan") +
                            previousStaff + ". Possible duplicate entry attempt!",
                    ticket.getTicketCode(),
                    attendeeName,
                    attendeeEmail,
                    tierName,
                    1,
                    event.getTitle(),
                    ticket.getScannedAt(),
                    ticket.getScannedBy() != null ? ticket.getScannedBy().getFullName() : "Staff",
                    (int) totalScanned,
                    (int) totalSold
            );
        }

        // Mark ticket as used
        ticket.setUsed(true);
        ticket.setScannedAt(LocalDateTime.now());
        ticket.setScannedBy(scannerStaff);
        ticketRepository.save(ticket);

        totalScanned++;

        return ScanTicketResponse.success(
                "Check-in verified successfully! Welcome " + attendeeName + " (" + tierName + ")",
                ticket.getTicketCode(),
                attendeeName,
                attendeeEmail,
                tierName,
                1,
                event.getTitle(),
                ticket.getScannedAt(),
                scannerName,
                (int) totalScanned,
                (int) totalSold
        );
    }

    @Transactional
    public boolean toggleBookingCheckIn(Long eventId, Long bookingOrTicketId, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateScannerAccess(event, currentUserId, isAdmin);

        // Try ticket first
        Ticket ticket = ticketRepository.findById(bookingOrTicketId).orElse(null);
        if (ticket != null && ticket.getEvent().getId().equals(eventId)) {
            boolean newStatus = !ticket.isUsed();
            ticket.setUsed(newStatus);
            ticket.setScannedAt(newStatus ? LocalDateTime.now() : null);
            if (newStatus) {
                userRepository.findById(currentUserId).ifPresent(ticket::setScannedBy);
            } else {
                ticket.setScannedBy(null);
            }
            ticketRepository.save(ticket);
            return newStatus;
        }

        // If bookingId was passed, toggle the first ticket of the booking
        Booking booking = bookingRepository.findById(bookingOrTicketId)
                .orElseThrow(() -> new IllegalArgumentException("Booking or Ticket not found with ID: " + bookingOrTicketId));

        if (!booking.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Booking does not belong to this event");
        }

        List<Ticket> bTickets = ticketRepository.findByBookingIdOrderByIdAsc(booking.getId());
        if (!bTickets.isEmpty()) {
            Ticket firstTicket = bTickets.get(0);
            boolean newStatus = !firstTicket.isUsed();
            firstTicket.setUsed(newStatus);
            firstTicket.setScannedAt(newStatus ? LocalDateTime.now() : null);
            if (newStatus) {
                userRepository.findById(currentUserId).ifPresent(firstTicket::setScannedBy);
            } else {
                firstTicket.setScannedBy(null);
            }
            ticketRepository.save(firstTicket);
            return newStatus;
        }

        return false;
    }

    public EventAnalyticsDto buildEventAnalytics(Event event, List<Booking> bookings, List<Ticket> tickets, List<TicketTier> tiers) {
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

        int totalCap = tiers != null && !tiers.isEmpty()
                ? tiers.stream().mapToInt(TicketTier::getTotalCapacity).sum()
                : 0;
        dto.setTotalCapacity(totalCap);

        // 1. Build Guest Manifest rows
        List<AttendeeBookingDto> bookingDtos = new ArrayList<>();
        if (tickets != null && !tickets.isEmpty()) {
            for (Ticket t : tickets) {
                AttendeeBookingDto row = new AttendeeBookingDto();
                row.setTicketId(t.getId());
                row.setBookingId(t.getBooking() != null ? t.getBooking().getId() : null);
                row.setOrderNumber(t.getBooking() != null ? t.getBooking().getOrderNumber() : "ORD-" + t.getId());
                row.setTicketCode(t.getTicketCode());
                row.setAttendeeName(t.getAttendeeName());
                row.setAttendeeEmail(t.getBooking() != null && t.getBooking().getAttendee() != null
                        ? t.getBooking().getAttendee().getEmail() : "N/A");
                row.setTierName(t.getTicketTier() != null ? t.getTicketTier().getName() : "Standard");
                row.setQuantity(1);
                row.setUnitPrice(t.getTicketTier() != null ? t.getTicketTier().getPrice() : BigDecimal.ZERO);
                row.setTotalAmount(row.getUnitPrice());
                row.setUsed(t.isUsed());
                row.setScannedAt(t.getScannedAt());
                row.setScannedByName(t.getScannedBy() != null ? t.getScannedBy().getFullName() : null);
                row.setBookedAt(t.getBooking() != null ? t.getBooking().getBookedAt() : null);
                bookingDtos.add(row);
            }
        }
        dto.setBookings(bookingDtos);
        dto.setTotalOrders(bookings.size());

        // 2. Build Dynamic Pass Tier Analytics
        List<PassTierAnalyticsDto> tierDtoList = new ArrayList<>();
        int totalSold = 0;
        int totalScanned = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalAvail = 0;

        if (tiers != null && !tiers.isEmpty()) {
            for (TicketTier tier : tiers) {
                int sold = tier.getTotalCapacity() - tier.getAvailableCapacity();
                if (sold < 0) sold = 0;

                long scannedCount = tickets != null
                        ? tickets.stream().filter(t -> t.getTicketTier() != null && t.getTicketTier().getId().equals(tier.getId()) && t.isUsed()).count()
                        : 0;

                BigDecimal rev = tier.getPrice().multiply(BigDecimal.valueOf(sold));

                PassTierAnalyticsDto pDto = new PassTierAnalyticsDto(
                        tier.getId(),
                        tier.getName(),
                        tier.getPrice(),
                        tier.getTotalCapacity(),
                        sold,
                        tier.getAvailableCapacity(),
                        rev,
                        (int) scannedCount
                );
                tierDtoList.add(pDto);

                totalSold += sold;
                totalScanned += (int) scannedCount;
                totalRevenue = totalRevenue.add(rev);
                totalAvail += tier.getAvailableCapacity();

                // Map standard names to normal/vip/vvip
                if (tier.getName().equalsIgnoreCase("Normal Pass")) dto.setNormal(pDto);
                else if (tier.getName().equalsIgnoreCase("VIP Pass")) dto.setVip(pDto);
                else if (tier.getName().equalsIgnoreCase("VVIP Pass")) dto.setVvip(pDto);
            }
        }

        // Fill fallback tiers if empty
        if (dto.getNormal() == null) dto.setNormal(new PassTierAnalyticsDto(PassType.NORMAL, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, 0));
        if (dto.getVip() == null) dto.setVip(new PassTierAnalyticsDto(PassType.VIP, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, 0));
        if (dto.getVvip() == null) dto.setVvip(new PassTierAnalyticsDto(PassType.VVIP, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, 0));

        dto.setTiers(tierDtoList);
        dto.setTotalSold(totalSold);
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalScanned(totalScanned);
        dto.setTotalAvailable(totalAvail);
        dto.setRemainingAttendees(Math.max(0, totalSold - totalScanned));

        if (bookings.size() > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            dto.setAverageOrderValue(totalRevenue.divide(BigDecimal.valueOf(bookings.size()), 2, RoundingMode.HALF_UP));
        }

        dto.setSoldPercentage(totalCap > 0 ? ((double) totalSold / totalCap) * 100.0 : 0.0);
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