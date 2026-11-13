package com.rudra.quickpass.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", length = 50, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", nullable = false)
    private User attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity = 1;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 30, nullable = false)
    private String status = "CONFIRMED";

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Ticket> tickets = new ArrayList<>();

    public Booking() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.bookedAt == null) {
            this.bookedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "CONFIRMED";
        }
    }

    // Helper Transient methods for backward compatibility
    @Transient
    public String getTicketCode() {
        return (tickets != null && !tickets.isEmpty()) ? tickets.get(0).getTicketCode() : null;
    }

    @Transient
    public String getQrCodeBase64() {
        return (tickets != null && !tickets.isEmpty()) ? tickets.get(0).getQrCodeBase64() : null;
    }

    @Transient
    public boolean isUsed() {
        return tickets != null && !tickets.isEmpty() && tickets.stream().allMatch(Ticket::isUsed);
    }

    @Transient
    public LocalDateTime getScannedAt() {
        return (tickets != null && !tickets.isEmpty()) ? tickets.get(0).getScannedAt() : null;
    }

    @Transient
    public int getQuantity() {
        return totalQuantity;
    }

    @Transient
    public String getPassType() {
        return (tickets != null && !tickets.isEmpty() && tickets.get(0).getTicketTier() != null)
                ? tickets.get(0).getTicketTier().getName()
                : "Pass";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public User getAttendee() {
        return attendee;
    }

    public void setAttendee(User attendee) {
        this.attendee = attendee;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        if (this.tickets == null) {
            this.tickets = new ArrayList<>();
        }
        this.tickets.add(ticket);
        ticket.setBooking(this);
    }
}
