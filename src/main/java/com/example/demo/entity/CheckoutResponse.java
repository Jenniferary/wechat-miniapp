package com.example.demo.entity;

public class CheckoutResponse {
    private int totalPrice;

    public CheckoutResponse(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}