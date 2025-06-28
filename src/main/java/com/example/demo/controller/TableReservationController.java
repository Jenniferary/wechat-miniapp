package com.example.demo.controller;

import com.example.demo.dto.ReservationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081")
public class TableReservationController {

    // 从 application.properties 文件中读取数据库连接配置
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    @PostMapping("/reserve")
    public Map<String, Object> reserveTable(@RequestBody ReservationRequest request) {
        Map<String, Object> result = new HashMap<>();
        String sql = null;
        String tableNumber = null;

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. 获取用户 ID
            String userSql = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement userStmt = conn.prepareStatement(userSql);
            userStmt.setString(1, request.getUsername());
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) {
                result.put("status", "error");
                result.put("message", "用户不存在");
                return result;
            }

            int userId = userRs.getInt("user_id");

            // 检查该用户是否已经预定过餐桌（status = 'occupied'）
            String checkSql = "SELECT COUNT(*) FROM tables WHERE user_id = ? AND status = 'occupied'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                result.put("status", "error");
                result.put("message", "您已经预定过餐桌！");
                return result;
            }

            // 2. 构建查询桌号 SQL
            String type = request.getTableType();
            int num = request.getNumOfPeople();
            if ("normal".equals(type)) {
                if (num <= 2)
                    sql = "SELECT * FROM tables WHERE table_type = 'small' AND status = 'available' LIMIT 1";
                else if (num <= 4)
                    sql = "SELECT * FROM tables WHERE table_type = 'medium' AND status = 'available' LIMIT 1";
                else if (num <= 8)
                    sql = "SELECT * FROM tables WHERE table_type = 'large' AND status = 'available' LIMIT 1";
                else {
                    result.put("status", "error");
                    result.put("message", "普通桌型仅支持1-8人");
                    return result;
                }
            } else if ("private".equals(type)) {
                if (num <= 8)
                    sql = "SELECT * FROM tables WHERE table_type = 'small private' AND status = 'available' LIMIT 1";
                else if (num <= 16)
                    sql = "SELECT * FROM tables WHERE table_type = 'large private' AND status = 'available' LIMIT 1";
                else {
                    result.put("status", "error");
                    result.put("message", "包厢仅支持1-16人");
                    return result;
                }
            }

            PreparedStatement tableStmt = conn.prepareStatement(sql);
            ResultSet tableRs = tableStmt.executeQuery();

            if (!tableRs.next()) {
                result.put("status", "error");
                result.put("message", "当前无可用餐桌，请稍候重试");
                return result;
            }

            tableNumber = tableRs.getString("table_number");

            // 3. 更新餐桌状态
            String updateSql = "UPDATE tables SET status = 'occupied', user_id = ?, reservation_time = ? WHERE table_number = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, userId);
            updateStmt.setString(2, request.getReservationTime());
            updateStmt.setString(3, tableNumber);
            updateStmt.executeUpdate();

            result.put("status", "success");
            result.put("message", "预定成功");
            result.put("tableNumber", tableNumber);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "预定失败：" + e.getMessage());
        }

        return result;
    }
}
