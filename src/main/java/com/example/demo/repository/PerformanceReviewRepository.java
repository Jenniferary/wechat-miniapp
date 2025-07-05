package com.example.demo.repository;

import com.example.demo.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PerformanceReviewRepository
        extends JpaRepository<PerformanceReview, Integer> {

    /** 按员工 + 时间段查询 */
    List<PerformanceReview> findByEmployeeIdAndEmployeeTypeAndReviewPeriodStartGreaterThanEqualAndReviewPeriodEndLessThanEqual(
            Integer employeeId, String employeeType, LocalDate from, LocalDate to);
    List<PerformanceReview> findByBranchId(Integer branchId);
    List<PerformanceReview> findByEmployeeType(String employeeType);
    List<PerformanceReview> findByBranchIdAndEmployeeType(Integer branchId, String employeeType);
    List<PerformanceReview> findByBranchIdOrderByReviewPeriodStartDesc(Integer branchId);


}
