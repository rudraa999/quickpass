package com.rudra.quickpass.controller;

import com.rudra.quickpass.dto.BookPassRequest;
import com.rudra.quickpass.dto.CreateEventRequest;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.security.CustomUserDetails;
import com.rudra.quickpass.service.BookingService;
import com.rudra.quickpass.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventService eventService;
    private final BookingService bookingService;

    public EventApiController(EventService eventService, BookingService bookingService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User organizer = userDetails.getUser();
        Event event = eventService.createEvent(request, organizer);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Event submitted successfully! Waiting for Admin approval.");
        response.put("eventId", event.getId());
        response.put("status", event.getStatus().name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookPass(
            @Valid @RequestBody BookPassRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User attendee = userDetails.getUser();
        Booking booking = bookingService.bookPass(request, attendee);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Pass booked successfully!");
        response.put("ticketCode", booking.getTicketCode());
        response.put("eventName", booking.getEvent().getTitle());
        response.put("passType", booking.getPassType().name());
        response.put("quantity", booking.getQuantity());
        response.put("totalAmount", booking.getTotalAmount());
        response.put("qrCodeBase64", booking.getQrCodeBase64());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Enforce that ONLY Main Admin can approve
        if (userDetails == null || !Role.ROLE_ADMIN.name().equals(userDetails.getRole())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied: Only QuickPass Main Admin can approve event requests.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        eventService.approveEvent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event approved successfully! The creator's role has been updated to Organizer and the event is live.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Enforce that ONLY Main Admin can reject
        if (userDetails == null || !Role.ROLE_ADMIN.name().equals(userDetails.getRole())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied: Only QuickPass Main Admin can reject event requests.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        eventService.rejectEvent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event has been rejected.");
        return ResponseEntity.ok(response);
    }
}
