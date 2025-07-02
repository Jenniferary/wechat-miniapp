package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin
public class OnboardingController {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private WaiterRepository waiterRepository;

    @Autowired
    private ChefRepository chefRepository;

    @Autowired
    private CounterRepository counterRepository; // 前台

    @Autowired
    private HrManagerRepository hrManagerRepository; // HR

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 获取所有入职申请
    @GetMapping
    public List<OnboardingRequest> getAll() {
        return em.createQuery("FROM OnboardingRequest", OnboardingRequest.class).getResultList();
    }

    // 获取待审批的入职申请
    @GetMapping("/pending")
    public List<OnboardingRequest> getPendingRequests() {
        return em.createQuery(
                        "FROM OnboardingRequest WHERE status = '已提交待审批' OR status = 'HR审批通过待店长审批'", OnboardingRequest.class)
                .getResultList();
    }

    // 获取某个门店的待审批入职申请
    @GetMapping("/pending-by-branch")
    public List<OnboardingRequest> getPendingByBranch(@RequestParam Integer branchId) {
        return em.createQuery(
                        "FROM OnboardingRequest WHERE (status = '已提交待审批' OR status = 'HR审批通过待店长审批') AND appliedBranchId = :branchId",
                        OnboardingRequest.class)
                .setParameter("branchId", branchId)
                .getResultList();
    }

    // 提交入职申请
    @PostMapping
    @Transactional
    public Map<String, String> submit(@RequestBody OnboardingRequest req) {
        em.persist(req);
        return Map.of("status", "success", "message", "入职申请提交成功");
    }

    // 更新申请状态，HR审批部分
    @PutMapping("/{id}/status")
    @Transactional
    public Map<String, String> updateStatus(@PathVariable int id, @RequestParam OnboardingRequest.ApprovalStatus status) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req == null) {
            return Map.of("status", "error", "message", "申请不存在");
        }
        req.setStatus(status);
        return Map.of("status", "success", "message", "状态更新成功");
    }

    // 店长审批通过（仅修改状态）
    @PutMapping("/{id}/approve")
    @Transactional
    public Map<String, String> approveRequest(@PathVariable int id) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req == null) {
            return Map.of("status", "error", "message", "申请不存在");
        }
        if (!req.getStatus().equals(OnboardingRequest.ApprovalStatus.HR审批通过待店长审批)) {
            return Map.of("status", "error", "message", "只能审批待店长审批的申请");
        }

        req.setStatus(OnboardingRequest.ApprovalStatus.店长审批通过已正式入职);
        em.merge(req);

        return Map.of("status", "success", "message", "店长审批通过，等待员工确认入职");
    }

    // 员工确认入职，创建账户接口
    @PostMapping("/{id}/confirm")
    @Transactional
    public Map<String, String> confirmOnboarding(@PathVariable int id,
                                                 @RequestBody Map<String, String> confirmData) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req == null) {
            return Map.of("status", "error", "message", "申请不存在");
        }
        if (!req.getStatus().equals(OnboardingRequest.ApprovalStatus.店长审批通过已正式入职)) {
            return Map.of("status", "error", "message", "当前状态不允许确认入职");
        }

        String username = confirmData.get("username");
        String rawPassword = confirmData.get("password");
        if (username == null || username.isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            return Map.of("status", "error", "message", "用户名或密码不能为空");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        switch (req.getPosition()) {
            case 服务员 -> {
                Waiter waiter = new Waiter();
                waiter.setUsername(username);
                waiter.setPasswordHash(encodedPassword);
                waiter.setName(req.getName());
                waiter.setPhone(req.getPhone());
                waiter.setEmail(req.getEmail());
                waiter.setBranchId(req.getAppliedBranchId());
                waiter.setHireDate(LocalDate.now().atStartOfDay());
                waiterRepository.save(waiter);
            }
            case 前台 -> {
                Counter counter = new Counter();
                counter.setUsername(username);
                counter.setPasswordHash(encodedPassword);
                counter.setName(req.getName());
                counter.setPhone(req.getPhone());
                counter.setEmail(req.getEmail());
                counter.setBranchId(req.getAppliedBranchId());
                counter.setHireDate(LocalDate.now().atStartOfDay());
                counterRepository.save(counter);
            }
            case 后厨 -> {
                Chef chef = new Chef();
                chef.setUsername(username);
                chef.setPasswordHash(encodedPassword);
                chef.setName(req.getName());
                chef.setPhone(req.getPhone());
                chef.setEmail(req.getEmail());
                chef.setBranchId(req.getAppliedBranchId());
                chef.setHireDate(LocalDate.now().atStartOfDay());
                chefRepository.save(chef);
            }
            case HR -> {
                HrManager hr = new HrManager();
                hr.setUsername(username);
                hr.setPasswordHash(encodedPassword);
                hr.setName(req.getName());
                hr.setPhone(req.getPhone());
                hr.setEmail(req.getEmail());
                hr.setBranchId(req.getAppliedBranchId());
                hr.setHireDate(LocalDate.now().atStartOfDay());
                hrManagerRepository.save(hr);
            }
            default -> {
                return Map.of("status", "error", "message", "职位不支持");
            }
        }

        req.setStatus(OnboardingRequest.ApprovalStatus.员工已确认入职);
        em.merge(req);

        return Map.of("status", "success", "message", "员工已确认入职，账号创建成功");
    }

    // 店长驳回
    @PutMapping("/{id}/reject")
    @Transactional
    public Map<String, String> rejectRequest(@PathVariable int id) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req == null) {
            return Map.of("status", "error", "message", "申请不存在");
        }
        if (!req.getStatus().equals(OnboardingRequest.ApprovalStatus.HR审批通过待店长审批)) {
            return Map.of("status", "error", "message", "只能驳回待店长审批的申请");
        }
        req.setStatus(OnboardingRequest.ApprovalStatus.已驳回);
        em.merge(req);
        return Map.of("status", "success", "message", "申请已被驳回");
    }

    // 删除申请
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> delete(@PathVariable int id) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req != null) em.remove(req);
        return Map.of("status", "success", "message", "删除成功");
    }

    // 获取某个申请人的所有入职申请
    @GetMapping("/by-applicant")
    public List<OnboardingRequest> getByApplicantId(@RequestParam Integer applicantId) {
        return em.createQuery(
                        "FROM OnboardingRequest WHERE applicantId = :applicantId", OnboardingRequest.class)
                .setParameter("applicantId", applicantId)
                .getResultList();
    }
}
