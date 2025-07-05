package com.example.demo.repository;

import com.example.demo.entity.FranchiseApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseApplicationRepository extends JpaRepository<FranchiseApplication, Integer> {
    
    // 根据申请人手机号查找申请
    Optional<FranchiseApplication> findByApplicantPhone(String applicantPhone);
    
    // 根据申请人身份证号查找申请
    Optional<FranchiseApplication> findByApplicantIdCard(String applicantIdCard);
    
    // 根据状态查找申请
    List<FranchiseApplication> findByStatus(FranchiseApplication.ApplicationStatus status);
    
    // 根据申请人姓名模糊查询
    List<FranchiseApplication> findByApplicantNameContaining(String applicantName);
    
    // 根据拟开店名称模糊查询
    List<FranchiseApplication> findByProposedStoreNameContaining(String proposedStoreName);
    
    // 根据拟开店地址模糊查询
    List<FranchiseApplication> findByProposedLocationContaining(String proposedLocation);
    
    // 查询指定状态的申请数量
    @Query("SELECT COUNT(fa) FROM FranchiseApplication fa WHERE fa.status = :status")
    long countByStatus(@Param("status") FranchiseApplication.ApplicationStatus status);
    
    // 根据投资金额范围查询
    @Query("SELECT fa FROM FranchiseApplication fa WHERE fa.investmentAmount BETWEEN :minAmount AND :maxAmount")
    List<FranchiseApplication> findByInvestmentAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, 
                                                           @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    // 根据经纬度范围查询附近的申请
    @Query("SELECT fa FROM FranchiseApplication fa WHERE fa.latitude BETWEEN :minLat AND :maxLat AND fa.longitude BETWEEN :minLng AND :maxLng")
    List<FranchiseApplication> findByLocationRange(@Param("minLat") Double minLat, 
                                                  @Param("maxLat") Double maxLat,
                                                  @Param("minLng") Double minLng, 
                                                  @Param("maxLng") Double maxLng);
    
    // 查询所有申请，按创建时间倒序排列
    List<FranchiseApplication> findAllByOrderByCreatedAtDesc();
    
    // 查询指定状态的申请，按创建时间倒序排列
    List<FranchiseApplication> findByStatusOrderByCreatedAtDesc(FranchiseApplication.ApplicationStatus status);
}