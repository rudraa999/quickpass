package com.rudra.quickpass.controller;

import com.rudra.quickpass.dto.AddStaffRequest;
import com.rudra.quickpass.dto.BookPassRequest;
import com.rudra.quickpass.dto.CreateEventRequest;
import com.rudra.quickpass.dto.EventAnalyticsDto;
import com.rudra.quickpass.dto.ScanTicketRequest;
import com.rudra.quickpass.dto.ScanTicketResponse;
import com.rudra.quickpass.dto.StaffMemberDto;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.security.CustomUserDetails;
import com.rudra.quickpass.service.BookingService;
import com.rudra.quickpass.service.EventAnalyticsService;
import com.rudra.quickpass.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final EventAnalyticsService eventAnalyticsService;

    public EventApiController(EventService eventService,
                              BookingService bookingService,
                              EventAnalyticsService eventAnalyticsService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.eventAnalyticsService = eventAnalyticsService;
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
        response.put("orderNumber", booking.getOrderNumber());
        response.put("ticketCode", booking.getTicketCode());
        response.put("eventName", booking.getEvent().getTitle());
        response.put("passType", booking.getPassType() != null ? booking.getPassType() : "Standard");
        response.put("quantity", booking.getQuantity());
        response.put("totalAmount", booking.getTotalAmount());
        response.put("qrCodeBase64", booking.getQrCodeBase64());

        List<Map<String, Object>> ticketsList = new ArrayList<>();
        if (booking.getTickets() != null) {
            for (com.rudra.quickpass.entity.Ticket t : booking.getTickets()) {
                Map<String, Object> tMap = new HashMap<>();
                tMap.put("id", t.getId());
                tMap.put("ticketCode", t.getTicketCode());
                tMap.put("attendeeName", t.getAttendeeName());
                tMap.put("tierName", t.getTicketTier() != null ? t.getTicketTier().getName() : "Standard");
                tMap.put("qrCodeBase64", t.getQrCodeBase64());
                ticketsList.add(tMap);
            }
        }
        response.put("tickets", ticketsList);
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

    @GetMapping("/{id}/analytics")
    public ResponseEntity<EventAnalyticsDto> getEventAnalytics(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        EventAnalyticsDto analytics = eventAnalyticsService.getEventAnalytics(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/{id}/scan")
    public ResponseEntity<ScanTicketResponse> scanTicket(
            @PathVariable Long id,
            @Valid @RequestBody ScanTicketRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        ScanTicketResponse response = eventAnalyticsService.scanTicketForEvent(
                id, request.getTicketCode(), userDetails.getId(), isAdmin
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/bookings/{bookingId}/toggle-scan")
    public ResponseEntity<Map<String, Object>> toggleBookingScan(
            @PathVariable Long id,
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        boolean isNowUsed = eventAnalyticsService.toggleBookingCheckIn(id, bookingId, userDetails.getId(), isAdmin);

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("isUsed", isNowUsed);
        response.put("message", isNowUsed ? "Attendee checked in successfully" : "Attendee check-in revoked");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/staff")
    public ResponseEntity<Map<String, Object>> addStaff(
            @PathVariable Long id,
            @Valid @RequestBody AddStaffRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        StaffMemberDto staffDto = eventService.addStaffToEvent(
                id, request.getEmail(), request.getFullName(), userDetails.getId(), isAdmin
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Staff member " + staffDto.getFullName() + " (" + staffDto.getEmail() + ") assigned to event successfully!");
        response.put("staff", staffDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<StaffMemberDto>> getStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        List<StaffMemberDto> staffList = eventService.getStaffForEvent(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(staffList);
    }

    @DeleteMapping("/{id}/staff/{userId}")
    public ResponseEntity<Map<String, String>> removeStaff(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        eventService.removeStaffFromEvent(id, userId, userDetails.getId(), isAdmin);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Staff member removed from event.");
        return ResponseEntity.ok(response);
    }
}
