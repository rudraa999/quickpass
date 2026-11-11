package com.rudra.quickpass.dto;

import com.rudra.quickpass.entity.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventAnalyticsDto {
    private Long eventId;
    private String title;
    private String description;
    private String category;
    private String venue;
    private LocalDateTime eventDateTime;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    private EventStatus status;
    private String organizerName;

    // Totals - Sales
    private int totalCapacity;
    private int totalSold;
    private int totalAvailable;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    private int totalOrders;
    private double soldPercentage;

    // Totals - Entry / Check-In Analytics
    private int totalScanned;
    private int remainingAttendees;
    private double checkInPercentage;

    // Pass Tier Breakdown
    private PassTierAnalyticsDto normal;
    private PassTierAnalyticsDto vip;
    private PassTierAnalyticsDto vvip;

    // Full Attendee Guest List
    private List<AttendeeBookingDto> bookings = new ArrayList<>();

    public EventAnalyticsDto() {
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public int getTotalAvailable() {
        return totalAvailable;
    }

    public void setTotalAvailable(int totalAvailable) {
        this.totalAvailable = totalAvailable;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getSoldPercentage() {
        return soldPercentage;
    }

    public void setSoldPercentage(double soldPercentage) {
        this.soldPercentage = soldPercentage;
    }

    public int getTotalScanned() {
        return totalScanned;
    }

    public void setTotalScanned(int totalScanned) {
        this.totalScanned = totalScanned;
    }

    public int getRemainingAttendees() {
        return remainingAttendees;
    }

    public void setRemainingAttendees(int remainingAttendees) {
        this.remainingAttendees = remainingAttendees;
    }

    public double getCheckInPercentage() {
        return checkInPercentage;
    }

    public void setCheckInPercentage(double checkInPercentage) {
        this.checkInPercentage = checkInPercentage;
    }

    public PassTierAnalyticsDto getNormal() {
        return normal;
    }

    public void setNormal(PassTierAnalyticsDto normal) {
        this.normal = normal;
    }

    public PassTierAnalyticsDto getVip() {
        return vip;
    }

    public void setVip(PassTierAnalyticsDto vip) {
        this.vip = vip;
    }

    public PassTierAnalyticsDto getVvip() {
        return vvip;
    }

    public void setVvip(PassTierAnalyticsDto vvip) {
        this.vvip = vvip;
    }

    public List<AttendeeBookingDto> getBookings() {
        return bookings;
    }

    public void setBookings(List<AttendeeBookingDto> bookings) {
        this.bookings = bookings;
    }
}

