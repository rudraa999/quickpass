package com.rudra.quickpass.dto;

import java.math.BigDecimal;

public class AdminDashboardStatsDto {
    private BigDecimal totalGrossRevenue = BigDecimal.ZERO;
    private int totalTicketsSold = 0;
    private int totalBookingsCount = 0;
    private int totalScannedPasses = 0;
    private double globalAttendanceRate = 0.0;

    // Event Stats
    private long totalEvents = 0;
    private long pendingEventsCount = 0;
    private long approvedEventsCount = 0;
    private long rejectedEventsCount = 0;

    // User Stats
    private long totalUsers = 0;
    private long attendeesCount = 0;
    private long organizersCount = 0;
    private long adminsCount = 0;

    public AdminDashboardStatsDto() {
    }

    public BigDecimal getTotalGrossRevenue() {
        return totalGrossRevenue;
    }

    public void setTotalGrossRevenue(BigDecimal totalGrossRevenue) {
        this.totalGrossRevenue = totalGrossRevenue;
    }

    public int getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(int totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public int getTotalBookingsCount() {
        return totalBookingsCount;
    }

    public void setTotalBookingsCount(int totalBookingsCount) {
        this.totalBookingsCount = totalBookingsCount;
    }

    public int getTotalScannedPasses() {
        return totalScannedPasses;
    }

    public void setTotalScannedPasses(int totalScannedPasses) {
        this.totalScannedPasses = totalScannedPasses;
    }

    public double getGlobalAttendanceRate() {
        return globalAttendanceRate;
    }

    public void setGlobalAttendanceRate(double globalAttendanceRate) {
        this.globalAttendanceRate = globalAttendanceRate;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getPendingEventsCount() {
        return pendingEventsCount;
    }

    public void setPendingEventsCount(long pendingEventsCount) {
        this.pendingEventsCount = pendingEventsCount;
    }

    public long getApprovedEventsCount() {
        return approvedEventsCount;
    }

    public void setApprovedEventsCount(long approvedEventsCount) {
        this.approvedEventsCount = approvedEventsCount;
    }

    public long getRejectedEventsCount() {
        return rejectedEventsCount;
    }

    public void setRejectedEventsCount(long rejectedEventsCount) {
        this.rejectedEventsCount = rejectedEventsCount;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getAttendeesCount() {
        return attendeesCount;
    }

    public void setAttendeesCount(long attendeesCount) {
        this.attendeesCount = attendeesCount;
    }

    public long getOrganizersCount() {
        return organizersCount;
    }

    public void setOrganizersCount(long organizersCount) {
        this.organizersCount = organizersCount;
    }

    public long getAdminsCount() {
        return adminsCount;
    }

    public void setAdminsCount(long adminsCount) {
        this.adminsCount = adminsCount;
    }
}

