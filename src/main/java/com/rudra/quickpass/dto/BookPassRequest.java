package com.rudra.quickpass.dto;

import com.rudra.quickpass.entity.PassType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BookPassRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    private Long tierId;

    private PassType passType;

    @Min(value = 1, message = "Must purchase at least 1 pass")
    private int quantity = 1;

    private List<String> attendeeNames;

    public BookPassRequest() {
    }

    public BookPassRequest(Long eventId, Long tierId, int quantity) {
        this.eventId = eventId;
        this.tierId = tierId;
        this.quantity = quantity;
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

    public Long getTierId() {
        return tierId;
    }

    public void setTierId(Long tierId) {
        this.tierId = tierId;
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

    public List<String> getAttendeeNames() {
        return attendeeNames;
    }

    public void setAttendeeNames(List<String> attendeeNames) {
        this.attendeeNames = attendeeNames;
    }
}
