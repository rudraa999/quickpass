package com.rudra.quickpass.controller;

import com.rudra.quickpass.dto.AdminBookingDto;
import com.rudra.quickpass.dto.AdminDashboardStatsDto;
import com.rudra.quickpass.dto.AdminUserDto;
import com.rudra.quickpass.dto.EventAnalyticsDto;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.security.CustomUserDetails;
import com.rudra.quickpass.service.AdminService;
import com.rudra.quickpass.service.BookingService;
import com.rudra.quickpass.service.EventAnalyticsService;
import com.rudra.quickpass.service.EventService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final EventAnalyticsService eventAnalyticsService;
    private final AdminService adminService;

    @Value("${quickpass.jwt.cookie-name:quickpass_token}")
    private String cookieName;

    public WebController(EventService eventService,
                         BookingService bookingService,
                         EventAnalyticsService eventAnalyticsService,
                         AdminService adminService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.eventAnalyticsService = eventAnalyticsService;
        this.adminService = adminService;
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    private void populateUserHeader(Model model, CustomUserDetails user) {
        if (user != null) {
            model.addAttribute("fullName", user.getFullName());
            model.addAttribute("email", user.getUsername());
            model.addAttribute("role", user.getRole());
            model.addAttribute("isAdmin", Role.ROLE_ADMIN.name().equals(user.getRole()));
            model.addAttribute("isStaff", Role.ROLE_STAFF.name().equals(user.getRole()));
            model.addAttribute("canAccessStaffPortal", Role.ROLE_STAFF.name().equals(user.getRole())
                    || Role.ROLE_ORGANIZER.name().equals(user.getRole())
                    || Role.ROLE_ADMIN.name().equals(user.getRole()));
        }
    }

    @GetMapping("/")
    public String home() {
        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "logout", required = false) String logout,
            HttpServletResponse response
    ) {
        if (logout != null) {
            Cookie cookie = new Cookie(cookieName, "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            SecurityContextHolder.clearContext();
            return "login";
        }

        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @GetMapping({"/dashboard", "/events"})
    public String dashboard(Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/events";
        }
        populateUserHeader(model, user);

        List<Event> approvedEvents = eventService.getApprovedEvents();
        model.addAttribute("events", approvedEvents);
        return "dashboard";
    }

    @GetMapping("/organizer")
    public String organizerHub(Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/organizer";
        }
        populateUserHeader(model, user);

        List<Event> myEvents = eventService.getOrganizerEvents(user.getId());
        model.addAttribute("myEvents", myEvents);
        return "organizer";
    }

    @GetMapping("/organizer/events/{id}")
    public String organizerEventDashboard(@PathVariable Long id, Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/organizer/events/" + id;
        }
        populateUserHeader(model, user);

        boolean isAdmin = Role.ROLE_ADMIN.name().equals(user.getRole());
        try {
            EventAnalyticsDto analytics = eventAnalyticsService.getEventAnalytics(id, user.getId(), isAdmin);
            model.addAttribute("analytics", analytics);
            return "event-dashboard";
        } catch (AccessDeniedException ex) {
            return "redirect:/organizer?error=unauthorized";
        } catch (IllegalArgumentException ex) {
            return "redirect:/organizer?error=notfound";
        }
    }

    @GetMapping("/organizer/create-event")
    public String createEventPage(Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/organizer/create-event";
        }
        populateUserHeader(model, user);
        return "create-event";
    }

    @GetMapping("/organizer/my-events")
    public String organizerMyEvents() {
        return "redirect:/organizer";
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/my-bookings";
        }
        populateUserHeader(model, user);

        List<Booking> bookings = bookingService.getMyBookings(user.getId());
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    @GetMapping({"/admin", "/admin/dashboard", "/admin/events"})
    public String adminPortal(
            @RequestParam(value = "tab", required = false, defaultValue = "overview") String tab,
            Model model
    ) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/admin";
        }
        // Strict permission check: only ROLE_ADMIN is allowed to view admin portal
        if (!Role.ROLE_ADMIN.name().equals(user.getRole())) {
            return "redirect:/dashboard";
        }
        populateUserHeader(model, user);

        AdminDashboardStatsDto stats = adminService.getDashboardStats();
        List<Event> pendingEvents = adminService.getPendingEvents();
        List<Event> allEvents = adminService.getAllEvents();
        List<AdminUserDto> users = adminService.getAllUsers();
        List<AdminBookingDto> bookings = adminService.getAllBookings();

        model.addAttribute("stats", stats);
        model.addAttribute("pendingEvents", pendingEvents);
        model.addAttribute("allEvents", allEvents);
        model.addAttribute("users", users);
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeTab", tab);

        return "admin-events";
    }

    @GetMapping("/staff/events")
    public String staffEvents(Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/staff/events";
        }
        populateUserHeader(model, user);

        List<Event> assignedEvents;
        if (Role.ROLE_ADMIN.name().equals(user.getRole())) {
            assignedEvents = eventService.getApprovedEvents();
        } else if (Role.ROLE_ORGANIZER.name().equals(user.getRole())) {
            assignedEvents = eventService.getOrganizerEvents(user.getId());
            List<Event> staffed = eventService.getEventsForStaff(user.getId());
            for (Event e : staffed) {
                if (assignedEvents.stream().noneMatch(existing -> existing.getId().equals(e.getId()))) {
                    assignedEvents.add(e);
                }
            }
        } else {
            assignedEvents = eventService.getEventsForStaff(user.getId());
        }

        model.addAttribute("assignedEvents", assignedEvents);
        return "staff-events";
    }

    @GetMapping("/staff/events/{id}")
    public String staffScannerPortal(@PathVariable Long id, Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/staff/events/" + id;
        }
        populateUserHeader(model, user);

        Event event = eventService.getEventById(id);
        boolean isAdmin = Role.ROLE_ADMIN.name().equals(user.getRole());
        boolean isOrganizer = event.getOrganizer() != null && event.getOrganizer().getId().equals(user.getId());
        boolean isStaff = eventService.isUserStaffForEvent(id, user.getId());

        if (!isAdmin && !isOrganizer && !isStaff) {
            return "redirect:/staff/events?error=unauthorized";
        }

        List<Booking> bookings = bookingService.getEventBookings(id);
        int totalSold = bookings.stream().mapToInt(Booking::getQuantity).sum();
        int totalScanned = bookings.stream().filter(Booking::isUsed).mapToInt(Booking::getQuantity).sum();
        double checkInRate = totalSold > 0 ? ((double) totalScanned / totalSold) * 100.0 : 0.0;

        model.addAttribute("event", event);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalSold", totalSold);
        model.addAttribute("totalScanned", totalScanned);
        model.addAttribute("checkInRate", checkInRate);

        return "staff-scanner";
    }

    @GetMapping("/account")
    public String accountPage(Model model) {
        CustomUserDetails user = getCurrentUserDetails();
        if (user == null) {
            return "redirect:/login?redirect=/account";
        }
        populateUserHeader(model, user);
        return "account";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }
}
