package com.rudra.quickpass.dto;

import java.time.LocalDateTime;

public class ScanTicketResponse {
    private String status; // SUCCESS, ALREADY_SCANNED, INVALID
    private String message;
    private String ticketCode;
    private String attendeeName;
    private String attendeeEmail;
    private String passType;
    private int quantity;
    private String eventTitle;
    private LocalDateTime scannedAt;
    private int totalTicketsScanned;
    private int totalTicketsSold;
    private double checkInPercentage;

    public ScanTicketResponse() {
    }

    public static ScanTicketResponse success(String message, String ticketCode, String attendeeName,
                                            String attendeeEmail, String passType, int quantity,
                                            String eventTitle, LocalDateTime scannedAt,
                                            int totalTicketsScanned, int totalTicketsSold) {
        ScanTicketResponse res = new ScanTicketResponse();
        res.setStatus("SUCCESS");
        res.setMessage(message);
        res.setTicketCode(ticketCode);
        res.setAttendeeName(attendeeName);
        res.setAttendeeEmail(attendeeEmail);
        res.setPassType(passType);
        res.setQuantity(quantity);
        res.setEventTitle(eventTitle);
        res.setScannedAt(scannedAt);
        res.setTotalTicketsScanned(totalTicketsScanned);
        res.setTotalTicketsSold(totalTicketsSold);
        res.setCheckInPercentage(totalTicketsSold > 0 ? ((double) totalTicketsScanned / totalTicketsSold) * 100.0 : 0.0);
        return res;
    }

    public static ScanTicketResponse alreadyScanned(String message, String ticketCode, String attendeeName,
                                                   String attendeeEmail, String passType, int quantity,
                                                   String eventTitle, LocalDateTime scannedAt,
                                                   int totalTicketsScanned, int totalTicketsSold) {
        ScanTicketResponse res = new ScanTicketResponse();
        res.setStatus("ALREADY_SCANNED");
        res.setMessage(message);
        res.setTicketCode(ticketCode);
        res.setAttendeeName(attendeeName);
        res.setAttendeeEmail(attendeeEmail);
        res.setPassType(passType);
        res.setQuantity(quantity);
        res.setEventTitle(eventTitle);
        res.setScannedAt(scannedAt);
        res.setTotalTicketsScanned(totalTicketsScanned);
        res.setTotalTicketsSold(totalTicketsSold);
        res.setCheckInPercentage(totalTicketsSold > 0 ? ((double) totalTicketsScanned / totalTicketsSold) * 100.0 : 0.0);
        return res;
    }

    public static ScanTicketResponse invalid(String message) {
        ScanTicketResponse res = new ScanTicketResponse();
        res.setStatus("INVALID");
        res.setMessage(message);
        return res;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }

    public String getPassType() {
        return passType;
    }

    public void setPassType(String passType) {
        this.passType = passType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public int getTotalTicketsScanned() {
        return totalTicketsScanned;
    }

    public void setTotalTicketsScanned(int totalTicketsScanned) {
        this.totalTicketsScanned = totalTicketsScanned;
    }

    public int getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(int totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public double getCheckInPercentage() {
        return checkInPercentage;
    }

    public void setCheckInPercentage(double checkInPercentage) {
        this.checkInPercentage = checkInPercentage;
    }
}

