package com.example.demo.dto;

import com.example.demo.entity.FranchiseApplication;
import java.math.BigDecimal;
import java.time.LocalDate;

public class FranchiseApplicationDTO {
    private String applicantName;
    private String applicantPhone;
    private String applicantEmail;
    private String applicantIdCard;
    private String businessExperience;
    private Double investmentAmount; // 前端发送的是 number 类型
    private String proposedLocation;
    private String proposedStoreName;
    private String expectedOpeningDate; // 前端发送的是字符串
    private String applicationReason;

    // 构造函数
    public FranchiseApplicationDTO() {}

    // 转换为实体类
    public FranchiseApplication toEntity() {
        FranchiseApplication entity = new FranchiseApplication();
        entity.setApplicantName(this.applicantName);
        entity.setApplicantPhone(this.applicantPhone);
        entity.setApplicantEmail(this.applicantEmail);
        entity.setApplicantIdCard(this.applicantIdCard);
        entity.setBusinessExperience(this.businessExperience);
        
        // 转换投资金额
        if (this.investmentAmount != null) {
            entity.setInvestmentAmount(BigDecimal.valueOf(this.investmentAmount));
        }
        
        entity.setProposedLocation(this.proposedLocation);
        entity.setProposedStoreName(this.proposedStoreName);
        
        // 转换日期
        if (this.expectedOpeningDate != null && !this.expectedOpeningDate.isEmpty()) {
            entity.setExpectedOpeningDate(LocalDate.parse(this.expectedOpeningDate));
        }
        
        entity.setApplicationReason(this.applicationReason);
        
        return entity;
    }

    // Getter 和 Setter 方法
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

    public Double getInvestmentAmount() {
        return investmentAmount;
    }

    public void setInvestmentAmount(Double investmentAmount) {
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

    public String getExpectedOpeningDate() {
        return expectedOpeningDate;
    }

    public void setExpectedOpeningDate(String expectedOpeningDate) {
        this.expectedOpeningDate = expectedOpeningDate;
    }

    public String getApplicationReason() {
        return applicationReason;
    }

    public void setApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }
}