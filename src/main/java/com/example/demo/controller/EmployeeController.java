package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin
public class EmployeeController {

    @Autowired
    private WaiterRepository waiterRepository;

    @Autowired
    private ChefRepository chefRepository;

    @Autowired
    private HrManagerRepository hrManagerRepository;

    @Autowired
    private CounterRepository counterRepository;

    /**
     * 查询所有员工（支持参数 role、branchId）
     * - role = waiter / chef / hr / counter
     * - branchId 可选，限制返回指定门店的员工
     */
    @GetMapping
    public Map<String, Object> getEmployees(@RequestParam(required = false) String role,
                                            @RequestParam(required = false) Integer branchId) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (role == null || role.isEmpty()) {
            // 查询全部员工（按分店条件过滤）
            result.addAll(mapWaiters(branchId != null ? waiterRepository.findByBranchId(branchId) : waiterRepository.findAll()));
            result.addAll(mapChefs(branchId != null ? chefRepository.findByBranchId(branchId) : chefRepository.findAll()));
            result.addAll(mapCounters(branchId != null ? counterRepository.findByBranchId(branchId) : counterRepository.findAll()));
            result.addAll(mapHrs(branchId != null ? hrManagerRepository.findByBranchId(branchId) : hrManagerRepository.findAll()));
        } else {
            switch (role.toLowerCase()) {
                case "waiter":
                    result.addAll(mapWaiters(branchId != null ? waiterRepository.findByBranchId(branchId) : waiterRepository.findAll()));
                    break;
                case "chef":
                    result.addAll(mapChefs(branchId != null ? chefRepository.findByBranchId(branchId) : chefRepository.findAll()));
                    break;
                case "counter":
                    result.addAll(mapCounters(branchId != null ? counterRepository.findByBranchId(branchId) : counterRepository.findAll()));
                    break;
                case "hr":
                    result.addAll(mapHrs(branchId != null ? hrManagerRepository.findByBranchId(branchId) : hrManagerRepository.findAll()));
                    break;
                default:
                    return Map.of("status", "error", "message", "未知的角色类型：" + role);
            }
        }

        return Map.of("status", "success", "data", result);
    }

    private List<Map<String, Object>> mapWaiters(List<Waiter> list) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (Waiter w : list) {
            res.add(Map.of(
                    "id", w.getId(),
                    "name", w.getName(),
                    "username", w.getUsername(),
                    "phone", w.getPhone(),
                    "email", w.getEmail(),
                    "branchId", w.getBranchId(),
                    "role", "服务员"
            ));
        }
        return res;
    }

    private List<Map<String, Object>> mapChefs(List<Chef> list) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (Chef c : list) {
            res.add(Map.of(
                    "id", c.getId(),
                    "name", c.getName(),
                    "username", c.getUsername(),
                    "phone", c.getPhone(),
                    "email", c.getEmail(),
                    "branchId", c.getBranchId(),
                    "role", "厨师"
            ));
        }
        return res;
    }

    private List<Map<String, Object>> mapCounters(List<Counter> list) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (Counter c : list) {
            res.add(Map.of(
                    "id", c.getId(),
                    "name", c.getName(),
                    "username", c.getUsername(),
                    "phone", c.getPhone(),
                    "email", c.getEmail(),
                    "branchId", c.getBranchId(),
                    "role", "收银员"
            ));
        }
        return res;
    }

    private List<Map<String, Object>> mapHrs(List<HrManager> list) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (HrManager h : list) {
            res.add(Map.of(
                    "id", h.getId(),
                    "name", h.getName(),
                    "username", h.getUsername(),
                    "phone", h.getPhone(),
                    "email", h.getEmail(),
                    "branchId", h.getBranchId(),
                    "role", "HR"
            ));
        }
        return res;
    }
}
