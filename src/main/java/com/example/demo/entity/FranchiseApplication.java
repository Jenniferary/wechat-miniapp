package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "franchise_applications")
public class FranchiseApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;
    
    @Column(name = "applicant_phone", nullable = false, length = 20, unique = true)
    private String applicantPhone;
    
    @Column(name = "applicant_email", length = 100)
    private String applicantEmail;
    
    @Column(name = "applicant_id_card", nullable = false, length = 18, unique = true)
    private String applicantIdCard;
    
    @Column(name = "business_experience", columnDefinition = "TEXT")
    private String businessExperience;
    
    @Column(name = "investment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal investmentAmount;
    
    @Column(name = "proposed_location", nullable = false, length = 500)
    private String proposedLocation;
    
    @Column(name = "proposed_store_name", nullable = false, length = 255)
    private String proposedStoreName;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "store_area", precision = 8, scale = 2)
    private BigDecimal storeArea;
    
    @Column(name = "expected_opening_date")
    private LocalDate expectedOpeningDate;
    
    @Column(name = "application_reason", columnDefinition = "TEXT")
    private String applicationReason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status = ApplicationStatus.待审核;
    
    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;
    
    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ApplicationStatus {
        待审核, 审核中, 审核通过, 审核拒绝, 已退回, 已开业
    }
    
    // 构造函数
    public FranchiseApplication() {}
    
    // Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
    
    public String getApplicantEmail() {
        return applicantEmail;
    }
    
    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }
    
    public String getApplicantIdCard() {
        return applicantIdCard;
    }
    
    public void setApplicantIdCard(String applicantIdCard) {
        this.applicantIdCard = applicantIdCard;
    }
    
    public String getBusinessExperience() {
        return businessExperience;
    }
    
    public void setBusinessExperience(String businessExperience) {
        this.businessExperience = businessExperience;
    }
    
    public BigDecimal getInvestmentAmount() {
        return investmentAmount;
    }
    
    public void setInvestmentAmount(BigDecimal investmentAmount) {
        this.investmentAmount = investmentAmount;
    }
    
    public String getProposedLocation() {
        return proposedLocation;
    }
    
    public void setProposedLocation(String proposedLocation) {
        this.proposedLocation = proposedLocation;
    }
    
    public String getProposedStoreName() {
        return proposedStoreName;
    }
    
    public void setProposedStoreName(String proposedStoreName) {
        this.proposedStoreName = proposedStoreName;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public BigDecimal getStoreArea() {
        return storeArea;
    }
    
    public void setStoreArea(BigDecimal storeArea) {
        this.storeArea = storeArea;
    }
    
    public LocalDate getExpectedOpeningDate() {
        return expectedOpeningDate;
    }
    
    public void setExpectedOpeningDate(LocalDate expectedOpeningDate) {
        this.expectedOpeningDate = expectedOpeningDate;
    }
    
    public String getApplicationReason() {
        return applicationReason;
    }
    
    public void setApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public String getReviewComments() {
        return reviewComments;
    }
    
    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }
    
    public String getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}