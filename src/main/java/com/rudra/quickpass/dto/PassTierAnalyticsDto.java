package com.rudra.quickpass.dto;

import com.rudra.quickpass.entity.PassType;
import java.math.BigDecimal;

public class PassTierAnalyticsDto {
    private PassType passType;
    private BigDecimal price;
    private int capacity;
    private int sold;
    private int available;
    private BigDecimal revenue;
    private int scanned;
    private double soldPercentage;
    private double checkInPercentage;
    private String inventoryStatus; // "IN_STOCK", "SELLING_FAST", "SOLD_OUT"

    public PassTierAnalyticsDto() {
    }

    public PassTierAnalyticsDto(PassType passType, BigDecimal price, int capacity, int sold, int available,
                                BigDecimal revenue, int scanned) {
        this.passType = passType;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.capacity = capacity;
        this.sold = sold;
        this.available = available;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.scanned = scanned;
        this.soldPercentage = capacity > 0 ? ((double) sold / capacity) * 100.0 : 0.0;
        this.checkInPercentage = sold > 0 ? ((double) scanned / sold) * 100.0 : 0.0;

        if (available <= 0) {
            this.inventoryStatus = "SOLD_OUT";
        } else if (capacity > 0 && ((double) available / capacity) <= 0.20) {
            this.inventoryStatus = "SELLING_FAST";
        } else {
            this.inventoryStatus = "IN_STOCK";
        }
    }

    public PassType getPassType() {
        return passType;
    }

    public void setPassType(PassType passType) {
        this.passType = passType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public int getScanned() {
        return scanned;
    }

    public void setScanned(int scanned) {
        this.scanned = scanned;
    }

    public double getSoldPercentage() {
        return soldPercentage;
    }

    public void setSoldPercentage(double soldPercentage) {
        this.soldPercentage = soldPercentage;
    }

    public double getCheckInPercentage() {
        return checkInPercentage;
    }

    public void setCheckInPercentage(double checkInPercentage) {
        this.checkInPercentage = checkInPercentage;
    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(String inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }
}

