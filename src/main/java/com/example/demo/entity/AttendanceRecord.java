package com.example.demo.entity;

import java.time.LocalDateTime;

public class AttendanceRecord {
    private Integer id;
    private Integer employeeId;
    private String employeeType;
    private Integer branchId;
    private LocalDateTime checkInTime;
    private Double latitude;
    private Double longitude;
    private String status; // e.g. "normal"

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
