package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/takeaway")
@CrossOrigin(origins = "http://localhost:8081")
public class TakeawayController {

    @Autowired
    private JdbcTemplate jdbc;

    @PostMapping("/submit")
    public Map<String, Object> submitTakeawayOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = (String) request.get("username");
            String address = (String) request.get("address");
            String estimatedTime = (String) request.get("estimatedTime");
            List<String> items = (List<String>) request.get("items");

            // 检查 totalPrice 和 selectedCoupon 是否为 null，若为 null 则给定默认值
            double totalPrice = request.containsKey("totalPrice") && request.get("totalPrice") != null ?
                    Double.parseDouble(request.get("totalPrice").toString()) : 0.0;
            double selectedCoupon = request.containsKey("selectedCoupon") && request.get("selectedCoupon") != null ?
                    Double.parseDouble(request.get("selectedCoupon").toString()) : 0.0;

            // 获取 user_id
            Integer userId = jdbc.queryForObject(
                    "SELECT user_id FROM users WHERE username = ?",
                    new Object[]{username},
                    Integer.class
            );

            if (userId == null) {
                throw new Exception("未找到用户：" + username);
            }

            String dishList = String.join(", ", items);
            double finalPrice = totalPrice - selectedCoupon;
            boolean isCouponUsed = selectedCoupon > 0;
            // ✅ 更新库存：每道菜减1
            Map<String, Integer> dishCountMap = new HashMap<>();
            for (String dishName : items) {
                dishCountMap.put(dishName, dishCountMap.getOrDefault(dishName, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : dishCountMap.entrySet()) {
                String dishName = entry.getKey();
                int count = entry.getValue();

                // 检查库存是否足够（可选）
                Integer currentStock = jdbc.queryForObject(
                        "SELECT dish_stock FROM dishes WHERE dish_name = ?",
                        new Object[]{dishName},
                        Integer.class
                );
                if (currentStock == null || currentStock < count) {
                    response.put("success", false);
                    response.put("message", "菜品库存不足：" + dishName);
                    response.put("errorCode", "stockNotEnough");
                    return response;
                }

                // 扣减库存
                jdbc.update(
                        "UPDATE dishes SET dish_stock = dish_stock - ? WHERE dish_name = ?",
                        count,
                        dishName
                );
            }
            // 插入订单数据
            jdbc.update("""
    INSERT INTO takeaway_orders (
        user_id, delivery_address, dish_list, price, time_ordered,
        discount_amount, is_coupon_used, is_paid,
        delivery_person_id, delivery_time, delivery_status, estimated_delivery_time, remark
    )
    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, NULL, NULL, 'pending', ?, ?)
""", userId, address, dishList, finalPrice, selectedCoupon, isCouponUsed ? 1 : 0, 0, estimatedTime, request.get("remark"));


            // 如果使用了优惠券，则处理优惠券的删除
            if (isCouponUsed) {
                try {
                    Integer couponId = jdbc.queryForObject(
                            "SELECT coupon_id FROM coupons WHERE user_id = ? AND discount_amount = ? AND expiry_date >= CURRENT_TIMESTAMP LIMIT 1",
                            new Object[]{userId, selectedCoupon},
                            Integer.class
                    );
                    if (couponId != null) {
                        jdbc.update("DELETE FROM coupons WHERE coupon_id = ?", couponId);
                    }
                } catch (Exception ignored) {
                    // 如果优惠券不存在或发生错误，忽略并继续
                }
            }

            // 查询可用优惠券
            List<Map<String, Object>> coupons = jdbc.queryForList("""
            SELECT coupon_id, min_threshold, discount_amount
            FROM coupons
            WHERE user_id = ? AND expiry_date >= CURRENT_TIMESTAMP AND min_threshold <= ?
        """, userId, totalPrice);

            response.put("success", true);
            response.put("availableCoupons", coupons);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "提交外卖订单失败！ 错误信息: " + e.getMessage());
            response.put("availableCoupons", Collections.emptyList());
        }

        return response;
    }



    @PostMapping("/fetch-coupons")
    public Map<String, Object> fetchCoupons(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = (String) request.get("username");
            double totalPrice = Double.parseDouble(request.get("totalPrice").toString());

            Integer userId = jdbc.queryForObject(
                    "SELECT user_id FROM users WHERE username = ?",
                    new Object[]{username},
                    Integer.class
            );

            List<Map<String, Object>> coupons = jdbc.queryForList("""
                SELECT coupon_id, min_threshold, discount_amount
                FROM coupons
                WHERE user_id = ? AND expiry_date >= CURRENT_TIMESTAMP AND min_threshold <= ?
            """, userId, totalPrice);

            response.put("success", true);
            response.put("availableCoupons", coupons);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取优惠券失败！");
            response.put("availableCoupons", Collections.emptyList());
        }

        return response;
    }
}
