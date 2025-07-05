// DeliveryPersonController.java
package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(originPatterns = "http://localhost:*")
public class DeliveryPersonController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 获取所有空闲外卖员
    @GetMapping("/available")
    public List<Map<String, Object>> getAvailableDeliveryPersons() {
        String sql = "SELECT * FROM delivery_persons WHERE status = 'not_delivering'";
        return jdbcTemplate.queryForList(sql);
    }

    // 添加外卖员
    @PostMapping("/add")
    public Map<String, Object> addDeliveryPerson(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String phone = body.get("phoneNumber");

        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "INSERT INTO delivery_persons (name, phone_number, status) VALUES (?, ?, 'not_delivering')";
            int rows = jdbcTemplate.update(sql, name, phone);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }
        return result;
    }
}