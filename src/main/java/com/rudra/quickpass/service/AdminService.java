package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.AdminBookingDto;
import com.rudra.quickpass.dto.AdminDashboardStatsDto;
import com.rudra.quickpass.dto.AdminUserDto;
import com.rudra.quickpass.entity.*;
import com.rudra.quickpass.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Value("${quickpass.admin.email:admin@quickpass.com}")
    private String rootAdminEmail;

    public AdminService(EventRepository eventRepository,
                        BookingRepository bookingRepository,
                        TicketRepository ticketRepository,
                        UserRepository userRepository,
                        EventService eventService) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getDashboardStats() {
        AdminDashboardStatsDto stats = new AdminDashboardStatsDto();

        List<Booking> allBookings = bookingRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Booking b : allBookings) {
            if (b.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(b.getTotalAmount());
            }
        }

        int totalTicketsSold = (int) ticketRepository.count();
        int totalScanned = (int) ticketRepository.countByIsUsedTrue();

        stats.setTotalGrossRevenue(totalRevenue);
        stats.setTotalTicketsSold(totalTicketsSold);
        stats.setTotalBookingsCount(allBookings.size());
        stats.setTotalScannedPasses(totalScanned);

        if (totalTicketsSold > 0) {
            double rate = ((double) totalScanned / totalTicketsSold) * 100.0;
            stats.setGlobalAttendanceRate(Math.round(rate * 10.0) / 10.0);
        } else {
            stats.setGlobalAttendanceRate(0.0);
        }

        // Event counts
        stats.setTotalEvents(eventRepository.count());
        stats.setPendingEventsCount(eventRepository.countByStatus(EventStatus.PENDING_APPROVAL));
        stats.setApprovedEventsCount(eventRepository.countByStatus(EventStatus.APPROVED));
        stats.setRejectedEventsCount(eventRepository.countByStatus(EventStatus.REJECTED));

        // User counts
        stats.setTotalUsers(userRepository.count());
        stats.setAttendeesCount(userRepository.countByRole(Role.ROLE_ATTENDEE));
        stats.setOrganizersCount(userRepository.countByRole(Role.ROLE_ORGANIZER));
        stats.setAdminsCount(userRepository.countByRole(Role.ROLE_ADMIN));

        return stats;
    }

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatusOrderByCreatedAtDesc(EventStatus.PENDING_APPROVAL);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> getAllUsers() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        List<AdminUserDto> dtos = new ArrayList<>(users.size());

        for (User u : users) {
            long eventsCount = eventRepository.countByOrganizerId(u.getId());
            long bookingsCount = bookingRepository.countByAttendeeId(u.getId());
            boolean isRoot = rootAdminEmail.equalsIgnoreCase(u.getEmail());

            dtos.add(new AdminUserDto(
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRole(),
                    u.getCreatedAt(),
                    eventsCount,
                    bookingsCount,
                    isRoot));
        }

        return dtos;
    }

    public List<AdminBookingDto> getAllBookings() {
        return getMasterLedger();
    }

    @Transactional(readOnly = true)
    public List<AdminBookingDto> getMasterLedger() {
        List<Ticket> tickets = ticketRepository.findAll();
        List<AdminBookingDto> dtos = new ArrayList<>(tickets.size());

        for (Ticket t : tickets) {
            Booking b = t.getBooking();
            Event e = t.getEvent();
            User attendee = b != null ? b.getAttendee() : null;

            Long eventId = e != null ? e.getId() : null;
            String eventTitle = e != null ? e.getTitle() : "N/A";
            Long attendeeId = attendee != null ? attendee.getId() : null;
            String attendeeName = t.getAttendeeName() != null ? t.getAttendeeName() : (attendee != null ? attendee.getFullName() : "Unknown");
            String attendeeEmail = attendee != null ? attendee.getEmail() : "N/A";
            String tierName = t.getTicketTier() != null ? t.getTicketTier().getName() : "Standard";
            BigDecimal unitPrice = t.getTicketTier() != null ? t.getTicketTier().getPrice() : BigDecimal.ZERO;

            PassType passType = PassType.NORMAL;
            if (tierName != null) {
                if (tierName.toUpperCase().contains("VVIP")) passType = PassType.VVIP;
                else if (tierName.toUpperCase().contains("VIP")) passType = PassType.VIP;
            }

            AdminBookingDto dto = new AdminBookingDto(
                    b != null ? b.getId() : t.getId(),
                    t.getTicketCode(),
                    eventId,
                    eventTitle,
                    attendeeId,
                    attendeeName,
                    attendeeEmail,
                    passType,
                    1,
                    unitPrice,
                    unitPrice,
                    t.isUsed(),
                    t.getScannedAt(),
                    b != null ? b.getBookedAt() : null);
            dto.setOrderNumber(b != null ? b.getOrderNumber() : "ORD-" + t.getId());
            dto.setTierName(tierName);
            dto.setScannedByName(t.getScannedBy() != null ? t.getScannedBy().getFullName() : null);
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional
    public Event approveEvent(Long eventId) {
        return eventService.approveEvent(eventId);
    }

    @Transactional
    public Event rejectEvent(Long eventId) {
        return eventService.rejectEvent(eventId);
    }

    @Transactional
    public Event delistEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        event.setStatus(EventStatus.REJECTED);
        return eventRepository.save(event);
    }

    @Transactional
    public Event restoreEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        event.setStatus(EventStatus.APPROVED);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        if (bookingRepository.existsByEventId(eventId)) {
            throw new IllegalStateException(
                    "Cannot delete event because tickets have already been booked. Please delist/reject it instead to preserve attendee records.");
        }

        eventRepository.delete(event);
    }

    @Transactional
    public User updateUserRole(Long userId, Role newRole, Long currentAdminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (rootAdminEmail.equalsIgnoreCase(user.getEmail()) && newRole != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Root administrator account role cannot be revoked.");
        }

        if (user.getId().equals(currentAdminId) && newRole != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You cannot revoke your own administrator privileges.");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional
    public Ticket toggleBookingCheckIn(Long ticketId, Long adminUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        boolean nextState = !ticket.isUsed();
        ticket.setUsed(nextState);
        ticket.setScannedAt(nextState ? LocalDateTime.now() : null);
        if (nextState && adminUserId != null) {
            userRepository.findById(adminUserId).ifPresent(ticket::setScannedBy);
        } else if (!nextState) {
            ticket.setScannedBy(null);
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket toggleBookingCheckIn(Long ticketId) {
        return toggleBookingCheckIn(ticketId, null);
    }
}
