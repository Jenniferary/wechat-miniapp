package com.example.demo.entity;

import java.util.List;
import java.util.Map;

public class OrderResponse {
    private boolean success;
    private int userId;
    private String tableNumber;
    private List<String> items;
    private double totalPrice;
    private List<Map<String, Object>> availableCoupons;

    public OrderResponse(boolean success, int userId, String tableNumber, List<String> items, double totalPrice, List<Map<String, Object>> availableCoupons) {
        this.success = success;
        this.userId = userId;
        this.tableNumber = tableNumber;
        this.items = items;
        this.totalPrice = totalPrice;
        this.availableCoupons = availableCoupons;
    }

    // Getters & Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public List<Map<String, Object>> getAvailableCoupons() { return availableCoupons; }
    public void setAvailableCoupons(List<Map<String, Object>> availableCoupons) { this.availableCoupons = availableCoupons; }
}
