package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/api/takeaway")
@CrossOrigin(origins = "http://localhost:8081")
public class TakeawayAssignController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ✅ 1. 获取所有待分配的外卖订单（delivery_status = 'pending'）
    @GetMapping("/pending-orders/{branchId}")
    public List<Map<String, Object>> getPendingOrders(@PathVariable String branchId) {
        String sql = "SELECT * FROM takeaway_orders WHERE delivery_status = 'pending' and branch_id = "+branchId+" ORDER BY time_ordered DESC";
        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", rs.getInt("order_id"));
            order.put("deliveryAddress", rs.getString("delivery_address"));
            order.put("dishList", rs.getString("dish_list"));
            order.put("price", rs.getDouble("price"));
            order.put("timeOrdered", rs.getTimestamp("time_ordered"));
            order.put("discountAmount", rs.getDouble("discount_amount"));
            return order;
        });
    }

    // ✅ 2. 获取所有可用的外卖员（status = 'not_delivering'）
    @GetMapping("/available-delivery-persons")
    public List<Map<String, Object>> getAvailableDeliveryPersons() {
        String sql = "SELECT * FROM delivery_persons WHERE status = 'not_delivering'";
        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            Map<String, Object> person = new HashMap<>();
            person.put("deliveryPersonId", rs.getInt("delivery_person_id"));
            person.put("name", rs.getString("name"));
            return person;
        });
    }

    // ✅ 3. 分配外卖员给订单，同时更新外卖员和订单状态
    @PostMapping("/assign-delivery")
    public Map<String, Object> assignDelivery(@RequestBody Map<String, Object> request) {
        Integer orderId = (Integer) request.get("orderId");
        Integer deliveryPersonId = (Integer) request.get("deliveryPersonId");

        Map<String, Object> result = new HashMap<>();
        if (orderId == null || deliveryPersonId == null) {
            result.put("success", false);
            result.put("message", "参数缺失");
            return result;
        }

        try {
            // 1. 设置订单状态为 out_for_delivery
            String updateOrderSql = "UPDATE takeaway_orders SET delivery_person_id = ?, delivery_status = 'out_for_delivery' WHERE order_id = ?";
            int updated1 = jdbcTemplate.update(updateOrderSql, deliveryPersonId, orderId);

            // 2. 设置外卖员状态为 delivering
            String updatePersonSql = "UPDATE delivery_persons SET status = 'delivering' WHERE delivery_person_id = ?";
            int updated2 = jdbcTemplate.update(updatePersonSql, deliveryPersonId);

            if (updated1 > 0 && updated2 > 0) {
                result.put("success", true);
                result.put("message", "分配成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误：" + e.getMessage());
        }

        return result;
    }

    // ✅ 4. 获取派送中的外卖订单（delivery_status = 'out_for_delivery'）
    @GetMapping("/out-orders")
    public List<Map<String, Object>> getOutForDeliveryOrders() {
        String sql = "SELECT * FROM takeaway_orders WHERE delivery_status = 'out_for_delivery' ORDER BY time_ordered DESC";
        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", rs.getInt("order_id"));
            order.put("deliveryAddress", rs.getString("delivery_address"));
            order.put("dishList", rs.getString("dish_list"));
            order.put("price", rs.getDouble("price"));
            order.put("timeOrdered", rs.getTimestamp("time_ordered"));
            order.put("discountAmount", rs.getDouble("discount_amount"));
            order.put("deliveryPersonId", rs.getInt("delivery_person_id"));
            return order;
        });
    }

    // ✅ 5. 确认送达并记录送达时间，同时将外卖员状态恢复为 not_delivering
    @PostMapping("/confirm-delivery")
    public Map<String, Object> confirmDelivery(@RequestBody Map<String, Object> request) {
        Integer orderId = (Integer) request.get("orderId");
        Timestamp deliveryTime = new Timestamp(System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取订单的配送员 ID
            String getSql = "SELECT delivery_person_id FROM takeaway_orders WHERE order_id = ?";
            Integer personId = jdbcTemplate.queryForObject(getSql, Integer.class, orderId);

            // 1. 更新订单状态为 delivered，设置实际送达时间
            String updateOrderSql = "UPDATE takeaway_orders SET delivery_status = 'delivered', delivery_time = ? WHERE order_id = ?";
            int orderUpdate = jdbcTemplate.update(updateOrderSql, deliveryTime, orderId);

            // 2. 设置外卖员状态为 not_delivering
            String updatePersonSql = "UPDATE delivery_persons SET status = 'not_delivering' WHERE delivery_person_id = ?";
            int personUpdate = jdbcTemplate.update(updatePersonSql, personId);

            if (orderUpdate > 0 && personUpdate > 0) {
                result.put("success", true);
                result.put("message", "确认送达成功");
            } else {
                result.put("success", false);
                result.put("message", "确认失败");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "错误：" + e.getMessage());
        }

        return result;
    }
}
