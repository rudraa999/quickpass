package com.rudra.quickpass.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rudra.quickpass.entity.PassType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminBookingDto {
    private Long bookingId;
    private String ticketCode;
    private Long eventId;
    private String eventTitle;
    private Long attendeeId;
    private String attendeeName;
    private String attendeeEmail;
    private PassType passType;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private boolean isUsed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scannedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookedAt;

    public AdminBookingDto() {
    }

    public AdminBookingDto(Long bookingId, String ticketCode, Long eventId, String eventTitle,
                           Long attendeeId, String attendeeName, String attendeeEmail,
                           PassType passType, int quantity, BigDecimal unitPrice, BigDecimal totalAmount,
                           boolean isUsed, LocalDateTime scannedAt, LocalDateTime bookedAt) {
        this.bookingId = bookingId;
        this.ticketCode = ticketCode;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.attendeeId = attendeeId;
        this.attendeeName = attendeeName;
        this.attendeeEmail = attendeeEmail;
        this.passType = passType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.isUsed = isUsed;
        this.scannedAt = scannedAt;
        this.bookedAt = bookedAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public Long getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(Long attendeeId) {
        this.attendeeId = attendeeId;
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

    public PassType getPassType() {
        return passType;
    }

    public void setPassType(PassType passType) {
        this.passType = passType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }
}

