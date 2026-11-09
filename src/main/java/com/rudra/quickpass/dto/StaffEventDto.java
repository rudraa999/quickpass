package com.rudra.quickpass.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class StaffEventDto {
    private Long eventId;
    private String title;
    private String category;
    private String venue;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDateTime;

    private String organizerName;
    private String organizerEmail;
    private String staffInstructions;
    private String gateRole;

    private int totalSold;
    private int totalScanned;
    private double attendanceRate;

    public StaffEventDto() {
    }

    public StaffEventDto(Long eventId, String title, String category, String venue,
                         LocalDateTime eventDateTime, String organizerName, String organizerEmail,
                         String staffInstructions, String gateRole, int totalSold, int totalScanned, double attendanceRate) {
        this.eventId = eventId;
        this.title = title;
        this.category = category;
        this.venue = venue;
        this.eventDateTime = eventDateTime;
        this.organizerName = organizerName;
        this.organizerEmail = organizerEmail;
        this.staffInstructions = staffInstructions;
        this.gateRole = gateRole;
        this.totalSold = totalSold;
        this.totalScanned = totalScanned;
        this.attendanceRate = attendanceRate;
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

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    public String getStaffInstructions() {
        return staffInstructions;
    }

    public void setStaffInstructions(String staffInstructions) {
        this.staffInstructions = staffInstructions;
    }

    public String getGateRole() {
        return gateRole;
    }

    public void setGateRole(String gateRole) {
        this.gateRole = gateRole;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public int getTotalScanned() {
        return totalScanned;
    }

    public void setTotalScanned(int totalScanned) {
        this.totalScanned = totalScanned;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }
}

