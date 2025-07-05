package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@CrossOrigin(origins = "http://localhost:8081")
public class ExchangeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 获取用户当前积分
    @GetMapping("/points")
    public Map<String, Object> getPoints(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT member_points FROM users WHERE username = ?";
            Double points = jdbcTemplate.queryForObject(sql, new Object[]{username}, Double.class);

            if (points != null) {
                result.put("success", true);
                result.put("points", points);
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取积分失败: " + e.getMessage());
        }
        return result;
    }

    // 兑换优惠券
    @PostMapping("/redeem")
    public Map<String, Object> redeemCoupon(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        String username = (String) payload.get("username");
        double requiredPoints = ((Number) payload.get("requiredPoints")).doubleValue();
        double minThreshold = ((Number) payload.get("minThreshold")).doubleValue();
        double discountAmount = ((Number) payload.get("discountAmount")).doubleValue();

        try {
            // 获取用户积分和ID
            String userSql = "SELECT user_id, member_points FROM users WHERE username = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(userSql, username);

            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            int userId = (Integer) user.get("user_id");
            double userPoints = (Double) user.get("member_points");

            if (userPoints < requiredPoints) {
                result.put("success", false);
                result.put("message", "积分不足，无法兑换");
                return result;
            }

            // 扣除积分
            double newPoints = userPoints - requiredPoints;
            String updateSql = "UPDATE users SET member_points = ? WHERE user_id = ?";
            jdbcTemplate.update(updateSql, newPoints, userId);

            // 插入优惠券记录
            String insertSql = "INSERT INTO coupons (user_id, start_date, expiry_date, min_threshold, discount_amount) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, userId,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now().plusDays(14)),
                    minThreshold, discountAmount);

            result.put("success", true);
            result.put("message", "兑换成功，优惠券已发放");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "兑换失败: " + e.getMessage());
        }

        return result;
    }
}
