package com.example.demo.controller;

import com.example.demo.entity.OrderRequest;
import com.example.demo.entity.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {
    @Autowired
    private JdbcTemplate jdbc;
    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer orderId = (Integer) request.get("order_id");
            String paymentMethod = (String) request.get("payment_method");  // 支付方式

            // 1. 获取订单信息（餐桌号、用户ID和支付金额）
            Map<String, Object> order = jdbc.queryForMap("SELECT table_number, user_id, price FROM orders WHERE order_id = ?", orderId);
            if (order == null) {
                response.put("success", false);
                response.put("message", "订单不存在");
                return response;
            }

            String tableNumber = (String) order.get("table_number");
            Integer userId = (Integer) order.get("user_id");
            Double paymentAmount = (Double) order.get("price");  // 从订单表中获取支付金额

            // 2. 根据支付方式处理支付
            boolean paymentSuccess = false;
            switch (paymentMethod) {
                case "card":
                    paymentSuccess = processCardPayment(orderId, paymentAmount, userId);
                    break;
                case "alipay":
                    paymentSuccess = processAlipayPayment(orderId, paymentAmount);
                    break;
                case "wechat":
                    paymentSuccess = processWeChatPayment(orderId, paymentAmount);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "不支持的支付方式");
                    return response;
            }

            // 3. 支付成功后更新订单为已支付
            if (paymentSuccess) {
                int updateOrder = jdbc.update("UPDATE orders SET is_paid = 1 WHERE order_id = ?", orderId);

                if (updateOrder > 0 && tableNumber != null) {
                    // 4. 更新餐桌状态，释放餐桌
                    jdbc.update("UPDATE tables SET status = 'available', user_id = NULL, reservation_time = NULL WHERE table_number = ?", tableNumber);

                    response.put("success", true);
                    response.put("message", "支付成功，餐桌已释放");
                } else {
                    response.put("success", false);
                    response.put("message", "支付成功，但更新订单失败");
                }
            } else {
                response.put("success", false);
                response.put("message", "支付失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "支付异常");
        }
        return response;
    }

    // 处理储值卡支付的逻辑
    private boolean processCardPayment(Integer orderId, Double paymentAmount, Integer userId) {
        try {
            // 获取用户余额
            Map<String, Object> user = jdbc.queryForMap("SELECT member_balance FROM users WHERE user_id = ?", userId);
            if (user == null || user.get("member_balance") == null) {
                return false;
            }

            // 获取余额并进行支付处理
            BigDecimal balance = (BigDecimal) user.get("member_balance");
            BigDecimal payment = new BigDecimal(paymentAmount);

            if (balance.compareTo(payment) >= 0) {
                BigDecimal newBalance = balance.subtract(payment);
                int updateUserBalance = jdbc.update("UPDATE users SET member_balance = ? WHERE user_id = ?", newBalance, userId);

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
    @PostMapping("/submit-order")
    public Map<String, Object> submitOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = orderRequest.getUsername();
            double totalPrice = orderRequest.getTotalPrice();
            List<String> items = orderRequest.getItems();
            double selectedCoupon = orderRequest.getSelectedCoupon();

            // 获取 user_id
            Integer userId = jdbc.queryForObject(
                    "SELECT user_id FROM users WHERE username = ?",
                    new Object[]{username},
                    Integer.class
            );

            // 获取用户占用的桌号
            String tableNumber;
            try {
                tableNumber = jdbc.queryForObject(
                        "SELECT table_number FROM tables WHERE user_id = ?",
                        new Object[]{userId},
                        String.class
                );
            } catch (EmptyResultDataAccessException e) {
                response.put("success", false);
                response.put("message", "该用户没有已占用的桌号！");
                response.put("errorCode", "tableNumberNotFound");
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "查询桌号时发生错误！");
                response.put("errorCode", "tableNumberQueryError");
                return response;
            }

            if (tableNumber == null) {
                response.put("success", false);
                response.put("message", "该用户没有已占用的桌号！");
                response.put("errorCode", "tableNumberNotFound");
                return response;
            }

            // 存储订单到 orders 表
            String dishList = String.join(", ", items);  // 菜品列表转换为字符串
            double finalPrice = totalPrice - selectedCoupon;  // 计算最终价格，扣除优惠券金额
            String remark = orderRequest.getRemark(); // 获取备注信息
            jdbc.update(
                    "INSERT INTO orders (user_id, table_number, dish_list, price, time_ordered, discount_amount, is_coupon_used, is_paid, remark) " +
                            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)",
                    userId,
                    tableNumber,
                    dishList,
                    finalPrice,
                    selectedCoupon,
                    selectedCoupon > 0 ? 1 : 0,
                    0,
                    remark
            );

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

            // 删除使用的优惠券（如有）
            if (selectedCoupon > 0) {
                try {
                    Integer couponId = jdbc.queryForObject(
                            "SELECT coupon_id FROM coupons WHERE user_id = ? AND discount_amount = ? AND expiry_date >= CURRENT_TIMESTAMP",
                            new Object[]{userId, selectedCoupon},
                            Integer.class
                    );
                    if (couponId != null) {
                        jdbc.update("DELETE FROM coupons WHERE coupon_id = ?", couponId);
                    }
                } catch (Exception ignore) {}
            }

            // 查询剩余优惠券
            List<Map<String, Object>> coupons = jdbc.queryForList(
                    "SELECT coupon_id, min_threshold, discount_amount FROM coupons " +
                            "WHERE user_id = ? AND expiry_date >= CURRENT_TIMESTAMP AND min_threshold <= ?",
                    userId, totalPrice
            );

            response.put("success", true);
            response.put("availableCoupons", coupons);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "订单提交失败！");
            response.put("availableCoupons", Collections.emptyList());
            return response;
        }
    }

    // 获取用户可用优惠券接口
    @PostMapping("/fetch-coupons")
    public Map<String, Object> fetchCoupons(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = (String) request.get("username");

            // 安全转换总价为 double
            Number priceNumber = (Number) request.get("totalPrice");
            double totalPrice = priceNumber.doubleValue();

            // 获取 user_id
            Integer userId = jdbc.queryForObject(
                    "SELECT user_id FROM users WHERE username = ?",
                    new Object[]{username},
                    Integer.class
            );

            // 获取可用优惠券
            List<Map<String, Object>> coupons = jdbc.queryForList(
                    "SELECT coupon_id, min_threshold, discount_amount FROM coupons " +
                            "WHERE user_id = ? AND expiry_date >= CURRENT_TIMESTAMP AND min_threshold <= ?",
                    userId, totalPrice
            );

            response.put("success", true);
            response.put("availableCoupons", coupons);
            return response;

        } catch (Exception e) {
            e.printStackTrace();  // 查看后端控制台输出
            response.put("success", false);
            response.put("message", "获取优惠券失败！");
            response.put("availableCoupons", Collections.emptyList());
            return response;
        }
    }


    // 获取用户订单接口
    @GetMapping("/my-orders")
    public Map<String, Object> getUserOrders(@RequestParam("username") String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = jdbc.queryForObject(
                    "SELECT user_id FROM users WHERE username = ?",
                    new Object[]{username},
                    Integer.class
            );

            // 查询用户的所有订单
            List<Map<String, Object>> orders = jdbc.queryForList(
                    "SELECT order_id, table_number, dish_list, price, time_ordered, is_paid, discount_amount, is_coupon_used,remark " +
                            "FROM orders WHERE user_id = ?",
                    userId
            );

            response.put("success", true);
            response.put("orders", orders);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取订单失败！");
            return response;
        }
    }

    // 获取数据库所有订单信息
    @GetMapping("/get-all-orders")
    public Map<String, Object> getAllOrders() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 查询所有订单
            List<Map<String, Object>> orders = jdbc.queryForList(
                    "SELECT order_id, user_id, table_number, dish_list, price, time_ordered, is_paid, discount_amount, is_coupon_used " +
                            "FROM orders"
            );

            response.put("success", true);
            response.put("orders", orders);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取所有订单失败！");
        }
        return response;
    }


}

