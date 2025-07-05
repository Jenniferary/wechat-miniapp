package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;

@RestController
@RequestMapping("/api/table")
@CrossOrigin(originPatterns = "http://localhost:*")
public class TableController {

    // 自动注入JdbcTemplate（Spring提供的数据库操作工具类，底层会自动使用DataSource）
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TableController(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 餐桌分配接口
     */
    @PostMapping("/distribute")
    public Map<String, Object> distributeTable(@RequestBody Map<String, Object> req) {
        Map<String, Object> result = new HashMap<>();

        try {
            int userId = Integer.parseInt(req.get("userId").toString());
            String tableType = (String) req.get("tableType");
            int numOfPeople = Integer.parseInt(req.get("numOfPeople").toString());
            String reservationTime = (String) req.get("reservationTime");

            // 检查用户是否存在
            String checkUserSql = "SELECT * FROM users WHERE user_id = ?";
            List<Map<String, Object>> userRs = jdbcTemplate.queryForList(checkUserSql, userId);
            if (userRs.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 根据类型和人数构造SQL
            String queryTableSql = "";
            if ("normal".equals(tableType)) {
                if (numOfPeople <= 2) {
                    queryTableSql = "SELECT * FROM tables WHERE table_type = 'small' AND status = 'available' LIMIT 1";
                } else if (numOfPeople <= 4) {
                    queryTableSql = "SELECT * FROM tables WHERE table_type = 'medium' AND status = 'available' LIMIT 1";
                } else if (numOfPeople <= 8) {
                    queryTableSql = "SELECT * FROM tables WHERE table_type = 'large' AND status = 'available' LIMIT 1";
                } else {
                    result.put("success", false);
                    result.put("message", "普通桌仅支持 1-8 人");
                    return result;
                }
            } else if ("private".equals(tableType)) {
                if (numOfPeople <= 8) {
                    queryTableSql = "SELECT * FROM tables WHERE table_type = 'small private' AND status = 'available' LIMIT 1";
                } else if (numOfPeople <= 16) {
                    queryTableSql = "SELECT * FROM tables WHERE table_type = 'large private' AND status = 'available' LIMIT 1";
                } else {
                    result.put("success", false);
                    result.put("message", "包厢仅支持 1-16 人");
                    return result;
                }
            }

            String tableNumber = null;
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(queryTableSql);
            if (!tables.isEmpty()) {
                tableNumber = (String) tables.get(0).get("table_number");

                // 分配
                String updateSql = "UPDATE tables SET status = 'occupied', user_id = ?, reservation_time = ? WHERE table_number = ?";
                jdbcTemplate.update(updateSql, userId, reservationTime, tableNumber);

                result.put("success", true);
                result.put("tableNumber", tableNumber);
                return result;
            } else {
                result.put("success", false);
                result.put("message", "暂无可用的餐桌");
                return result;
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    /**
     * 餐桌释放接口
     */
    @PostMapping("/relieve")
    public Map<String, Object> relieveTables(@RequestBody Map<String, Object> req) {
        Map<String, Object> result = new HashMap<>();
        List<String> releasedTables = new ArrayList<>();

        try {
            int userId = Integer.parseInt(req.get("userId").toString());

            // 查询用户是否存在
            String userSql = "SELECT * FROM users WHERE user_id = ?";
            List<Map<String, Object>> rs = jdbcTemplate.queryForList(userSql, userId);
            if (rs.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 查询被该用户占用的桌号
            String selectSql = "SELECT table_number FROM tables WHERE user_id = ?";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(selectSql, userId);
            for (Map<String, Object> table : tables) {
                releasedTables.add((String) table.get("table_number"));
            }

            // 释放这些餐桌
            String updateSql = "UPDATE tables SET status = 'available', user_id = NULL, reservation_time = NULL WHERE user_id = ?";
            int affected = jdbcTemplate.update(updateSql, userId);
            if (affected > 0) {
                result.put("success", true);
                result.put("releasedTables", releasedTables);
                result.put("message", "释放成功！");
            } else {
                result.put("success", false);
                result.put("message", "该用户未预定任何餐桌");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
