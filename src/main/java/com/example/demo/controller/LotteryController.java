package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lottery")
public class LotteryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/draw")
    public Map<String, Object> drawLottery(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }

            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    "SELECT user_id, member_points FROM users WHERE username = ?", username);

            if (users.isEmpty()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }

            Map<String, Object> userMap = users.get(0);
            Integer userId = (Integer) userMap.get("user_id");

            // 这里用BigDecimal取member_points，防止类型转换异常
            BigDecimal pointsDecimal = (BigDecimal) userMap.get("member_points");
            int points = pointsDecimal == null ? 0 : pointsDecimal.intValue();

            if (points < 10) {
                response.put("success", false);
                response.put("message", "积分不足");
                return response;
            }

            // 扣除10积分，确保同时判断积分足够，防止并发扣减失败
            int updateCount = jdbcTemplate.update(
                    "UPDATE users SET member_points = member_points - 10 WHERE user_id = ? AND member_points >= 10",
                    userId);

            if (updateCount == 0) {
                response.put("success", false);
                response.put("message", "扣积分失败，积分不足");
                return response;
            }

            // 30%概率中奖
            boolean isWin = Math.random() < 0.3;

            if (isWin) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiry = now.plusDays(10);

                jdbcTemplate.update(
                        "INSERT INTO coupons (user_id, start_date, expiry_date, min_threshold, discount_amount) VALUES (?, ?, ?, ?, ?)",
                        userId, Timestamp.valueOf(now), Timestamp.valueOf(expiry), new BigDecimal("100"), new BigDecimal("10"));
            }

            // 查询扣积分后的最新积分，BigDecimal转int
            BigDecimal newPointsDecimal = jdbcTemplate.queryForObject(
                    "SELECT member_points FROM users WHERE user_id = ?", BigDecimal.class, userId);
            int newPoints = newPointsDecimal == null ? 0 : newPointsDecimal.intValue();

            response.put("success", true);
            response.put("isWin", isWin);
            response.put("memberPoints", newPoints);
            response.put("message", isWin ? "中奖！获得优惠券" : "未中奖");

        } catch (Exception e) {
            e.printStackTrace(); // 查看具体异常日志
            response.put("success", false);
            response.put("message", "抽奖失败");
        }
        return response;
    }
}
