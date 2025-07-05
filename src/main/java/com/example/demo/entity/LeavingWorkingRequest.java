package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaving_working_requests") // 指定数据库表名
public class LeavingWorkingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键生成策略
    private Integer id;

    @Column(name = "employee_id") // 映射到数据库字段
    private Integer employeeId;

    @Column(name = "employee_type")
    private String employeeType;

    @Column(name = "branch_id")
    private Integer branchId;

    private String reason;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private String name;  // 新增 name 字段
    private String username;  // 新增 username 字段

    // Getters and setters
    public String getUsername() {
        return username;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
