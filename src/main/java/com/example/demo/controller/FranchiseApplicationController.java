package com.example.demo.controller;

import com.example.demo.dto.FranchiseApplicationDTO;
import com.example.demo.entity.FranchiseApplication;
import com.example.demo.entity.FranchiseNotification;
import com.example.demo.repository.FranchiseApplicationRepository;
import com.example.demo.repository.FranchiseNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/franchise/applications")
@CrossOrigin(originPatterns = "http://localhost:*")
public class FranchiseApplicationController {
    
    @Autowired
    private FranchiseApplicationRepository franchiseApplicationRepository;
    
    @Autowired
    private FranchiseNotificationRepository franchiseNotificationRepository;
    
    // 提交加盟申请
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitApplication(@RequestBody FranchiseApplicationDTO applicationDTO) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 转换 DTO 为实体
            FranchiseApplication application = applicationDTO.toEntity();
            
            // 检查手机号是否已存在
            Optional<FranchiseApplication> existingByPhone = franchiseApplicationRepository.findByApplicantPhone(application.getApplicantPhone());
            if (existingByPhone.isPresent()) {
                response.put("success", false);
                response.put("message", "该手机号已提交过申请");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查身份证号是否已存在
            Optional<FranchiseApplication> existingByIdCard = franchiseApplicationRepository.findByApplicantIdCard(application.getApplicantIdCard());
            if (existingByIdCard.isPresent()) {
                response.put("success", false);
                response.put("message", "该身份证号已提交过申请");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置默认状态（时间由 @PrePersist 自动设置）
            application.setStatus(FranchiseApplication.ApplicationStatus.待审核);
            
            // 确保投资金额正确转换
            if (application.getInvestmentAmount() == null) {
                response.put("success", false);
                response.put("message", "投资金额不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            FranchiseApplication savedApplication = franchiseApplicationRepository.save(application);
            
            response.put("success", true);
            response.put("message", "申请提交成功");
            response.put("data", savedApplication);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "申请提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取所有申请
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllApplications() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseApplication> applications = franchiseApplicationRepository.findAllByOrderByCreatedAtDesc();
            response.put("success", true);
            response.put("data", applications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取申请列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据状态获取申请
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getApplicationsByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            FranchiseApplication.ApplicationStatus applicationStatus = FranchiseApplication.ApplicationStatus.valueOf(status);
            List<FranchiseApplication> applications = franchiseApplicationRepository.findByStatusOrderByCreatedAtDesc(applicationStatus);
            response.put("success", true);
            response.put("data", applications);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "无效的状态参数");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取申请列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据ID获取申请详情
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseApplication> application = franchiseApplicationRepository.findById(id);
            if (application.isPresent()) {
                response.put("success", true);
                response.put("data", application.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取申请详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 审核申请
    @PutMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewApplication(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> reviewData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseApplication> optionalApplication = franchiseApplicationRepository.findById(id);
            if (!optionalApplication.isPresent()) {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
            
            FranchiseApplication application = optionalApplication.get();
            
            // 更新审核信息
            String statusStr = (String) reviewData.get("status");
            String reviewComments = (String) reviewData.get("reviewComments");
            String reviewedBy = (String) reviewData.get("reviewedBy");
            
            application.setStatus(FranchiseApplication.ApplicationStatus.valueOf(statusStr));
            application.setReviewComments(reviewComments);
            application.setReviewedBy(reviewedBy);
            application.setReviewedAt(LocalDateTime.now());
            application.setUpdatedAt(LocalDateTime.now());
            
            FranchiseApplication updatedApplication = franchiseApplicationRepository.save(application);
            
            response.put("success", true);
            response.put("message", "审核完成");
            response.put("data", updatedApplication);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "无效的状态参数");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "审核失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据手机号查询申请
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getApplicationByPhone(@PathVariable String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseApplication> application = franchiseApplicationRepository.findByApplicantPhone(phone);
            if (application.isPresent()) {
                response.put("success", true);
                // 返回数组格式以匹配前端期望
                response.put("data", List.of(application.get()));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", true);
                response.put("data", List.of()); // 返回空数组而不是404
                response.put("message", "未找到相关申请");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 搜索申请（根据申请人姓名或店铺名称）
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchApplications(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseApplication> applicationsByName = franchiseApplicationRepository.findByApplicantNameContaining(keyword);
            List<FranchiseApplication> applicationsByStoreName = franchiseApplicationRepository.findByProposedStoreNameContaining(keyword);
            
            // 合并结果并去重
            applicationsByName.addAll(applicationsByStoreName);
            List<FranchiseApplication> uniqueApplications = applicationsByName.stream().distinct().toList();
            
            response.put("success", true);
            response.put("data", uniqueApplications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取申请统计信息
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getApplicationStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Long> statistics = new HashMap<>();
            statistics.put("待审核", franchiseApplicationRepository.countByStatus(FranchiseApplication.ApplicationStatus.待审核));
            statistics.put("审核中", franchiseApplicationRepository.countByStatus(FranchiseApplication.ApplicationStatus.审核中));
            statistics.put("审核通过", franchiseApplicationRepository.countByStatus(FranchiseApplication.ApplicationStatus.审核通过));
            statistics.put("审核拒绝", franchiseApplicationRepository.countByStatus(FranchiseApplication.ApplicationStatus.审核拒绝));
            statistics.put("已退回", franchiseApplicationRepository.countByStatus(FranchiseApplication.ApplicationStatus.已退回));
            statistics.put("已开业", franchiseApplicationRepository.countByStatus(FranchiseApplication.ApplicationStatus.已开业));
            statistics.put("总计", franchiseApplicationRepository.count());
            
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 更新申请状态
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> statusData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseApplication> optionalApplication = franchiseApplicationRepository.findById(id);
            if (!optionalApplication.isPresent()) {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
            
            FranchiseApplication application = optionalApplication.get();
            
            // 更新状态
            String statusStr = (String) statusData.get("status");
            application.setStatus(FranchiseApplication.ApplicationStatus.valueOf(statusStr));
            application.setUpdatedAt(LocalDateTime.now());
            
            FranchiseApplication updatedApplication = franchiseApplicationRepository.save(application);
            
            response.put("success", true);
            response.put("message", "状态更新成功");
            response.put("data", updatedApplication);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "无效的状态参数");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "状态更新失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 发送通知
    @PostMapping("/{id}/notify")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> notificationData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseApplication> optionalApplication = franchiseApplicationRepository.findById(id);
            if (!optionalApplication.isPresent()) {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
            
            FranchiseApplication application = optionalApplication.get();
            String notificationType = (String) notificationData.get("type");
            String content = (String) notificationData.get("content");
            
            // 创建通知记录
            FranchiseNotification notification = new FranchiseNotification(
                application.getId(),
                application.getApplicantName(),
                application.getApplicantPhone(),
                notificationType,
                content,
                "系统管理员" // 可以从认证信息中获取当前用户
            );
            
            // 保存通知记录
            franchiseNotificationRepository.save(notification);
            
            // 这里可以集成短信或邮件服务
            // 目前只是模拟发送通知
            System.out.println("发送通知给: " + application.getApplicantName());
            System.out.println("手机号: " + application.getApplicantPhone());
            System.out.println("通知类型: " + notificationType);
            System.out.println("通知内容: " + content);
            
            response.put("success", true);
            response.put("message", "通知发送成功");
            response.put("data", notification);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "通知发送失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 删除申请
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteApplication(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (franchiseApplicationRepository.existsById(id)) {
                franchiseApplicationRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "申请删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取申请的通知记录
    @GetMapping("/{id}/notifications")
    public ResponseEntity<Map<String, Object>> getApplicationNotifications(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查申请是否存在
            if (!franchiseApplicationRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
            
            List<FranchiseNotification> notifications = franchiseNotificationRepository.findByApplicationIdOrderBySentAtDesc(id);
            response.put("success", true);
            response.put("data", notifications);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知记录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据手机号获取通知记录
    @GetMapping("/notifications/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getNotificationsByPhone(@PathVariable String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseNotification> notifications = franchiseNotificationRepository.findByApplicantPhoneOrderBySentAtDesc(phone);
            response.put("success", true);
            response.put("data", notifications);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知记录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}