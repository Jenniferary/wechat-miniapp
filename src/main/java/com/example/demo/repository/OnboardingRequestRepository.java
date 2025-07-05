package com.example.demo.repository;

import com.example.demo.entity.OnboardingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnboardingRequestRepository extends JpaRepository<OnboardingRequest, Integer> {

    // 根据申请人的ID查询所有的入职申请记录
    List<OnboardingRequest> findByApplicantId(Integer applicantId);

    // 根据门店ID和状态查询待审批的入职申请
    List<OnboardingRequest> findByAppliedBranchIdAndStatusIn(Integer appliedBranchId, List<String> status);

    // 根据状态查询所有的入职申请
    List<OnboardingRequest> findByStatusIn(List<String> status);
}
