package com.example.demo.controller;

import com.example.demo.entity.OnboardingRequest;
import com.example.demo.entity.Waiter;
import com.example.demo.entity.Chef;
import com.example.demo.repository.WaiterRepository;
import com.example.demo.repository.ChefRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 店长审批通过
    @PutMapping("/{id}/approve")
    @Transactional
    public Map<String, String> approveRequest(@PathVariable int id) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req == null) {
            return Map.of("status", "error", "message", "申请不存在");
        }

        // 确保当前状态为 "HR审批通过待店长审批"
        if (!req.getStatus().equals(OnboardingRequest.ApprovalStatus.HR审批通过待店长审批)) {
            return Map.of("status", "error", "message", "只能对待店长审批的申请进行审批");
        }

        // 更新状态为 "店长审批通过已正式入职"
        req.setStatus(OnboardingRequest.ApprovalStatus.店长审批通过已正式入职);
        em.merge(req); // 保存更新

        // 根据 position 字段判断是服务员还是后厨
        if (req.getPosition() == OnboardingRequest.Position.服务员) {
            // 创建 Waiter 实体并保存
            waiterRepository.addWaiter(
                    req.getName(),
                    "defaultPassword",  // 默认密码
                    req.getName(),  // 使用申请人的名字作为姓名
                    req.getPhone(),
                    req.getEmail(),
                    req.getAppliedBranchId()
            );
        } else if (req.getPosition() == OnboardingRequest.Position.后厨) {
            // 创建 Chef 实体并保存
            chefRepository.addChef(
                    req.getName(),
                    "defaultPassword",  // 默认密码
                    req.getName(),  // 使用申请人的名字作为姓名
                    req.getPhone(),
                    req.getEmail(),
                    req.getAppliedBranchId()
            );
        }

        return Map.of("status", "success", "message", "店长审批通过，员工已添加");
    }

    // 店长驳回
    @PutMapping("/{id}/reject")
    @Transactional
    public Map<String, String> rejectRequest(@PathVariable int id) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req == null) {
            return Map.of("status", "error", "message", "申请不存在");
        }

        // 确保当前状态为 "HR审批通过待店长审批"
        if (!req.getStatus().equals(OnboardingRequest.ApprovalStatus.HR审批通过待店长审批)) {
            return Map.of("status", "error", "message", "只能对待店长审批的申请进行驳回");
        }

        // 更新状态为 "已驳回"
        req.setStatus(OnboardingRequest.ApprovalStatus.已驳回);
        em.merge(req); // 保存更新
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
