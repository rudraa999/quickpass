package com.rudra.quickpass.dto;

import java.math.BigDecimal;

public class TicketTierDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private int totalCapacity;
    private int availableCapacity;
    private int soldCount;

    public TicketTierDto() {
    }

    public TicketTierDto(Long id, String name, BigDecimal price, int totalCapacity, int availableCapacity, int soldCount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.totalCapacity = totalCapacity;
        this.availableCapacity = availableCapacity;
        this.soldCount = soldCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
}

