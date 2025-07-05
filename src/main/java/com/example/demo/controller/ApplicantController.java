package com.example.demo.controller;

import com.example.demo.entity.Applicant;
import com.example.demo.entity.OnboardingRequest;
import com.example.demo.repository.ApplicantRepository;
import com.example.demo.repository.OnboardingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private OnboardingRequestRepository onboardingRequestRepository; // 用于获取员工的入职申请

    // BCrypt加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Applicant applicant) {
        // 检查用户名是否已存在
        if (applicantRepository.findByUsername(applicant.getUsername()).isPresent()) {
            return Map.of("status", "error", "message", "用户名已存在");
        }
        // 检查邮箱是否已被注册
        if (applicantRepository.findByEmail(applicant.getEmail()).isPresent()) {
            return Map.of("status", "error", "message", "邮箱已被注册");
        }

        // 密码明文加密
        String encodedPassword = passwordEncoder.encode(applicant.getPasswordHash());
        applicant.setPasswordHash(encodedPassword);

        applicantRepository.save(applicant);

        return Map.of("status", "success", "message", "注册成功");
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<Applicant> applicantOpt = applicantRepository.findByUsername(username);
        if (applicantOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        Applicant applicant = applicantOpt.get();

        // 用 BCrypt 校验密码
        if (!passwordEncoder.matches(password, applicant.getPasswordHash())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        // 登录成功，返回用户信息（不返回密码）
        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", applicant.getId(),
                        "username", applicant.getUsername(),
                        "email", applicant.getEmail(),
                        "phone", applicant.getPhone(),
                        "fullName", applicant.getFullName()
                )
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getApplicant(@PathVariable Integer id) {
        Optional<Applicant> applicantOpt = applicantRepository.findById(id);
        if (applicantOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户不存在");
        }

        Applicant applicant = applicantOpt.get();

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", applicant.getId(),
                        "username", applicant.getUsername(),
                        "email", applicant.getEmail(),
                        "phone", applicant.getPhone(),
                        "fullName", applicant.getFullName()
                )
        );
    }

    // 获取员工的入职申请进度
    @GetMapping("/progress/{applicantId}")
    public Map<String, Object> getProgress(@PathVariable Integer applicantId) {
        List<OnboardingRequest> requests = onboardingRequestRepository.findByApplicantId(applicantId);

        if (requests.isEmpty()) {
            return Map.of("status", "error", "message", "未找到该申请人的入职申请记录");
        }

        // 根据申请状态，返回进度
        return Map.of(
                "status", "success",
                "data", requests.stream().map(req -> {
                    int progress = getProgressByStatus(req.getStatus());  // 使用枚举类型
                    return Map.of(
                            "requestId", req.getRequestId(),
                            "position", req.getPosition(),
                            "status", req.getStatus(),
                            "progress", progress,
                            "branch", req.getAppliedBranchId(),
                            "createdAt", req.getCreatedAt()
                    );
                }).toList()
        );
    }

    // 根据状态计算进度
    private int getProgressByStatus(OnboardingRequest.ApprovalStatus status) {
        switch (status) {
            case 已提交待审批: return 25;   // 提交后等待审批
            case HR审批通过待店长审批: return 50; // HR审批通过，待店长审批
            case 店长审批通过已正式入职: return 100; // 已入职
            case 已驳回: return 0;  // 被驳回
            default: return 0; // 默认进度
        }
    }
}
