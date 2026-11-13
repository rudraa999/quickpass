package com.rudra.quickpass.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketDto {

    private Long id;
    private String ticketCode;
    private Long tierId;
    private String tierName;
    private BigDecimal price;
    private String attendeeName;
    private String qrCodeBase64;
    private boolean isUsed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scannedAt;

    private String scannedByName;

    public TicketDto() {
    }

    public TicketDto(Long id, String ticketCode, Long tierId, String tierName, BigDecimal price,
                     String attendeeName, String qrCodeBase64, boolean isUsed,
                     LocalDateTime scannedAt, String scannedByName) {
        this.id = id;
        this.ticketCode = ticketCode;
        this.tierId = tierId;
        this.tierName = tierName;
        this.price = price;
        this.attendeeName = attendeeName;
        this.qrCodeBase64 = qrCodeBase64;
        this.isUsed = isUsed;
        this.scannedAt = scannedAt;
        this.scannedByName = scannedByName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public Long getTierId() {
        return tierId;
    }

    public void setTierId(Long tierId) {
        this.tierId = tierId;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public String getScannedByName() {
        return scannedByName;
    }

    public void setScannedByName(String scannedByName) {
        this.scannedByName = scannedByName;
    }
}

