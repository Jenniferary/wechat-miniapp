package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "http://localhost:*")  // Vue 默认端口
public class ReviewController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 查询当前用户所有订单（含评价状态）
    @GetMapping("/orders")
    public List<Map<String, Object>> getUserOrders(@RequestParam String username) {
        String userSql = "SELECT user_id FROM users WHERE username = ?";
        Integer userId = jdbcTemplate.queryForObject(userSql, Integer.class, username);

        String orderSql = "SELECT * FROM orders WHERE user_id = ?";
        List<Map<String, Object>> orders = jdbcTemplate.queryForList(orderSql, userId);

        for (Map<String, Object> order : orders) {
            Integer orderId = (Integer) order.get("order_id");

            // 判断是否已评价
            String reviewCheckSql = "SELECT COUNT(*) FROM reviews WHERE order_id = ?";
            Integer reviewCount = jdbcTemplate.queryForObject(reviewCheckSql, Integer.class, orderId);
            order.put("reviewed", reviewCount > 0);

            if (reviewCount > 0) {
                // 获取评价内容
                Map<String, Object> review = jdbcTemplate.queryForMap(
                        "SELECT rating, comment FROM reviews WHERE order_id = ?", orderId);
                order.put("rating", review.get("rating"));
                order.put("comment", review.get("comment"));
            }
        }

        return orders;
    }

    // 提交评价接口
    @PostMapping("/submit-review")
    public Map<String, String> submitReview(@RequestBody Map<String, Object> payload) {
        Integer orderId = (Integer) payload.get("order_id");
        Integer rating = (Integer) payload.get("rating");
        String comment = (String) payload.get("comment");

        String sql = "INSERT INTO reviews (order_id, rating, comment) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, orderId, rating, comment);

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "评价提交成功！");
        return result;
    }
}
