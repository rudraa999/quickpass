package com.rudra.quickpass.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rudra.quickpass.entity.Role;
import java.time.LocalDateTime;

public class StaffMemberDto {
    private Long id;
    private Long staffUserId;
    private String fullName;
    private String email;
    private Role role;
    private String gateRole;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime assignedAt;

    public StaffMemberDto() {
    }

    public StaffMemberDto(Long id, Long staffUserId, String fullName, String email, Role role, String gateRole, LocalDateTime assignedAt) {
        this.id = id;
        this.staffUserId = staffUserId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.gateRole = gateRole;
        this.assignedAt = assignedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStaffUserId() {
        return staffUserId;
    }

    public void setStaffUserId(Long staffUserId) {
        this.staffUserId = staffUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getGateRole() {
        return gateRole;
    }

    public void setGateRole(String gateRole) {
        this.gateRole = gateRole;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}

