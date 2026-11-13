package com.rudra.quickpass.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_tier_id", nullable = false)
    private TicketTier ticketTier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, unique = true, length = 50)
    private String ticketCode;

    @Column(nullable = false)
    private String attendeeName;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeBase64;

    @Column(nullable = false)
    private boolean isUsed = false;

    private LocalDateTime scannedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanned_by_user_id")
    private User scannedBy;

    public Ticket() {
    }

    public Ticket(Booking booking, TicketTier ticketTier, Event event, String ticketCode, String attendeeName, String qrCodeBase64) {
        this.booking = booking;
        this.ticketTier = ticketTier;
        this.event = event;
        this.ticketCode = ticketCode;
        this.attendeeName = attendeeName;
        this.qrCodeBase64 = qrCodeBase64;
        this.isUsed = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public TicketTier getTicketTier() {
        return ticketTier;
    }

    public void setTicketTier(TicketTier ticketTier) {
        this.ticketTier = ticketTier;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
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

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
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

    public User getScannedBy() {
        return scannedBy;
    }

    public void setScannedBy(User scannedBy) {
        this.scannedBy = scannedBy;
    }
}

