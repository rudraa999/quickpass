package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.CreateEventRequest;
import com.rudra.quickpass.dto.StaffMemberDto;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EventService(EventRepository eventRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Event createEvent(CreateEventRequest req, User organizer) {
        Event event = new Event();
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setCategory(req.getCategory());
        event.setVenue(req.getVenue());
        event.setEventDateTime(req.getEventDateTime());
        event.setBookingStartDate(req.getBookingStartDate());
        event.setBookingEndDate(req.getBookingEndDate());

        // Dynamic Ticket Tiers creation
        if (req.getTiers() != null && !req.getTiers().isEmpty()) {
            for (com.rudra.quickpass.dto.CreateTicketTierRequest tierReq : req.getTiers()) {
                if (tierReq.getName() != null && !tierReq.getName().trim().isEmpty() && tierReq.getCapacity() != null && tierReq.getCapacity() > 0) {
                    com.rudra.quickpass.entity.TicketTier tier = new com.rudra.quickpass.entity.TicketTier(
                            event,
                            tierReq.getName().trim(),
                            tierReq.getPrice() != null ? tierReq.getPrice() : java.math.BigDecimal.ZERO,
                            tierReq.getCapacity(),
                            tierReq.getCapacity()
                    );
                    event.addTier(tier);
                }
            }
        }

        // If no dynamic tiers were explicitly sent, create default tiers from form fields
        if (event.getTiers().isEmpty()) {
            if (req.getNormalCapacity() > 0) {
                event.addTier(new com.rudra.quickpass.entity.TicketTier(event, "Normal Pass", req.getNormalPrice() != null ? req.getNormalPrice() : java.math.BigDecimal.ZERO, req.getNormalCapacity(), req.getNormalCapacity()));
            }
            if (req.getVipCapacity() > 0) {
                event.addTier(new com.rudra.quickpass.entity.TicketTier(event, "VIP Pass", req.getVipPrice() != null ? req.getVipPrice() : java.math.BigDecimal.ZERO, req.getVipCapacity(), req.getVipCapacity()));
            }
            if (req.getVvipCapacity() > 0) {
                event.addTier(new com.rudra.quickpass.entity.TicketTier(event, "VVIP Pass", req.getVvipPrice() != null ? req.getVvipPrice() : java.math.BigDecimal.ZERO, req.getVvipCapacity(), req.getVvipCapacity()));
            }
        }

        // New event starts as PENDING_APPROVAL
        event.setStatus(EventStatus.PENDING_APPROVAL);
        event.setOrganizer(organizer);

        return eventRepository.save(event);
    }

    public List<Event> getApprovedEvents() {
        return eventRepository.findByStatusOrderByEventDateTimeAsc(EventStatus.APPROVED);
    }

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatusOrderByCreatedAtDesc(EventStatus.PENDING_APPROVAL);
    }

    public List<Event> getOrganizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
    }

    @Transactional
    public Event approveEvent(Long eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.APPROVED);

        // When the event is approved by the admin, promote the creator's role to
        // ROLE_ORGANIZER in the database
        User organizer = event.getOrganizer();
        if (organizer != null && organizer.getRole() != Role.ROLE_ADMIN) {
            organizer.setRole(Role.ROLE_ORGANIZER);
            userRepository.save(organizer);
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event rejectEvent(Long eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.REJECTED);
        return eventRepository.save(event);
    }

    @Transactional
    public StaffMemberDto addStaffToEvent(Long eventId, String email, String fullName, Long currentUserId,
            boolean isAdmin) {
        Event event = getEventById(eventId);
        validateOrganizerAccess(event, currentUserId, isAdmin);

        String normalizedEmail = email.trim().toLowerCase();
        User staffUser = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (staffUser == null) {
            // Auto-provision staff user account with default password
            String displayName = (fullName != null && !fullName.trim().isEmpty())
                    ? fullName.trim()
                    : capitalize(normalizedEmail.split("@")[0]);

            staffUser = new User(
                    displayName,
                    normalizedEmail,
                    passwordEncoder.encode("Staff@123"),
                    Role.ROLE_STAFF);
            userRepository.save(staffUser);
        } else {
            // If existing user is standard attendee, upgrade their role to ROLE_STAFF
            if (staffUser.getRole() == Role.ROLE_ATTENDEE) {
                staffUser.setRole(Role.ROLE_STAFF);
                userRepository.save(staffUser);
            }
        }

        event.addStaffMember(staffUser);
        eventRepository.save(event);

        return new StaffMemberDto(staffUser.getId(), staffUser.getFullName(), staffUser.getEmail(),
                staffUser.getRole().name());
    }

    @Transactional
    public void removeStaffFromEvent(Long eventId, Long staffUserId, Long currentUserId, boolean isAdmin) {
        Event event = getEventById(eventId);
        validateOrganizerAccess(event, currentUserId, isAdmin);

        User staffUser = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffUserId));

        event.removeStaffMember(staffUser);
        eventRepository.save(event);
    }

    public List<StaffMemberDto> getStaffForEvent(Long eventId, Long currentUserId, boolean isAdmin) {
        Event event = getEventById(eventId);
        validateOrganizerAccess(event, currentUserId, isAdmin);

        List<StaffMemberDto> list = new ArrayList<>();
        if (event.getStaffMembers() != null) {
            for (User u : event.getStaffMembers()) {
                list.add(new StaffMemberDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name()));
            }
        }
        return list;
    }

    public List<Event> getEventsForStaff(Long staffUserId) {
        return eventRepository.findEventsByStaffId(staffUserId);
    }

    public boolean isUserStaffForEvent(Long eventId, Long userId) {
        return eventRepository.isUserStaffForEvent(eventId, userId);
    }

    private void validateOrganizerAccess(Event event, Long currentUserId, boolean isAdmin) {
        if (isAdmin)
            return;
        if (event.getOrganizer() == null || !event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied: You are not the organizer of this event.");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return "Staff Member";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
