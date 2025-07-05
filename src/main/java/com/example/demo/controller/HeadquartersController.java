package com.example.demo.controller;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/headquarters")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class HeadquartersController {

    @Autowired
    private AdminRepository adminRepository;

    /**
     * 总部登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            String department = loginData.get("department");
            
            // 验证必填字段
            if (username == null || username.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "用户名不能为空");
                return response;
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "密码不能为空");
                return response;
            }
            
            if (department == null || !"headquarters".equals(department)) {
                response.put("status", "error");
                response.put("message", "请选择正确的部门");
                return response;
            }
            
            // 查询管理员信息
            Optional<Admin> adminOpt = adminRepository.findByUsernameAndPassword(username.trim(), password);
            
            if (adminOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "用户名或密码错误");
                return response;
            }
            
            Admin admin = adminOpt.get();
            
            // 登录成功
            response.put("status", "success");
            response.put("message", "登录成功");
            response.put("data", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "department", department,
                "role", "headquarters"
            ));
            
            return response;
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "登录失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 获取总部管理员信息
     */
    @GetMapping("/profile/{id}")
    public Map<String, Object> getProfile(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Admin> adminOpt = adminRepository.findById(id);
            
            if (adminOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "管理员不存在");
                return response;
            }
            
            Admin admin = adminOpt.get();
            
            response.put("status", "success");
            response.put("data", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "createTime", admin.getCreateTime().toString()
            ));
            
            return response;
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "获取信息失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/change-password/{id}")
    public Map<String, Object> changePassword(@PathVariable Integer id, @RequestBody Map<String, String> passwordData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                response.put("status", "error");
                response.put("message", "密码不能为空");
                return response;
            }
            
            Optional<Admin> adminOpt = adminRepository.findById(id);
            
            if (adminOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "管理员不存在");
                return response;
            }
            
            Admin admin = adminOpt.get();
            
            // 验证旧密码
            if (!admin.getPassword().equals(oldPassword)) {
                response.put("status", "error");
                response.put("message", "原密码错误");
                return response;
            }
            
            // 更新密码
            admin.setPassword(newPassword);
            adminRepository.save(admin);
            
            response.put("status", "success");
            response.put("message", "密码修改成功");
            
            return response;
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "修改密码失败: " + e.getMessage());
            return response;
        }
    }
}