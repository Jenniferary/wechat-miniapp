package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "onboarding_requests")
public class OnboardingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    private Integer applicantId;  // 关联 applicants 表的外键

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    private Position position;

    @Column(columnDefinition = "TEXT")
    private String resume;

    private Integer appliedBranchId;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.已提交待审批;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Gender { 男, 女 }
    public enum Position { 服务员, 前台, 后厨 }
    public enum ApprovalStatus { 已提交待审批, HR审批通过待店长审批, 店长审批通过已正式入职, 已驳回 }
}
