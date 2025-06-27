package com.example.demo.controller;

import com.example.demo.dto.CancelRequest;
import com.example.demo.entity.DishTableEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081")
public class MyReservationController {
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    // 获取我的预定记录
    @GetMapping("/my-reservation")
    public List<DishTableEntity> getMyReservations(@RequestParam String username) {
        List<DishTableEntity> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 查询 user_id
            String getUserSql = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement userStmt = conn.prepareStatement(getUserSql);
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) return list;
            int userId = userRs.getInt("user_id");

            // 查询该用户预定的桌子
            String getTablesSql = "SELECT id, table_number, reservation_time FROM tables WHERE user_id = ?";
            PreparedStatement tableStmt = conn.prepareStatement(getTablesSql);
            tableStmt.setInt(1, userId);
            ResultSet rs = tableStmt.executeQuery();

            while (rs.next()) {
                DishTableEntity entity = new DishTableEntity();
                entity.setReservationId(rs.getInt("id"));
                entity.setTableNumber(rs.getString("table_number"));
                entity.setReservationTime(rs.getString("reservation_time"));
                list.add(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 取消预定
    @PostMapping("/cancel-reservation")
    public Map<String, Object> cancelReservation(@RequestBody CancelRequest request) {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String cancelSql = "UPDATE tables SET status='available', user_id=NULL, reservation_time=NULL WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(cancelSql);
            stmt.setInt(1, request.getReservationId());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                result.put("status", "success");
                result.put("message", "预订已成功取消");
            } else {
                result.put("status", "error");
                result.put("message", "取消失败，未找到记录");
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "操作异常：" + e.getMessage());
        }

        return result;
    }
}
