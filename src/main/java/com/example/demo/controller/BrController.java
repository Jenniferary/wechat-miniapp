package com.example.demo.controller;

import com.example.demo.entity.BranchManager;
import com.example.demo.repository.BranchManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/branch-managers")
public class BrController {

    @Autowired
    private BranchManagerRepository branchManagerRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // 查询店长信息
        Optional<BranchManager> branchManagerOpt = branchManagerRepository.findByUsername(username);
        if (branchManagerOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        BranchManager branchManager = branchManagerOpt.get();

        // 用明文密码与数据库中的 password_hash 进行比较
        if (!password.equals(branchManager.getPassword())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        // 登录成功，返回店长信息（不返回密码）
        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", branchManager.getId(),
                        "branchId", branchManager.getBranchId(),
                        "username", branchManager.getUsername(),
                        "email", branchManager.getEmail(),
                        "phone", branchManager.getPhone(),
                        "name", branchManager.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getBranchManager(@PathVariable Integer id) {
        Optional<BranchManager> branchManagerOpt = branchManagerRepository.findById(id);
        if (branchManagerOpt.isEmpty()) {
            return Map.of("status", "error", "message", "店长不存在");
        }

        BranchManager branchManager = branchManagerOpt.get();

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", branchManager.getId(),
                        "branchId", branchManager.getBranchId(),
                        "username", branchManager.getUsername(),
                        "email", branchManager.getEmail(),
                        "phone", branchManager.getPhone(),
                        "name", branchManager.getName()
                )
        );
    }
}
