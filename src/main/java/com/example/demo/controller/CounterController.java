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

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", counter.getId(),
                        "username", counter.getUsername(),
                        "name", counter.getName(),
                        "phone", counter.getPhone(),
                        "email", counter.getEmail(),
                        "branchId", counter.getBranchId()
                )
        );
    }
}
