package com.rudra.quickpass.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rudra.quickpass.entity.PassType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AttendeeBookingDto {
    private Long bookingId;
    private Long ticketId;
    private String orderNumber;
    private String ticketCode;
    private String attendeeName;
    private String attendeeEmail;
    private PassType passType;
    private String tierName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private boolean isUsed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scannedAt;

    private String scannedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookedAt;

    private List<TicketDto> tickets;

    public AttendeeBookingDto() {
    }

    public AttendeeBookingDto(Long bookingId, String ticketCode, String attendeeName, String attendeeEmail,
            PassType passType, int quantity, BigDecimal unitPrice, BigDecimal totalAmount,
            boolean isUsed, LocalDateTime scannedAt, LocalDateTime bookedAt) {
        this.bookingId = bookingId;
        this.ticketCode = ticketCode;
        this.attendeeName = attendeeName;
        this.attendeeEmail = attendeeEmail;
        this.passType = passType;
        this.tierName = passType != null ? passType.name() : "Standard";
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

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public PassType getPassType() {
        return passType;
    }

    public void setPassType(PassType passType) {
        this.passType = passType;
    }

    public String getTierName() {
        return tierName != null ? tierName : (passType != null ? passType.name() : "Standard");
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
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

    public String getScannedByName() {
        return scannedByName;
    }

    public void setScannedByName(String scannedByName) {
        this.scannedByName = scannedByName;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public List<TicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDto> tickets) {
        this.tickets = tickets;
    }
}
