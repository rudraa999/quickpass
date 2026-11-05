package com.rudra.quickpass.dto;

import com.rudra.quickpass.entity.PassType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BookPassRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Pass type is required")
    private PassType passType;

    @Min(value = 1, message = "Must purchase at least 1 pass")
    private int quantity = 1;

    public BookPassRequest() {
    }

    public BookPassRequest(Long eventId, PassType passType, int quantity) {
        this.eventId = eventId;
        this.passType = passType;
        this.quantity = quantity;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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
}

