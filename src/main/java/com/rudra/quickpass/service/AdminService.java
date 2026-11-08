package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.AdminBookingDto;
import com.rudra.quickpass.dto.AdminDashboardStatsDto;
import com.rudra.quickpass.dto.AdminUserDto;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.BookingRepository;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final EventService eventService;

    @Value("${quickpass.admin.email:admin@quickpass.com}")
    private String rootAdminEmail;

    public AdminService(EventRepository eventRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            EventService eventService) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getDashboardStats() {
        AdminDashboardStatsDto stats = new AdminDashboardStatsDto();

        List<Booking> allBookings = bookingRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalTicketsSold = 0;
        int totalScanned = 0;

        for (Booking b : allBookings) {
            if (b.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(b.getTotalAmount());
            }
            totalTicketsSold += b.getQuantity();
            if (b.isUsed()) {
                totalScanned += b.getQuantity();
            }
        }

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
            long eventsCount = eventRepository.findByOrganizerIdOrderByCreatedAtDesc(u.getId()).size();
            long bookingsCount = bookingRepository.countByAttendeeId(u.getId());
            boolean isRoot = u.getEmail() != null && u.getEmail().equalsIgnoreCase(rootAdminEmail);

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

    @Transactional(readOnly = true)
    public List<AdminBookingDto> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllByOrderByBookedAtDesc();
        List<AdminBookingDto> dtos = new ArrayList<>(bookings.size());

        for (Booking b : bookings) {
            Long eventId = b.getEvent() != null ? b.getEvent().getId() : null;
            String eventTitle = b.getEvent() != null ? b.getEvent().getTitle() : "N/A";
            Long attendeeId = b.getAttendee() != null ? b.getAttendee().getId() : null;
            String attendeeName = b.getAttendee() != null ? b.getAttendee().getFullName() : "Unknown";
            String attendeeEmail = b.getAttendee() != null ? b.getAttendee().getEmail() : "N/A";

            dtos.add(new AdminBookingDto(
                    b.getId(),
                    b.getTicketCode(),
                    eventId,
                    eventTitle,
                    attendeeId,
                    attendeeName,
                    attendeeEmail,
                    b.getPassType(),
                    b.getQuantity(),
                    b.getUnitPrice(),
                    b.getTotalAmount(),
                    b.isUsed(),
                    b.getScannedAt(),
                    b.getBookedAt()));
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
    public User updateUserRole(Long targetUserId, Role newRole, Long currentAdminId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + targetUserId));

        if (targetUser.getEmail() != null && targetUser.getEmail().equalsIgnoreCase(rootAdminEmail)) {
            throw new IllegalArgumentException("The primary root administrator role cannot be altered.");
        }

        if (targetUser.getId().equals(currentAdminId) && newRole != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You cannot revoke your own administrator privileges.");
        }

        targetUser.setRole(newRole);
        return userRepository.save(targetUser);
    }

    @Transactional
    public Booking toggleBookingCheckIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        boolean nextState = !booking.isUsed();
        booking.setUsed(nextState);
        booking.setScannedAt(nextState ? LocalDateTime.now() : null);
        return bookingRepository.save(booking);
    }
}
