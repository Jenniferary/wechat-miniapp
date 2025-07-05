package com.example.demo.entity;

public class DishTableEntity {
    private int reservationId;       // 对应数据库中的 id 字段
    private String tableNumber;      // 桌号
    private String reservationTime;  // 预定时间

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }
}
