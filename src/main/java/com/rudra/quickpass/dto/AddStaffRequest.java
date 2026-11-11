package com.rudra.quickpass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AddStaffRequest {

    @NotBlank(message = "Staff email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    private String fullName;

    public AddStaffRequest() {
    }

    public AddStaffRequest(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

