package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "franchise_notifications")
public class FranchiseNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "application_id", nullable = false)
    private Integer applicationId;
    
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;
    
    @Column(name = "applicant_phone", nullable = false, length = 20)
    private String applicantPhone;
    
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "sent_by", length = 100)
    private String sentBy;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status = NotificationStatus.已发送;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum NotificationStatus {
        已发送, 发送失败, 已读, 未读
    }
    
    // 构造函数
    public FranchiseNotification() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.sentAt = LocalDateTime.now();
    }
    
    public FranchiseNotification(Integer applicationId, String applicantName, String applicantPhone, 
                               String notificationType, String content, String sentBy) {
        this();
        this.applicationId = applicationId;
        this.applicantName = applicantName;
        this.applicantPhone = applicantPhone;
        this.notificationType = notificationType;
        this.content = content;
        this.sentBy = sentBy;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }
    
    public String getApplicantName() {
        return applicantName;
    }
    
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }
    
    public String getApplicantPhone() {
        return applicantPhone;
    }
    
    public void setApplicantPhone(String applicantPhone) {
        this.applicantPhone = applicantPhone;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSentBy() {
        return sentBy;
    }
    
    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public void setStatus(NotificationStatus status) {
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
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}