package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.StaffEventDto;
import com.rudra.quickpass.dto.StaffMemberDto;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStaff;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.BookingRepository;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.EventStaffRepository;
import com.rudra.quickpass.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StaffService {

    private final EventRepository eventRepository;
    private final EventStaffRepository eventStaffRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(EventRepository eventRepository,
                        EventStaffRepository eventStaffRepository,
                        UserRepository userRepository,
                        BookingRepository bookingRepository,
                        PasswordEncoder passwordEncoder) {
        this.eventRepository = eventRepository;
        this.eventStaffRepository = eventStaffRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void validateOrganizerOrAdmin(Event event, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return;
        if (event.getOrganizer() == null || !event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied: Only the event organizer or platform admin can manage gate staff.");
        }
    }

    @Transactional
    public StaffMemberDto addStaffToEvent(Long eventId, String staffEmail, String gateRole, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateOrganizerOrAdmin(event, currentUserId, isAdmin);

        if (staffEmail == null || staffEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff email is required.");
        }

        String cleanEmail = staffEmail.trim().toLowerCase();

        // 1. Find or create user
        User staffUser = userRepository.findByEmail(cleanEmail).orElseGet(() -> {
            String defaultName = cleanEmail.split("@")[0];
            if (defaultName.length() > 0) {
                defaultName = Character.toUpperCase(defaultName.charAt(0)) + defaultName.substring(1) + " (Staff)";
            } else {
                defaultName = "Gate Staff";
            }
            User newUser = new User(
                    defaultName,
                    cleanEmail,
                    passwordEncoder.encode("staff123"),
                    Role.ROLE_STAFF
            );
            return userRepository.save(newUser);
        });

        // 2. If user is a standard attendee, elevate their role to ROLE_STAFF
        if (staffUser.getRole() == Role.ROLE_ATTENDEE) {
            staffUser.setRole(Role.ROLE_STAFF);
            staffUser = userRepository.save(staffUser);
        }

        // 3. Prevent duplicate assignment
        if (eventStaffRepository.existsByEventIdAndStaffId(event.getId(), staffUser.getId())) {
            throw new IllegalArgumentException("User " + cleanEmail + " is already assigned as gate staff for this event.");
        }

        EventStaff eventStaff = new EventStaff(event, staffUser, gateRole);
        eventStaff = eventStaffRepository.save(eventStaff);

        return new StaffMemberDto(
                eventStaff.getId(),
                staffUser.getId(),
                staffUser.getFullName(),
                staffUser.getEmail(),
                staffUser.getRole(),
                eventStaff.getGateRole(),
                eventStaff.getAssignedAt()
        );
    }

    @Transactional
    public void assignStaffEmailsBatch(Event event, String staffEmailsStr) {
        if (staffEmailsStr == null || staffEmailsStr.trim().isEmpty()) {
            return;
        }

        String[] emails = staffEmailsStr.split("[,;\\s]+");
        for (String email : emails) {
            String clean = email.trim().toLowerCase();
            if (clean.isEmpty() || !clean.contains("@")) continue;

            User staffUser = userRepository.findByEmail(clean).orElseGet(() -> {
                String defaultName = clean.split("@")[0];
                if (defaultName.length() > 0) {
                    defaultName = Character.toUpperCase(defaultName.charAt(0)) + defaultName.substring(1) + " (Staff)";
                } else {
                    defaultName = "Gate Staff";
                }
                User newUser = new User(
                        defaultName,
                        clean,
                        passwordEncoder.encode("staff123"),
                        Role.ROLE_STAFF
                );
                return userRepository.save(newUser);
            });

            if (staffUser.getRole() == Role.ROLE_ATTENDEE) {
                staffUser.setRole(Role.ROLE_STAFF);
                staffUser = userRepository.save(staffUser);
            }

            if (!eventStaffRepository.existsByEventIdAndStaffId(event.getId(), staffUser.getId())) {
                EventStaff es = new EventStaff(event, staffUser, "Gate Staff");
                eventStaffRepository.save(es);
            }
        }
    }

    @Transactional
    public void removeStaffFromEvent(Long eventId, Long staffUserId, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateOrganizerOrAdmin(event, currentUserId, isAdmin);

        eventStaffRepository.deleteByEventIdAndStaffId(eventId, staffUserId);
    }

    @Transactional
    public Event updateStaffInstructions(Long eventId, String instructions, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        validateOrganizerOrAdmin(event, currentUserId, isAdmin);

        event.setStaffInstructions(instructions);
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<StaffMemberDto> getStaffForEvent(Long eventId, Long currentUserId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        if (!isAdmin && (event.getOrganizer() == null || !event.getOrganizer().getId().equals(currentUserId))) {
            // Also allow staff member themselves
            if (!eventStaffRepository.existsByEventIdAndStaffId(eventId, currentUserId)) {
                throw new AccessDeniedException("Access denied: You are not authorized to view staff roster for this event.");
            }
        }

        List<EventStaff> list = eventStaffRepository.findByEventIdOrderByAssignedAtDesc(eventId);
        List<StaffMemberDto> dtos = new ArrayList<>(list.size());
        for (EventStaff es : list) {
            User u = es.getStaff();
            dtos.add(new StaffMemberDto(
                    es.getId(),
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRole(),
                    es.getGateRole(),
                    es.getAssignedAt()
            ));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<StaffEventDto> getAssignedEventsForStaff(Long staffUserId) {
        List<EventStaff> assignments = eventStaffRepository.findByStaffIdOrderByAssignedAtDesc(staffUserId);
        List<StaffEventDto> dtos = new ArrayList<>(assignments.size());

        for (EventStaff es : assignments) {
            Event ev = es.getEvent();
            List<Booking> bookings = bookingRepository.findByEventIdOrderByBookedAtDesc(ev.getId());

            int totalSold = 0;
            int totalScanned = 0;
            for (Booking b : bookings) {
                totalSold += b.getQuantity();
                if (b.isUsed()) {
                    totalScanned += b.getQuantity();
                }
            }

            double rate = totalSold > 0 ? ((double) totalScanned / totalSold) * 100.0 : 0.0;
            rate = Math.round(rate * 10.0) / 10.0;

            String orgName = ev.getOrganizer() != null ? ev.getOrganizer().getFullName() : "Organizer";
            String orgEmail = ev.getOrganizer() != null ? ev.getOrganizer().getEmail() : "";

            dtos.add(new StaffEventDto(
                    ev.getId(),
                    ev.getTitle(),
                    ev.getCategory(),
                    ev.getVenue(),
                    ev.getEventDateTime(),
                    orgName,
                    orgEmail,
                    ev.getStaffInstructions(),
                    es.getGateRole(),
                    totalSold,
                    totalScanned,
                    rate
            ));
        }

        return dtos;
    }
}

