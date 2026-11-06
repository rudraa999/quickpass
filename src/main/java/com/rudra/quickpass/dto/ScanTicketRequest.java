package com.rudra.quickpass.dto;

import jakarta.validation.constraints.NotBlank;

public class ScanTicketRequest {

    @NotBlank(message = "Ticket code or QR payload is required")
    private String ticketCode;

    public ScanTicketRequest() {
    }

    public ScanTicketRequest(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }
}

