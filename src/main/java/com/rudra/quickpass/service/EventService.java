package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.CreateEventRequest;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final StaffService staffService;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        StaffService staffService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.staffService = staffService;
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
        event.setStaffInstructions(req.getStaffInstructions());

        // Normal Tier
        event.setNormalPrice(req.getNormalPrice());
        event.setNormalCapacity(req.getNormalCapacity());
        event.setNormalAvailable(req.getNormalCapacity());

        // VIP Tier
        event.setVipPrice(req.getVipPrice());
        event.setVipCapacity(req.getVipCapacity());
        event.setVipAvailable(req.getVipCapacity());

        // VVIP Tier
        event.setVvipPrice(req.getVvipPrice());
        event.setVvipCapacity(req.getVvipCapacity());
        event.setVvipAvailable(req.getVvipCapacity());

        // New event starts as PENDING_APPROVAL
        event.setStatus(EventStatus.PENDING_APPROVAL);
        event.setOrganizer(organizer);

        Event savedEvent = eventRepository.save(event);

        // Assign initial staff emails if provided
        if (req.getStaffEmails() != null && !req.getStaffEmails().trim().isEmpty()) {
            staffService.assignStaffEmailsBatch(savedEvent, req.getStaffEmails());
        }

        return savedEvent;
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

        // When the event is approved by the admin, promote the creator's role to ROLE_ORGANIZER in the database
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
}

