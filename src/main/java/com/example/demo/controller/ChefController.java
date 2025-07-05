package com.example.demo.controller;

import com.example.demo.entity.Chef;
import com.example.demo.repository.ChefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chef")
@CrossOrigin
public class ChefController {

    @Autowired
    private ChefRepository chefRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Chef chef) {
        if (chefRepository.findByUsername(chef.getUsername()).isPresent()) {
            return Map.of("status", "error", "message", "用户名已存在");
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(chef.getPasswordHash());
        chef.setPasswordHash(encodedPassword);

        chefRepository.save(chef);
        return Map.of("status", "success", "message", "注册成功");
    }

    // 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<Chef> chefOpt = chefRepository.findByUsername(username);
        if (chefOpt.isEmpty()) {
            return Map.of("status", "error", "message", "用户名不存在");
        }

        Chef chef = chefOpt.get();
        if (!passwordEncoder.matches(password, chef.getPasswordHash())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "id", chef.getId(),
                        "username", chef.getUsername(),
                        "name", chef.getName(),
                        "phone", chef.getPhone(),
                        "email", chef.getEmail(),
                        "branchId", chef.getBranchId()
                )
        );
    }

    // 根据ID查询厨师信息
    @GetMapping("/{id}")
    public Map<String, Object> getChefById(@PathVariable Integer id) {
        return chefRepository.findById(id)
                .map(chef -> Map.of(
                        "status", "success",
                        "data", chef
                ))
                .orElseGet(() -> Map.of(
                        "status", "error",
                        "message", "用户不存在"
                ));
    }

    // 更新厨师信息
    @PutMapping("/{id}")
    public Map<String, String> updateChef(@PathVariable Integer id, @RequestBody Chef updatedChef) {
        return chefRepository.findById(id).map(chef -> {
            chef.setPhone(updatedChef.getPhone());
            chef.setEmail(updatedChef.getEmail());
            chefRepository.save(chef);
            return Map.of("status", "success", "message", "信息已更新");
        }).orElseGet(() -> Map.of("status", "error", "message", "用户不存在"));
    }
}
