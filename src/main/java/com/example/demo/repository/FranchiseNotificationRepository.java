package com.example.demo.repository;

import com.example.demo.entity.FranchiseNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseNotificationRepository extends JpaRepository<FranchiseNotification, Integer> {
    
    // 根据申请ID查找通知记录
    List<FranchiseNotification> findByApplicationIdOrderBySentAtDesc(Integer applicationId);
    
    // 根据申请人手机号查找通知记录
    List<FranchiseNotification> findByApplicantPhoneOrderBySentAtDesc(String applicantPhone);
    
    // 根据通知类型查找通知记录
    List<FranchiseNotification> findByNotificationTypeOrderBySentAtDesc(String notificationType);
    
    // 根据发送状态查找通知记录
    List<FranchiseNotification> findByStatusOrderBySentAtDesc(FranchiseNotification.NotificationStatus status);
    
    // 统计某个申请的通知数量
    long countByApplicationId(Integer applicationId);
    
    // 统计某个手机号的通知数量
    long countByApplicantPhone(String applicantPhone);
}