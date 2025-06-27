package com.example.demo.controller;

import com.example.demo.entity.Coupon;
import com.example.demo.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // 使用 JdbcTemplate 直接查询数据库

    // 根据用户名获取用户优惠券
    @GetMapping("/{username}")
    public List<Coupon> getCouponsByUsername(@PathVariable String username) {
        // 直接查询 users 表获取用户 ID
        Integer userId = getUserIdByUsername(username);
        if (userId != null) {
            // 查找该用户的所有优惠券
            return couponRepository.findByUserId(userId);
        } else {
            // 如果找不到用户，可以返回一个空列表或错误响应
            return null;
        }
    }

    // 通过用户名查询用户 ID
    private Integer getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, username);  // 使用 JdbcTemplate 查询用户 ID
    }
    @PostMapping("/distribute")
    public Map<String, Object> distributeCoupons(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userIdInput = data.get("userId").toString();
            String startDate = data.get("startDate").toString(); // 例如 "2025-04-18T15:20"
            String endDate = data.get("endDate").toString();
            BigDecimal minThreshold = new BigDecimal(data.get("minThreshold").toString());
            BigDecimal discountAmount = new BigDecimal(data.get("discountAmount").toString());

            // 使用 LocalDateTime 解析 datetime-local 格式，避免 Timestamp 格式错误
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startLocalDateTime = LocalDateTime.parse(startDate, formatter);
            LocalDateTime endLocalDateTime = LocalDateTime.parse(endDate, formatter);

            Timestamp startTimestamp = Timestamp.valueOf(startLocalDateTime);
            Timestamp endTimestamp = Timestamp.valueOf(endLocalDateTime);

            if (userIdInput.equalsIgnoreCase("ALL")) {
                // 分发给所有用户
                List<Integer> userIds = jdbcTemplate.queryForList("SELECT user_id FROM users", Integer.class);
                for (Integer userId : userIds) {
                    jdbcTemplate.update(
                            "INSERT INTO coupons (user_id, start_date, expiry_date, min_threshold, discount_amount) VALUES (?, ?, ?, ?, ?)",
                            userId, startTimestamp, endTimestamp, minThreshold, discountAmount);
                }
                response.put("message", "优惠券已分发给所有用户！");
            } else {
                // 分发给指定用户
                Integer userId = Integer.parseInt(userIdInput);
                int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?", Integer.class, userId);
                if (count == 0) {
                    response.put("success", false);
                    response.put("message", "用户不存在！");
                    return response;
                }

                jdbcTemplate.update(
                        "INSERT INTO coupons (user_id, start_date, expiry_date, min_threshold, discount_amount) VALUES (?, ?, ?, ?, ?)",
                        userId, startTimestamp, endTimestamp, minThreshold, discountAmount);
                response.put("message", "优惠券分发成功！");
            }

            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "分发失败！");
        }

        return response;
    }

}
