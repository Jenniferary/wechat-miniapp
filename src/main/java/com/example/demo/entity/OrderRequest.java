package com.example.demo.entity;

import java.util.List;

public class OrderRequest {
    private String username;
    private List<String> items;
    private double totalPrice;
    private double selectedCoupon;  // 新增：优惠券金额

    // Getters & Setters
    private String remark;
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getSelectedCoupon() { return selectedCoupon; }  // 新增：获取优惠券金额
    public void setSelectedCoupon(double selectedCoupon) { this.selectedCoupon = selectedCoupon; }  // 新增：设置优惠券金额
}
