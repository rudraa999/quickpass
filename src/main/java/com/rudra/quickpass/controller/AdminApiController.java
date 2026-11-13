package com.rudra.quickpass.controller;

import com.rudra.quickpass.dto.AdminBookingDto;
import com.rudra.quickpass.dto.AdminDashboardStatsDto;
import com.rudra.quickpass.dto.AdminUserDto;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.security.CustomUserDetails;
import com.rudra.quickpass.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @PostMapping("/events/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveEvent(@PathVariable Long id) {
        Event event = adminService.approveEvent(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Event '" + event.getTitle() + "' approved successfully and is now live!");
        res.put("eventId", event.getId());
        res.put("status", event.getStatus().name());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/events/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectEvent(@PathVariable Long id) {
        Event event = adminService.rejectEvent(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Event '" + event.getTitle() + "' was rejected.");
        res.put("eventId", event.getId());
        res.put("status", event.getStatus().name());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/events/{id}/delist")
    public ResponseEntity<Map<String, Object>> delistEvent(@PathVariable Long id) {
        Event event = adminService.delistEvent(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Event '" + event.getTitle() + "' has been delisted from the public platform.");
        res.put("eventId", event.getId());
        res.put("status", event.getStatus().name());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/events/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreEvent(@PathVariable Long id) {
        Event event = adminService.restoreEvent(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Event '" + event.getTitle() + "' restored and published live!");
        res.put("eventId", event.getId());
        res.put("status", event.getStatus().name());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        try {
            adminService.deleteEvent(id);
            Map<String, String> res = new HashMap<>();
            res.put("message", "Event deleted successfully.");
            return ResponseEntity.ok(res);
        } catch (IllegalStateException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails currentAdmin
    ) {
        String roleStr = payload.get("role");
        if (roleStr == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Role is required");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            Role newRole = Role.valueOf(roleStr);
            User updated = adminService.updateUserRole(id, newRole, currentAdmin.getId());
            Map<String, Object> res = new HashMap<>();
            res.put("message", "User " + updated.getFullName() + " role updated to " + updated.getRole().name());
            res.put("userId", updated.getId());
            res.put("newRole", updated.getRole().name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingDto>> getBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @PostMapping("/bookings/{id}/toggle-scan")
    public ResponseEntity<Map<String, Object>> toggleBookingScan(@PathVariable Long id) {
        com.rudra.quickpass.entity.Ticket ticket = adminService.toggleBookingCheckIn(id);
        Map<String, Object> res = new HashMap<>();
        res.put("ticketId", ticket.getId());
        res.put("isUsed", ticket.isUsed());
        res.put("ticketCode", ticket.getTicketCode());
        res.put("message", ticket.isUsed() ? "Pass checked in successfully" : "Pass check-in revoked");
        return ResponseEntity.ok(res);
    }
}

