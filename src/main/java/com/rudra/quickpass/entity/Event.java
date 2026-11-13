package com.rudra.quickpass.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime eventDateTime;

    @Column(nullable = false)
    private LocalDateTime bookingStartDate;

    @Column(nullable = false)
    private LocalDateTime bookingEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_staff",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> staffMembers = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<TicketTier> tiers = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Event() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EventStatus.PENDING_APPROVAL;
        }
    }

    public int getTotalAvailablePasses() {
        if (tiers != null && !tiers.isEmpty()) {
            return tiers.stream().mapToInt(TicketTier::getAvailableCapacity).sum();
        }
        return 0;
    }

    public int getTotalCapacity() {
        if (tiers != null && !tiers.isEmpty()) {
            return tiers.stream().mapToInt(TicketTier::getTotalCapacity).sum();
        }
        return 0;
    }

    public BigDecimal getStartingPrice() {
        if (tiers != null && !tiers.isEmpty()) {
            return tiers.stream()
                    .map(TicketTier::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    // ==================== Backward-Compatible Transient Getters ====================
    // These computed helpers prevent SpEL template errors across Thymeleaf views
    // without creating any legacy columns in the database.

    @Transient
    public BigDecimal getNormalPrice() {
        return getTierPrice("Normal Pass");
    }

    @Transient
    public int getNormalCapacity() {
        return getTierCapacity("Normal Pass");
    }

    @Transient
    public int getNormalAvailable() {
        return getTierAvailable("Normal Pass");
    }

    @Transient
    public BigDecimal getVipPrice() {
        return getTierPrice("VIP Pass");
    }

    @Transient
    public int getVipCapacity() {
        return getTierCapacity("VIP Pass");
    }

    @Transient
    public int getVipAvailable() {
        return getTierAvailable("VIP Pass");
    }

    @Transient
    public BigDecimal getVvipPrice() {
        return getTierPrice("VVIP Pass");
    }

    @Transient
    public int getVvipCapacity() {
        return getTierCapacity("VVIP Pass");
    }

    @Transient
    public int getVvipAvailable() {
        return getTierAvailable("VVIP Pass");
    }

    private BigDecimal getTierPrice(String tierName) {
        if (tiers != null) {
            for (TicketTier t : tiers) {
                if (t.getName() != null && t.getName().equalsIgnoreCase(tierName)) {
                    return t.getPrice();
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private int getTierCapacity(String tierName) {
        if (tiers != null) {
            for (TicketTier t : tiers) {
                if (t.getName() != null && t.getName().equalsIgnoreCase(tierName)) {
                    return t.getTotalCapacity();
                }
            }
        }
        return 0;
    }

    private int getTierAvailable(String tierName) {
        if (tiers != null) {
            for (TicketTier t : tiers) {
                if (t.getName() != null && t.getName().equalsIgnoreCase(tierName)) {
                    return t.getAvailableCapacity();
                }
            }
        }
        return 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public LocalDateTime getBookingStartDate() {
        return bookingStartDate;
    }

    public void setBookingStartDate(LocalDateTime bookingStartDate) {
        this.bookingStartDate = bookingStartDate;
    }

    public LocalDateTime getBookingEndDate() {
        return bookingEndDate;
    }

    public void setBookingEndDate(LocalDateTime bookingEndDate) {
        this.bookingEndDate = bookingEndDate;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Set<User> getStaffMembers() {
        return staffMembers;
    }

    public void setStaffMembers(Set<User> staffMembers) {
        this.staffMembers = staffMembers;
    }

    public void addStaffMember(User user) {
        this.staffMembers.add(user);
    }

    public void removeStaffMember(User user) {
        this.staffMembers.remove(user);
    }

    public List<TicketTier> getTiers() {
        return tiers;
    }

    public void setTiers(List<TicketTier> tiers) {
        this.tiers = tiers;
    }

    public void addTier(TicketTier tier) {
        if (this.tiers == null) {
            this.tiers = new ArrayList<>();
        }
        this.tiers.add(tier);
        tier.setEvent(this);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
