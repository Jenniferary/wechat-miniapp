package com.example.demo.controller;

import com.example.demo.entity.BranchManager;
import com.example.demo.repository.BranchManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/branch-managers")
public class BrController {

    @Autowired
    private BranchManagerRepository branchManagerRepository;

    // 创建 BCryptPasswordEncoder 实例
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // 查询店长信息
        Optional<BranchManager> branchManagerOpt = branchManagerRepository.findByUsername(username);
        if (branchManagerOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        BranchManager manager = branchManagerOpt.get();

        // 用明文密码与数据库的加密密码比对（数据库中字段为 password_hash，Java 中为 password）
        if (!passwordEncoder.matches(password, manager.getPassword())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        // 登录成功，返回店长信息（不返回密码）
        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", manager.getId(),
                        "branchId", manager.getBranchId(),
                        "username", manager.getUsername(),
                        "email", manager.getEmail(),
                        "phone", manager.getPhone(),
                        "name", manager.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getBranchManager(@PathVariable Integer id) {
        Optional<BranchManager> managerOpt = branchManagerRepository.findById(id);
        if (managerOpt.isEmpty()) {
            return Map.of("status", "error", "message", "店长不存在");
        }

        BranchManager manager = managerOpt.get();

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", manager.getId(),
                        "branchId", manager.getBranchId(),
                        "username", manager.getUsername(),
                        "email", manager.getEmail(),
                        "phone", manager.getPhone(),
                        "name", manager.getName()
                )
        );
    }
}
