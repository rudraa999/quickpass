package com.rudra.quickpass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateEventRequest {

    @NotBlank(message = "Event title is required")
    private String title;

    private String description;
    private String category;

    @NotBlank(message = "Venue location is required")
    private String venue;

    @NotNull(message = "Event date and time is required")
    private LocalDateTime eventDateTime;

    @NotNull(message = "Booking start date is required")
    private LocalDateTime bookingStartDate;

    @NotNull(message = "Booking end date is required")
    private LocalDateTime bookingEndDate;

    // Normal Pass Tier
    private BigDecimal normalPrice = BigDecimal.ZERO;
    private int normalCapacity = 0;

    // VIP Pass Tier
    private BigDecimal vipPrice = BigDecimal.ZERO;
    private int vipCapacity = 0;

    // VVIP Pass Tier
    private BigDecimal vvipPrice = BigDecimal.ZERO;
    private int vvipCapacity = 0;

    // Dynamic Ticket Tiers
    private java.util.List<CreateTicketTierRequest> tiers = new java.util.ArrayList<>();

    public CreateEventRequest() {
    }

    // Getters and Setters
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

    public java.util.List<CreateTicketTierRequest> getTiers() {
        return tiers;
    }

    public void setTiers(java.util.List<CreateTicketTierRequest> tiers) {
        this.tiers = tiers;
    }
}

