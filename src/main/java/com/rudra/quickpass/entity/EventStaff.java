package com.rudra.quickpass.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_staff", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "staff_id"})
})
public class EventStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(nullable = false)
    private String gateRole = "Gate Staff";

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public EventStaff() {
    }

    public EventStaff(Event event, User staff, String gateRole) {
        this.event = event;
        this.staff = staff;
        this.gateRole = (gateRole != null && !gateRole.trim().isEmpty()) ? gateRole.trim() : "Gate Staff";
    }

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
        if (this.gateRole == null || this.gateRole.trim().isEmpty()) {
            this.gateRole = "Gate Staff";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public String getGateRole() {
        return gateRole;
    }

    public void setGateRole(String gateRole) {
        this.gateRole = gateRole;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}

