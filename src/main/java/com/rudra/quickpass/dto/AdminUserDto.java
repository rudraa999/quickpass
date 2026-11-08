package com.rudra.quickpass.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rudra.quickpass.entity.Role;
import java.time.LocalDateTime;

public class AdminUserDto {
    private Long id;
    private String fullName;
    private String email;
    private Role role;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private long eventsCount;
    private long bookingsCount;
    private boolean isRootAdmin;

    public AdminUserDto() {
    }

    public AdminUserDto(Long id, String fullName, String email, Role role, LocalDateTime createdAt,
            long eventsCount, long bookingsCount, boolean isRootAdmin) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.eventsCount = eventsCount;
        this.bookingsCount = bookingsCount;
        this.isRootAdmin = isRootAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(long eventsCount) {
        this.eventsCount = eventsCount;
    }

    public long getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(long bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public boolean isRootAdmin() {
        return isRootAdmin;
    }

    public void setRootAdmin(boolean rootAdmin) {
        isRootAdmin = rootAdmin;
    }
}
