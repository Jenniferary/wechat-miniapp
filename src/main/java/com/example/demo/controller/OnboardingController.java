package com.example.demo.controller;

import com.example.demo.entity.OnboardingRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin
public class OnboardingController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public List<OnboardingRequest> getAll() {
        return em.createQuery("FROM OnboardingRequest", OnboardingRequest.class).getResultList();
    }

    @GetMapping("/pending")
    public List<OnboardingRequest> getPendingRequests() {
        return em.createQuery(
                        "FROM OnboardingRequest WHERE status = '待审批' OR status = '审批中'", OnboardingRequest.class)
                .getResultList();
    }
    @GetMapping("/pending-by-branch")
    public List<OnboardingRequest> getPendingByBranch(@RequestParam Integer branchId) {
        return em.createQuery(
                        "FROM OnboardingRequest WHERE (status = '待审批' OR status = '审批中') AND appliedBranchId = :branchId",
                        OnboardingRequest.class)
                .setParameter("branchId", branchId)
                .getResultList();
    }

    @PostMapping
    @Transactional
    public Map<String, String> submit(@RequestBody OnboardingRequest req) {
        em.persist(req);
        return Map.of("status", "success", "message", "入职申请提交成功");
    }

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

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> delete(@PathVariable int id) {
        OnboardingRequest req = em.find(OnboardingRequest.class, id);
        if (req != null) em.remove(req);
        return Map.of("status", "success", "message", "删除成功");
    }

    @GetMapping("/by-applicant")
    public List<OnboardingRequest> getByApplicantId(@RequestParam Integer applicantId) {
        return em.createQuery(
                        "FROM OnboardingRequest WHERE applicantId = :applicantId", OnboardingRequest.class)
                .setParameter("applicantId", applicantId)
                .getResultList();
    }
}
