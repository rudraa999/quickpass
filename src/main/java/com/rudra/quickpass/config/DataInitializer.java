package com.rudra.quickpass.config;

import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.EventRepository;
import com.rudra.quickpass.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${quickpass.admin.email}")
    private String adminEmail;

    @Value("${quickpass.admin.password}")
    private String adminPassword;

    @Value("${quickpass.admin.name:Main Admin}")
    private String adminName;

    public DataInitializer(UserRepository userRepository,
                           EventRepository eventRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Main Admin if not present (configured from launch.json / env)
        User admin = userRepository.findByEmail(adminEmail).map(existing -> {
            boolean updated = false;
            if (existing.getRole() != Role.ROLE_ADMIN) {
                existing.setRole(Role.ROLE_ADMIN);
                updated = true;
            }
            // If the password configured in launch.json changes, update it in the database
            if (!passwordEncoder.matches(adminPassword, existing.getPassword())) {
                existing.setPassword(passwordEncoder.encode(adminPassword));
                updated = true;
            }
            return updated ? userRepository.save(existing) : existing;
        }).orElseGet(() -> {
            User newAdmin = new User();
            newAdmin.setFullName(adminName);
            newAdmin.setEmail(adminEmail);
            newAdmin.setPassword(passwordEncoder.encode(adminPassword));
            newAdmin.setRole(Role.ROLE_ADMIN);
            return userRepository.save(newAdmin);
        });

        // 2. Seed a sample approved event if no events exist yet
        if (eventRepository.count() == 0) {
            Event sampleEvent = new Event();
            sampleEvent.setTitle("Global Tech Innovations Expo 2026");
            sampleEvent.setDescription("Experience keynote speeches, interactive AI demos, and meet global industry leaders across 3 exciting days.");
            sampleEvent.setCategory("Technology & AI");
            sampleEvent.setVenue("Skyline Convention Arena, Hall 4, Tech City");
            sampleEvent.setEventDateTime(LocalDateTime.now().plusDays(20).withHour(10).withMinute(0));
            sampleEvent.setBookingStartDate(LocalDateTime.now().minusDays(2));
            sampleEvent.setBookingEndDate(LocalDateTime.now().plusDays(19));

            // Dynamic Ticket Tiers
            sampleEvent.addTier(new com.rudra.quickpass.entity.TicketTier(sampleEvent, "Normal Pass", BigDecimal.valueOf(49.00), 150, 150));
            sampleEvent.addTier(new com.rudra.quickpass.entity.TicketTier(sampleEvent, "VIP Pass", BigDecimal.valueOf(129.00), 50, 50));
            sampleEvent.addTier(new com.rudra.quickpass.entity.TicketTier(sampleEvent, "VVIP Pass", BigDecimal.valueOf(299.00), 15, 15));

            sampleEvent.setStatus(EventStatus.APPROVED);
            sampleEvent.setOrganizer(admin);

            eventRepository.save(sampleEvent);
        }
    }
}

