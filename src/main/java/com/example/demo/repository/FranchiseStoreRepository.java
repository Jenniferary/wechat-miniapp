package com.example.demo.repository;

import com.example.demo.entity.FranchiseStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseStoreRepository extends JpaRepository<FranchiseStore, Integer> {
    
    // 根据申请ID查找店铺
    Optional<FranchiseStore> findByApplicationId(Integer applicationId);
    
    // 根据加盟商手机号查找店铺
    List<FranchiseStore> findByFranchiseePhone(String franchiseePhone);
    
    // 根据加盟商身份证号查找店铺
    List<FranchiseStore> findByFranchiseeIdCard(String franchiseeIdCard);
    
    // 根据店铺名称模糊查询
    List<FranchiseStore> findByStoreNameContaining(String storeName);
    
    // 根据店铺地址模糊查询
    List<FranchiseStore> findByStoreAddressContaining(String storeAddress);
    
    // 根据状态查找店铺
    List<FranchiseStore> findByStatus(FranchiseStore.StoreStatus status);
    
    // 根据加盟商姓名模糊查询
    List<FranchiseStore> findByFranchiseeNameContaining(String franchiseeName);
    
    // 查询指定状态的店铺数量
    @Query("SELECT COUNT(fs) FROM FranchiseStore fs WHERE fs.status = :status")
    long countByStatus(@Param("status") FranchiseStore.StoreStatus status);
    
    // 根据经纬度范围查询附近的店铺
    @Query("SELECT fs FROM FranchiseStore fs WHERE fs.latitude BETWEEN :minLat AND :maxLat AND fs.longitude BETWEEN :minLng AND :maxLng")
    List<FranchiseStore> findByLocationRange(@Param("minLat") Double minLat, 
                                           @Param("maxLat") Double maxLat,
                                           @Param("minLng") Double minLng, 
                                           @Param("maxLng") Double maxLng);
    
    // 根据投资金额范围查询
    @Query("SELECT fs FROM FranchiseStore fs WHERE fs.investmentAmount BETWEEN :minAmount AND :maxAmount")
    List<FranchiseStore> findByInvestmentAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, 
                                                      @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    // 根据月特许费范围查询
    @Query("SELECT fs FROM FranchiseStore fs WHERE fs.monthlyRoyalty BETWEEN :minRoyalty AND :maxRoyalty")
    List<FranchiseStore> findByMonthlyRoyaltyBetween(@Param("minRoyalty") java.math.BigDecimal minRoyalty, 
                                                   @Param("maxRoyalty") java.math.BigDecimal maxRoyalty);
    
    // 查询所有店铺，按创建时间倒序排列
    List<FranchiseStore> findAllByOrderByCreatedAtDesc();
    
    // 查询指定状态的店铺，按创建时间倒序排列
    List<FranchiseStore> findByStatusOrderByCreatedAtDesc(FranchiseStore.StoreStatus status);
    
    // 查询正在营业的店铺（正式营业状态）
    @Query("SELECT fs FROM FranchiseStore fs WHERE fs.status = 'OPERATING'")
    List<FranchiseStore> findOperatingStores();
    
    // 根据店铺管理员手机号查找店铺
    List<FranchiseStore> findByManagerPhone(String managerPhone);
    
    // 查询员工数量在指定范围内的店铺
    @Query("SELECT fs FROM FranchiseStore fs WHERE fs.employeeCount BETWEEN :minCount AND :maxCount")
    List<FranchiseStore> findByEmployeeCountBetween(@Param("minCount") Integer minCount, 
                                                   @Param("maxCount") Integer maxCount);
    
    // 查询座位数在指定范围内的店铺
    @Query("SELECT fs FROM FranchiseStore fs WHERE fs.seatingCapacity BETWEEN :minCapacity AND :maxCapacity")
    List<FranchiseStore> findBySeatingCapacityBetween(@Param("minCapacity") Integer minCapacity, 
                                                     @Param("maxCapacity") Integer maxCapacity);
}