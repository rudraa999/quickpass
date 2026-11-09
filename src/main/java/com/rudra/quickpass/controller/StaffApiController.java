package com.rudra.quickpass.controller;

import com.rudra.quickpass.dto.ScanTicketRequest;
import com.rudra.quickpass.dto.ScanTicketResponse;
import com.rudra.quickpass.dto.StaffEventDto;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.security.CustomUserDetails;
import com.rudra.quickpass.service.EventAnalyticsService;
import com.rudra.quickpass.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ORGANIZER', 'ROLE_ADMIN')")
public class StaffApiController {

    private final StaffService staffService;
    private final EventAnalyticsService eventAnalyticsService;

    public StaffApiController(StaffService staffService, EventAnalyticsService eventAnalyticsService) {
        this.staffService = staffService;
        this.eventAnalyticsService = eventAnalyticsService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<StaffEventDto>> getAssignedEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(staffService.getAssignedEventsForStaff(userDetails.getId()));
    }

    @PostMapping("/events/{eventId}/scan")
    public ResponseEntity<ScanTicketResponse> scanTicket(
            @PathVariable Long eventId,
            @Valid @RequestBody ScanTicketRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(userDetails.getRole());
        ScanTicketResponse response = eventAnalyticsService.scanTicketForEvent(
                eventId, request.getTicketCode(), userDetails.getId(), isAdmin
        );
        return ResponseEntity.ok(response);
    }
}

