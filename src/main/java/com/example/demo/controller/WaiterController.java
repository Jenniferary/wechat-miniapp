package com.example.demo.controller;

import com.example.demo.entity.Waiter;
import com.example.demo.repository.WaiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/waiters")
@CrossOrigin
public class WaiterController {

    @Autowired
    private WaiterRepository waiterRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 服务员注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Waiter waiter) {
        if (waiterRepository.findByUsername(waiter.getUsername()).isPresent()) {
            return Map.of("status", "error", "message", "用户名已存在");
        }
        // 密码加密存储
        waiter.setPasswordHash(passwordEncoder.encode(waiter.getPasswordHash()));

        waiterRepository.save(waiter);

        return Map.of("status", "success", "message", "注册成功");
    }

    // 服务员登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<Waiter> waiterOpt = waiterRepository.findByUsername(username);
        if (waiterOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        Waiter waiter = waiterOpt.get();

        if (!passwordEncoder.matches(password, waiter.getPasswordHash())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        // 登录成功，返回基本信息
        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", waiter.getId(),
                        "username", waiter.getUsername(),
                        "name", waiter.getName(),
                        "phone", waiter.getPhone(),
                        "email", waiter.getEmail(),
                        "branchId", waiter.getBranchId()
                )
        );
    }

    // 获取所有服务员（可选）
    @GetMapping
    public java.util.List<Waiter> getAllWaiters() {
        return waiterRepository.findAll();
    }

    // 根据ID获取服务员信息（可选）
    @GetMapping("/{id}")
    public Map<String, Object> getWaiterById(@PathVariable Integer id) {
        Optional<Waiter> waiterOpt = waiterRepository.findById(id);
        if (waiterOpt.isEmpty()) {
            return Map.of("status", "error", "message", "服务员不存在");
        }
        return Map.of("status", "success", "data", waiterOpt.get());
    }
}
