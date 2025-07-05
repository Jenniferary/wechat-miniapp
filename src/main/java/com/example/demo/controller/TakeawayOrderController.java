package com.example.demo.controller;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/takeaway")
@CrossOrigin(origins = "http://localhost:8081") // Vue 前端端口
public class TakeawayOrderController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 查询用户外卖订单
    @GetMapping("/orders/{username}")
    public List<Map<String, Object>> getTakeawayOrders(@PathVariable String username) {
        // 获取用户ID
        String userIdSql = "SELECT user_id FROM users WHERE username = ?";
        Integer userId = jdbcTemplate.queryForObject(userIdSql, Integer.class, username);

        if (userId == null) return Collections.emptyList();

        // 查询订单
        String orderSql = "SELECT * FROM takeaway_orders WHERE user_id = ? ORDER BY time_ordered DESC";

        return jdbcTemplate.query(orderSql, (ResultSet rs, int rowNum) -> {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", rs.getInt("order_id"));
            order.put("deliveryAddress", rs.getString("delivery_address"));
            order.put("dishList", rs.getString("dish_list"));
            order.put("price", rs.getDouble("price"));
            order.put("timeOrdered", rs.getTimestamp("time_ordered"));
            order.put("discountAmount", rs.getDouble("discount_amount"));
            order.put("isCouponUsed", rs.getBoolean("is_coupon_used"));
            order.put("deliveryPersonId", rs.getInt("delivery_person_id"));
            order.put("estimatedDeliveryTime", rs.getTimestamp("estimated_delivery_time"));
            order.put("deliveryTime", rs.getTimestamp("delivery_time"));
            order.put("deliveryStatus", rs.getString("delivery_status"));
            order.put("remark",rs.getString("remark"));
            return order;
        }, userId);
    }

    // 查询全部外卖订单（管理员用）
    @GetMapping("/all-orders")
    public List<Map<String, Object>> getAllTakeawayOrders() {
        String sql = "SELECT * FROM takeaway_orders ORDER BY time_ordered DESC";
        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", rs.getInt("order_id"));
            order.put("userId", rs.getInt("user_id"));
            order.put("deliveryAddress", rs.getString("delivery_address"));
            order.put("dishList", rs.getString("dish_list"));
            order.put("price", rs.getDouble("price"));
            order.put("timeOrdered", rs.getTimestamp("time_ordered"));
            order.put("discountAmount", rs.getDouble("discount_amount"));
            order.put("deliveryPersonId", rs.getInt("delivery_person_id"));
            order.put("deliveryStatus", rs.getString("delivery_status"));
            order.put("remark",rs.getString("remark"));
            return order;
        });
    }

    @PostMapping("/confirm")
    public Map<String, Object> confirmDelivery(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> result = new HashMap<>();

        Integer orderId;

        // ✅ 统一解析并强制转换为整数
        try {
            orderId = Integer.valueOf(requestBody.get("orderId").toString());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "参数错误：orderId 缺失或格式不正确");
            return result;
        }

        Timestamp deliveryTime = new Timestamp(System.currentTimeMillis());

        Integer deliveryPersonId;
        try {
            String getDeliveryPersonIdSql = "SELECT delivery_person_id FROM takeaway_orders WHERE order_id = ?";
            deliveryPersonId = jdbcTemplate.queryForObject(getDeliveryPersonIdSql, Integer.class, orderId);
        } catch (EmptyResultDataAccessException e) {
            result.put("success", false);
            result.put("message", "订单未找到，无法确认收货");
            return result;
        }

        // ✅ 更新外卖员状态
        int deliveryPersonStatusUpdated = jdbcTemplate.update(
                "UPDATE delivery_persons SET status = 'not_delivering' WHERE delivery_person_id = ?",
                deliveryPersonId
        );

        // ✅ 更新订单状态
        int orderStatusUpdated = jdbcTemplate.update(
                "UPDATE takeaway_orders SET delivery_status = 'delivered', delivery_time = ? WHERE order_id = ?",
                deliveryTime, orderId
        );

        if (orderStatusUpdated > 0 && deliveryPersonStatusUpdated > 0) {
            result.put("success", true);
            result.put("message", "确认收货成功，外卖员状态已更新");
        } else {
            result.put("success", false);
            result.put("message", "确认失败：数据库更新异常");
        }

        return result;
    }


    //支付接口
    @PostMapping("/pay")
    public Map<String, Object> handlePayment(@RequestBody Map<String, Object> requestBody) {
        Integer orderId = (Integer) requestBody.get("orderId");
        String paymentMethod = (String) requestBody.get("paymentMethod");

        // 获取订单的价格（你可以根据订单获取价格或其他信息）
        String orderSql = "SELECT price FROM takeaway_orders WHERE order_id = ?";
        Double price = jdbcTemplate.queryForObject(orderSql, Double.class, orderId);

        Map<String, Object> result = new HashMap<>();
        boolean paymentSuccess = false;

        if ("card".equalsIgnoreCase(paymentMethod)) {
            // 通过储值卡支付
            Integer userId = getUserIdByOrderId(orderId); // 从订单中获取用户ID
            paymentSuccess = processCardPayment(orderId, price, userId);
        } else if ("alipay".equalsIgnoreCase(paymentMethod)) {
            // 通过支付宝支付
            paymentSuccess = processAlipayPayment(orderId, price);
        } else if ("wechat".equalsIgnoreCase(paymentMethod)) {
            // 通过微信支付
            paymentSuccess = processWeChatPayment(orderId, price);
        }

        if (paymentSuccess) {
            // 支付成功，更新订单的支付状态
            String updatePaymentStatusSql = "UPDATE takeaway_orders SET is_paid = 1 WHERE order_id = ?";
            jdbcTemplate.update(updatePaymentStatusSql, orderId);

            result.put("success", true);
            result.put("message", "支付成功！");
        } else {
            result.put("success", false);
            result.put("message", "支付失败");
        }

        return result;
    }
    // 获取用户ID
    private Integer getUserIdByOrderId(Integer orderId) {
        String sql = "SELECT user_id FROM takeaway_orders WHERE order_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, orderId);
    }

    private boolean processCardPayment(Integer orderId, Double paymentAmount, Integer userId) {
        try {
            // 获取用户余额
            Map<String, Object> user = jdbcTemplate.queryForMap("SELECT member_balance FROM users WHERE user_id = ?", userId);
            if (user == null || user.get("member_balance") == null) {
                return false;
            }

            // 获取余额并进行支付处理
            BigDecimal balance = (BigDecimal) user.get("member_balance");
            BigDecimal payment = new BigDecimal(paymentAmount);

            if (balance.compareTo(payment) >= 0) {
                BigDecimal newBalance = balance.subtract(payment);
                // 更新用户余额
                int updateUserBalance = jdbcTemplate.update("UPDATE users SET member_balance = ? WHERE user_id = ?", newBalance, userId);

                // 更新订单支付状态
                String updateOrderStatusSql = "UPDATE takeaway_orders SET is_paid = 1 WHERE order_id = ?";
                jdbcTemplate.update(updateOrderStatusSql, orderId);

                return updateUserBalance > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // 处理支付宝支付的逻辑
    private boolean processAlipayPayment(Integer orderId, Double paymentAmount) {
        // 这里可以调用支付宝的 SDK 进行支付处理
        return true;  // 假设支付成功
    }

    // 处理微信支付的逻辑
    private boolean processWeChatPayment(Integer orderId, Double paymentAmount) {
        // 这里可以调用微信支付的 SDK 进行支付处理
        return true;
    }




}
