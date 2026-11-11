package com.rudra.quickpass.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // Normal Pass Tier
    @Column(nullable = false)
    private BigDecimal normalPrice = BigDecimal.ZERO;
    private int normalCapacity = 0;
    private int normalAvailable = 0;

    // VIP Pass Tier
    @Column(nullable = false)
    private BigDecimal vipPrice = BigDecimal.ZERO;
    private int vipCapacity = 0;
    private int vipAvailable = 0;

    // VVIP Pass Tier
    @Column(nullable = false)
    private BigDecimal vvipPrice = BigDecimal.ZERO;
    private int vvipCapacity = 0;
    private int vvipAvailable = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

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
        return normalAvailable + vipAvailable + vvipAvailable;
    }

    public int getTotalCapacity() {
        return normalCapacity + vipCapacity + vvipCapacity;
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

    public BigDecimal getNormalPrice() {
        return normalPrice;
    }

    public void setNormalPrice(BigDecimal normalPrice) {
        this.normalPrice = normalPrice;
    }

    public int getNormalCapacity() {
        return normalCapacity;
    }

    public void setNormalCapacity(int normalCapacity) {
        this.normalCapacity = normalCapacity;
    }

    public int getNormalAvailable() {
        return normalAvailable;
    }

    public void setNormalAvailable(int normalAvailable) {
        this.normalAvailable = normalAvailable;
    }

    public BigDecimal getVipPrice() {
        return vipPrice;
    }

    public void setVipPrice(BigDecimal vipPrice) {
        this.vipPrice = vipPrice;
    }

    public int getVipCapacity() {
        return vipCapacity;
    }

    public void setVipCapacity(int vipCapacity) {
        this.vipCapacity = vipCapacity;
    }

    public int getVipAvailable() {
        return vipAvailable;
    }

    public void setVipAvailable(int vipAvailable) {
        this.vipAvailable = vipAvailable;
    }

    public BigDecimal getVvipPrice() {
        return vvipPrice;
    }

    public void setVvipPrice(BigDecimal vvipPrice) {
        this.vvipPrice = vvipPrice;
    }

    public int getVvipCapacity() {
        return vvipCapacity;
    }

    public void setVvipCapacity(int vvipCapacity) {
        this.vvipCapacity = vvipCapacity;
    }

    public int getVvipAvailable() {
        return vvipAvailable;
    }

    public void setVvipAvailable(int vvipAvailable) {
        this.vvipAvailable = vvipAvailable;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

