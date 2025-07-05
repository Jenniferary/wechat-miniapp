package com.example.demo.controller;

import com.example.demo.entity.Counter;
import com.example.demo.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/counter")
@CrossOrigin
public class CounterController {

    @Autowired
    private CounterRepository counterRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 前台注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Counter counter) {
        if (counterRepository.findByUsername(counter.getUsername()).isPresent()) {
            return Map.of("status", "error", "message", "用户名已存在");
        }

        counter.setPasswordHash(passwordEncoder.encode(counter.getPasswordHash()));
        counterRepository.save(counter);
        return Map.of("status", "success", "message", "注册成功");
    }

    // 前台登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");

        Optional<Counter> counterOpt = counterRepository.findByUsername(username);
        if (counterOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        Counter counter = counterOpt.get();
        if (!passwordEncoder.matches(password, counter.getPasswordHash())) {
            return Map.of("status", "error", "message", "密码错误");
        }
        System.out.println(counter.getBranchId());
        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", counter.getId(),
                        "username", counter.getUsername(),
                        "name", counter.getName(),
                        "phone", counter.getPhone(),
                        "email", counter.getEmail(),
                        "branchId", counter.getBranchId(),
                        "hireDate", counter.getHireDate()
                )
        );
    }

    // 获取所有前台（可选）
    @GetMapping
    public java.util.List<Counter> getAllCounters() {
        return counterRepository.findAll();
    }

    // 根据 ID 获取前台信息
    @GetMapping("/{id}")
    public Map<String, Object> getCounterById(@PathVariable Integer id) {
        Optional<Counter> opt = counterRepository.findById(id);
        if (opt.isEmpty()) {
            return Map.of("status", "error", "message", "前台不存在");
        }
        return Map.of("status", "success", "data", opt.get());
    }

    // 修改前台信息（只改电话和邮箱）
    @PutMapping("/{id}")
    public Map<String, Object> updateCounter(@PathVariable Integer id, @RequestBody Counter updated) {
        Optional<Counter> opt = counterRepository.findById(id);
        if (opt.isEmpty()) {
            return Map.of("status", "error", "message", "前台不存在");
        }

        Counter counter = opt.get();
        counter.setPhone(updated.getPhone());
        counter.setEmail(updated.getEmail());

        counterRepository.save(counter);
        return Map.of("status", "success", "message", "修改成功");
    }
}
