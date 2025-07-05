package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "franchise_stores")
public class FranchiseStore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "application_id", nullable = false)
    private Integer applicationId;
    
    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;
    
    @Column(name = "store_address", nullable = false, length = 500)
    private String storeAddress;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "store_area", precision = 8, scale = 2)
    private BigDecimal storeArea;
    
    @Column(name = "franchisee_name", nullable = false, length = 100)
    private String franchiseeName;
    
    @Column(name = "franchisee_phone", nullable = false, length = 20)
    private String franchiseePhone;
    
    @Column(name = "franchisee_email", length = 100)
    private String franchiseeEmail;
    
    @Column(name = "franchisee_id_card", nullable = false, length = 18)
    private String franchiseeIdCard;
    
    @Column(name = "investment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal investmentAmount;
    
    @Column(name = "franchise_fee", precision = 10, scale = 2)
    private BigDecimal franchiseFee;
    
    @Column(name = "monthly_royalty", precision = 8, scale = 2)
    private BigDecimal monthlyRoyalty;
    
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;
    
    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;
    
    @Column(name = "opening_date")
    private LocalDate openingDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StoreStatus status = StoreStatus.筹备中;
    
    @Column(name = "business_license", length = 100)
    private String businessLicense;
    
    @Column(name = "food_permit", length = 100)
    private String foodPermit;
    
    @Column(name = "manager_name", length = 100)
    private String managerName;
    
    @Column(name = "manager_phone", length = 20)
    private String managerPhone;
    
    @Column(name = "employee_count")
    private Integer employeeCount;
    
    @Column(name = "seating_capacity")
    private Integer seatingCapacity;
    
    @Column(name = "operating_hours", length = 100)
    private String operatingHours;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum StoreStatus {
        筹备中, 装修中, 试营业, 正式营业, 暂停营业, 已关闭
    }
    
    // 构造函数
    public FranchiseStore() {}
    
    // Getter 和 Setter 方法
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
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public String getStoreAddress() {
        return storeAddress;
    }
    
    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
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
    
    public String getFranchiseeName() {
        return franchiseeName;
    }
    
    public void setFranchiseeName(String franchiseeName) {
        this.franchiseeName = franchiseeName;
    }
    
    public String getFranchiseePhone() {
        return franchiseePhone;
    }
    
    public void setFranchiseePhone(String franchiseePhone) {
        this.franchiseePhone = franchiseePhone;
    }
    
    public String getFranchiseeEmail() {
        return franchiseeEmail;
    }
    
    public void setFranchiseeEmail(String franchiseeEmail) {
        this.franchiseeEmail = franchiseeEmail;
    }
    
    public String getFranchiseeIdCard() {
        return franchiseeIdCard;
    }
    
    public void setFranchiseeIdCard(String franchiseeIdCard) {
        this.franchiseeIdCard = franchiseeIdCard;
    }
    
    public BigDecimal getInvestmentAmount() {
        return investmentAmount;
    }
    
    public void setInvestmentAmount(BigDecimal investmentAmount) {
        this.investmentAmount = investmentAmount;
    }
    
    public BigDecimal getFranchiseFee() {
        return franchiseFee;
    }
    
    public void setFranchiseFee(BigDecimal franchiseFee) {
        this.franchiseFee = franchiseFee;
    }
    
    public BigDecimal getMonthlyRoyalty() {
        return monthlyRoyalty;
    }
    
    public void setMonthlyRoyalty(BigDecimal monthlyRoyalty) {
        this.monthlyRoyalty = monthlyRoyalty;
    }
    
    public LocalDate getContractStartDate() {
        return contractStartDate;
    }
    
    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }
    
    public LocalDate getContractEndDate() {
        return contractEndDate;
    }
    
    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }
    
    public LocalDate getOpeningDate() {
        return openingDate;
    }
    
    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }
    
    public StoreStatus getStatus() {
        return status;
    }
    
    public void setStatus(StoreStatus status) {
        this.status = status;
    }
    
    public String getBusinessLicense() {
        return businessLicense;
    }
    
    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
    
    public String getFoodPermit() {
        return foodPermit;
    }
    
    public void setFoodPermit(String foodPermit) {
        this.foodPermit = foodPermit;
    }
    
    public String getManagerName() {
        return managerName;
    }
    
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
    
    public String getManagerPhone() {
        return managerPhone;
    }
    
    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }
    
    public Integer getEmployeeCount() {
        return employeeCount;
    }
    
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }
    
    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
    
    public String getOperatingHours() {
        return operatingHours;
    }
    
    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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