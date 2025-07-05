package com.example.demo.controller;

import com.example.demo.entity.HrManager;
import com.example.demo.repository.HrManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin
public class HrManagerController {

    @Autowired
    private HrManagerRepository hrManagerRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<HrManager> hrOpt = hrManagerRepository.findByUsername(username);
        if (hrOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        HrManager hrManager = hrOpt.get();

        if (!passwordEncoder.matches(password, hrManager.getPasswordHash())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", hrManager.getId(),
                        "username", hrManager.getUsername(),
                        "name", hrManager.getName(),
                        "email", hrManager.getEmail(),
                        "phone", hrManager.getPhone(),
                        "branchId", hrManager.getBranchId(),
                        "hireDate", hrManager.getHireDate()
                )
        );
    }

    // 获取 HR 信息
    @GetMapping("/{id}")
    public Map<String, Object> getHrInfo(@PathVariable Integer id) {
        Optional<HrManager> hrOpt = hrManagerRepository.findById(id);
        if (hrOpt.isEmpty()) {
            return Map.of("status", "error", "message", "HR不存在");
        }
        HrManager hr = hrOpt.get();
        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", hr.getId(),
                        "username", hr.getUsername(),
                        "name", hr.getName(),
                        "email", hr.getEmail(),
                        "phone", hr.getPhone(),
                        "branchId", hr.getBranchId(),
                        "hireDate", hr.getHireDate()
                )
        );
    }

    // 修改 HR 信息（仅允许修改 email 和 phone）
    @PutMapping("/{id}")
    public Map<String, Object> updateHrInfo(@PathVariable Integer id, @RequestBody Map<String, Object> updateData) {
        Optional<HrManager> hrOpt = hrManagerRepository.findById(id);
        if (hrOpt.isEmpty()) {
            return Map.of("status", "error", "message", "HR不存在");
        }

        HrManager hr = hrOpt.get();

        // 只允许更新 email 和 phone
        if (updateData.containsKey("email")) {
            hr.setEmail((String) updateData.get("email"));
        }
        if (updateData.containsKey("phone")) {
            hr.setPhone((String) updateData.get("phone"));
        }

        hrManagerRepository.save(hr);
        return Map.of("status", "success", "message", "信息更新成功");
    }
}
