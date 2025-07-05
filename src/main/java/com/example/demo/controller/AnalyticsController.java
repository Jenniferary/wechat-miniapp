package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:8081")
public class AnalyticsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取当前登录用户的分店信息
     * 通过前端传递的managerId获取分店信息
     */
    @GetMapping("/branches")
    public Map<String, Object> getBranches(@RequestParam(required = false) Integer managerId) {
        try {
            List<Map<String, Object>> branches = new ArrayList<>();

            if (managerId != null) {
                // 根据managerId获取对应的分店
                String query = """
                    SELECT rb.id as branch_id, rb.name as branch_name 
                    FROM restaurant_branches rb 
                    INNER JOIN branch_managers bm ON rb.manager_id = bm.id
                    WHERE bm.id = ?
                    ORDER BY rb.id
                    """;
                branches = jdbcTemplate.queryForList(query, managerId);
            }

            return createSuccessResponse(branches);
        } catch (Exception e) {
            System.out.println("获取分店列表失败: " + e.getMessage());
            return createSuccessResponse(new ArrayList<>());
        }
    }

    /**
     * 获取实时统计数据 - 通过managerId验证权限
     */
    @GetMapping("/realtime")
    public Map<String, Object> getRealtimeStats(
            @RequestParam Integer managerId,
            @RequestParam Integer branchId) {
        try {
            // 添加调试信息
            System.out.println("=== 实时数据查询调试 ===");
            System.out.println("managerId: " + managerId);
            System.out.println("branchId: " + branchId);
            System.out.println("今日日期: " + java.time.LocalDate.now());

            Map<String, Object> stats = new HashMap<>();

            // 验证managerId是否有权限访问该branchId
            if (!hasAccessToBranch(managerId, branchId)) {
                return createErrorResponse("无权限访问该分店数据");
            }

            // 今日堂食订单数
            try {
                String dineInQuery = "SELECT COUNT(*) FROM orders WHERE DATE(time_ordered) = CURDATE() AND branch_id = ?";
                Integer dineInOrders = jdbcTemplate.queryForObject(dineInQuery, Integer.class, branchId);
                stats.put("dineInOrders", dineInOrders != null ? dineInOrders : 0);
                System.out.println("今日堂食订单数: " + dineInOrders);
            } catch (Exception e) {
                System.out.println("获取堂食订单失败: " + e.getMessage());
                stats.put("dineInOrders", 0);
            }

            // 今日外卖订单数
            try {
                String takeawayQuery = "SELECT COUNT(*) FROM takeaway_orders WHERE DATE(time_ordered) = CURDATE() AND branch_id = ?";
                Integer takeawayOrders = jdbcTemplate.queryForObject(takeawayQuery, Integer.class, branchId);
                stats.put("takeawayOrders", takeawayOrders != null ? takeawayOrders : 0);
                System.out.println("今日外卖订单数: " + takeawayOrders);
            } catch (Exception e) {
                System.out.println("获取外卖订单失败: " + e.getMessage());
                stats.put("takeawayOrders", 0);
            }

            // 餐桌使用情况 - 修复版本
            // 在getRealtimeStats方法中，替换餐桌使用情况的查询部分：

// 餐桌使用情况 - 修复版本（使用tables表总数36作为分母）
            try {
                Integer occupiedTables = 0;
                Integer totalTables = 0;

                try {
                    // 检查tables表是否存在
                    String checkTableQuery = "SHOW TABLES LIKE 'tables'";
                    List<Map<String, Object>> tableExists = jdbcTemplate.queryForList(checkTableQuery);

                    if (!tableExists.isEmpty()) {
                        // tables表存在，使用全部桌子数据（不按分店过滤）
                        String occupiedQuery = "SELECT COUNT(*) FROM tables WHERE status = 'occupied'";
                        String totalQuery = "SELECT COUNT(*) FROM tables";

                        occupiedTables = jdbcTemplate.queryForObject(occupiedQuery, Integer.class);
                        totalTables = jdbcTemplate.queryForObject(totalQuery, Integer.class);

                        System.out.println("使用tables表 - 占用桌子: " + occupiedTables + ", 总桌子: " + totalTables);
                    } else {
                        System.out.println("tables表不存在，使用订单表计算桌子占用率");
                        throw new Exception("tables表不存在");
                    }
                } catch (Exception e) {
                    // 方案2：使用订单表中的桌子信息（你建议的方式）
                    System.out.println("tables表查询失败，使用订单表计算: " + e.getMessage());

                    // 被占用的桌子：当前有未支付订单的不同桌子数量
                    String occupiedFromOrdersQuery = """
            SELECT COUNT(DISTINCT table_number) 
            FROM orders 
            WHERE is_paid = 0 AND branch_id = ? AND table_number IS NOT NULL AND table_number != ''
            """;

                    // 总桌子数：从所有历史订单中获取该分店使用过的不同桌子数量
                    String totalFromOrdersQuery = """
            SELECT COUNT(DISTINCT table_number) 
            FROM orders 
            WHERE branch_id = ? AND table_number IS NOT NULL AND table_number != ''
            """;

                    occupiedTables = jdbcTemplate.queryForObject(occupiedFromOrdersQuery, Integer.class, branchId);
                    totalTables = jdbcTemplate.queryForObject(totalFromOrdersQuery, Integer.class, branchId);

                    System.out.println("使用订单表计算 - 占用桌子: " + occupiedTables + ", 总桌子: " + totalTables);
                }

                stats.put("occupiedTables", occupiedTables != null ? occupiedTables : 0);
                stats.put("totalTables", totalTables != null ? totalTables : 0);

            } catch (Exception e) {
                System.out.println("获取餐桌数据完全失败: " + e.getMessage());
                stats.put("occupiedTables", 0);
                stats.put("totalTables", 0);
            }

            // 今日总营收 - 分步调试版本
            try {
                System.out.println("=== 营收查询调试 ===");

                // 先查询堂食已支付订单
                String dineInRevenueQuery = """
                    SELECT COALESCE(SUM(price), 0) as revenue, COUNT(*) as count
                    FROM orders 
                    WHERE DATE(time_ordered) = CURDATE() AND is_paid = 1 AND branch_id = ?
                    """;

                Map<String, Object> dineInResult = jdbcTemplate.queryForMap(dineInRevenueQuery, branchId);
                Double dineInRevenue = ((Number) dineInResult.get("revenue")).doubleValue();
                Integer dineInPaidCount = ((Number) dineInResult.get("count")).intValue();

                System.out.println("堂食已支付订单数: " + dineInPaidCount);
                System.out.println("堂食营收: " + dineInRevenue);

                // 查询外卖已支付订单
                String takeawayRevenueQuery = """
                    SELECT COALESCE(SUM(price), 0) as revenue, COUNT(*) as count
                    FROM takeaway_orders 
                    WHERE DATE(time_ordered) = CURDATE() AND is_paid = 1 AND branch_id = ?
                    """;

                Double takeawayRevenue = 0.0;
                Integer takeawayPaidCount = 0;

                try {
                    Map<String, Object> takeawayResult = jdbcTemplate.queryForMap(takeawayRevenueQuery, branchId);
                    takeawayRevenue = ((Number) takeawayResult.get("revenue")).doubleValue();
                    takeawayPaidCount = ((Number) takeawayResult.get("count")).intValue();
                } catch (Exception e) {
                    System.out.println("takeaway_orders表可能不存在: " + e.getMessage());
                    takeawayRevenue = 0.0;
                    takeawayPaidCount = 0;
                }

                System.out.println("外卖已支付订单数: " + takeawayPaidCount);
                System.out.println("外卖营收: " + takeawayRevenue);

                Double totalRevenue = dineInRevenue + takeawayRevenue;
                System.out.println("总营收: " + totalRevenue);

                // 额外调试：查看今日所有订单
                String debugQuery = """
                    SELECT order_id, price, is_paid, time_ordered 
                    FROM orders 
                    WHERE DATE(time_ordered) = CURDATE() AND branch_id = ?
                    ORDER BY time_ordered DESC
                    """;

                List<Map<String, Object>> todayOrders = jdbcTemplate.queryForList(debugQuery, branchId);
                System.out.println("今日所有订单 (" + todayOrders.size() + " 条):");
                for (Map<String, Object> order : todayOrders) {
                    System.out.println("  订单ID: " + order.get("order_id") +
                            ", 金额: " + order.get("price") +
                            ", 已支付: " + order.get("is_paid") +
                            ", 时间: " + order.get("time_ordered"));
                }

                stats.put("todayRevenue", String.format("%.2f", totalRevenue));

            } catch (Exception e) {
                System.out.println("获取营收数据失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("todayRevenue", "0.00");
            }

            // 添加当前分店信息
            stats.put("currentBranchId", branchId);
            stats.put("branchName", getBranchName(branchId));

            System.out.println("=== 最终返回数据 ===");
            System.out.println(stats);

            return createSuccessResponse(stats);
        } catch (Exception e) {
            System.out.println("获取实时数据失败: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("获取实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取图表数据 - 通过managerId验证权限
     */
    @GetMapping("/charts")
    public Map<String, Object> getChartsData(
            @RequestParam(defaultValue = "today") String timeRange,
            @RequestParam Integer managerId,
            @RequestParam Integer branchId) {
        try {
            // 验证权限
            if (!hasAccessToBranch(managerId, branchId)) {
                return createErrorResponse("无权限访问该分店数据");
            }

            Map<String, Object> chartsData = new HashMap<>();

            chartsData.put("orderTrend", getOrderTrendData(timeRange, branchId));
            chartsData.put("revenue", getRevenueData(timeRange, branchId));
            chartsData.put("orderType", getOrderTypeData(timeRange, branchId));
            chartsData.put("popularDishes", getPopularDishesData(timeRange, branchId));

            return createSuccessResponse(chartsData);
        } catch (Exception e) {
            System.out.println("获取图表数据失败: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("获取图表数据失败: " + e.getMessage());
        }
    }

    /**
     * 验证店长是否有权限访问指定分店
     */
    private boolean hasAccessToBranch(Integer managerId, Integer branchId) {
        try {
            String query = """
                SELECT COUNT(*) FROM restaurant_branches rb 
                INNER JOIN branch_managers bm ON rb.manager_id = bm.id
                WHERE bm.id = ? AND rb.id = ?
                """;
            Integer count = jdbcTemplate.queryForObject(query, Integer.class, managerId, branchId);
            boolean hasAccess = count != null && count > 0;
            System.out.println("权限验证 - managerId: " + managerId + ", branchId: " + branchId + ", 有权限: " + hasAccess);
            return hasAccess;
        } catch (Exception e) {
            System.out.println("验证权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取分店名称
     */
    private String getBranchName(Integer branchId) {
        try {
            String query = "SELECT name FROM restaurant_branches WHERE id = ?";
            return jdbcTemplate.queryForObject(query, String.class, branchId);
        } catch (Exception e) {
            return "未知分店";
        }
    }

    /**
     * 获取订单趋势数据 - 强制使用指定分店ID
     */
    private List<Map<String, Object>> getOrderTrendData(String timeRange, Integer branchId) {
        String dateFormat = getDateFormat(timeRange);
        String dateFilter = getDateFilter(timeRange);

        try {
            String dineInQuery = String.format(
                    "SELECT %s as date, COUNT(*) as count FROM orders WHERE %s AND branch_id = ? GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            String takeawayQuery = String.format(
                    "SELECT %s as date, COUNT(*) as count FROM takeaway_orders WHERE %s AND branch_id = ? GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            List<Map<String, Object>> dineInData = jdbcTemplate.queryForList(dineInQuery, branchId);
            List<Map<String, Object>> takeawayData = new ArrayList<>();

            try {
                takeawayData = jdbcTemplate.queryForList(takeawayQuery, branchId);
            } catch (Exception e) {
                System.out.println("takeaway_orders表查询失败: " + e.getMessage());
            }

            // 合并数据
            Map<String, Map<String, Object>> mergedData = new HashMap<>();

            for (Map<String, Object> row : dineInData) {
                String date = row.get("date").toString();
                Map<String, Object> dateData = new HashMap<>();
                dateData.put("date", date);
                dateData.put("dineIn", row.get("count"));
                dateData.put("takeaway", 0);
                mergedData.put(date, dateData);
            }

            for (Map<String, Object> row : takeawayData) {
                String date = row.get("date").toString();
                Map<String, Object> dateData = mergedData.getOrDefault(date, new HashMap<>());
                dateData.put("date", date);
                dateData.put("dineIn", dateData.getOrDefault("dineIn", 0));
                dateData.put("takeaway", row.get("count"));
                mergedData.put(date, dateData);
            }

            List<Map<String, Object>> result = new ArrayList<>(mergedData.values());
            result.sort((a, b) -> a.get("date").toString().compareTo(b.get("date").toString()));

            return result;
        } catch (Exception e) {
            System.out.println("获取订单趋势数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取营收数据 - 强制使用指定分店ID
     */
    private List<Map<String, Object>> getRevenueData(String timeRange, Integer branchId) {
        String dateFormat = getDateFormat(timeRange);
        String dateFilter = getDateFilter(timeRange);

        try {
            String dineInQuery = String.format(
                    "SELECT %s as date, SUM(price) as revenue FROM orders WHERE %s AND is_paid = 1 AND branch_id = ? GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            String takeawayQuery = String.format(
                    "SELECT %s as date, SUM(price) as revenue FROM takeaway_orders WHERE %s AND is_paid = 1 AND branch_id = ? GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            List<Map<String, Object>> dineInData = jdbcTemplate.queryForList(dineInQuery, branchId);
            List<Map<String, Object>> takeawayData = new ArrayList<>();

            try {
                takeawayData = jdbcTemplate.queryForList(takeawayQuery, branchId);
            } catch (Exception e) {
                System.out.println("takeaway_orders表查询失败: " + e.getMessage());
            }

            Map<String, Double> mergedRevenue = new HashMap<>();

            for (Map<String, Object> row : dineInData) {
                String date = row.get("date").toString();
                Double revenue = ((Number) row.get("revenue")).doubleValue();
                mergedRevenue.put(date, mergedRevenue.getOrDefault(date, 0.0) + revenue);
            }

            for (Map<String, Object> row : takeawayData) {
                String date = row.get("date").toString();
                Double revenue = ((Number) row.get("revenue")).doubleValue();
                mergedRevenue.put(date, mergedRevenue.getOrDefault(date, 0.0) + revenue);
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, Double> entry : mergedRevenue.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", entry.getKey());
                item.put("revenue", entry.getValue());
                result.add(item);
            }

            result.sort((a, b) -> a.get("date").toString().compareTo(b.get("date").toString()));

            return result;
        } catch (Exception e) {
            System.out.println("获取营收数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取订单类型分布数据 - 强制使用指定分店ID
     */
    private List<Map<String, Object>> getOrderTypeData(String timeRange, Integer branchId) {
        String dateFilter = getDateFilter(timeRange);

        try {
            List<Map<String, Object>> result = new ArrayList<>();

            String dineInQuery = "SELECT COUNT(*) as count FROM orders WHERE " + dateFilter + " AND branch_id = ?";
            Integer dineInCount = jdbcTemplate.queryForObject(dineInQuery, Integer.class, branchId);

            Map<String, Object> dineInData = new HashMap<>();
            dineInData.put("name", "堂食订单");
            dineInData.put("value", dineInCount != null ? dineInCount : 0);
            result.add(dineInData);

            String takeawayQuery = "SELECT COUNT(*) as count FROM takeaway_orders WHERE " + dateFilter + " AND branch_id = ?";
            Integer takeawayCount = 0;
            try {
                takeawayCount = jdbcTemplate.queryForObject(takeawayQuery, Integer.class, branchId);
            } catch (Exception e) {
                System.out.println("takeaway_orders表查询失败: " + e.getMessage());
            }

            Map<String, Object> takeawayData = new HashMap<>();
            takeawayData.put("name", "外卖订单");
            takeawayData.put("value", takeawayCount != null ? takeawayCount : 0);
            result.add(takeawayData);

            return result;
        } catch (Exception e) {
            System.out.println("获取订单类型分布失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门菜品数据 - 强制使用指定分店ID
     */
    private List<Map<String, Object>> getPopularDishesData(String timeRange, Integer branchId) {
        String dateFilter = getDateFilter(timeRange);

        try {
            String query = String.format("""
                SELECT dish_name as name, COUNT(*) as count FROM (
                    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(dish_list, ',', 1), ',', -1)) as dish_name FROM orders WHERE %s AND branch_id = ?
                    UNION ALL
                    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(dish_list, ',', 2), ',', -1)) as dish_name FROM orders WHERE %s AND branch_id = ? AND CHAR_LENGTH(dish_list) - CHAR_LENGTH(REPLACE(dish_list, ',', '')) >= 1
                ) as dishes 
                WHERE dish_name != '' AND dish_name IS NOT NULL
                GROUP BY dish_name 
                ORDER BY count DESC 
                LIMIT 10
                """, dateFilter, dateFilter);

            return jdbcTemplate.queryForList(query, branchId, branchId);
        } catch (Exception e) {
            System.out.println("获取热门菜品数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据时间范围获取日期格式
     */
    private String getDateFormat(String timeRange) {
        switch (timeRange) {
            case "today":
                return "DATE_FORMAT(time_ordered, '%H:00')";
            case "week":
                return "DATE_FORMAT(time_ordered, '%Y-%m-%d')";
            case "month":
                return "DATE_FORMAT(time_ordered, '%Y-%m-%d')";
            case "quarter":
                return "DATE_FORMAT(time_ordered, '%Y-%m')";
            case "year":
                return "DATE_FORMAT(time_ordered, '%Y-%m')";
            default:
                return "DATE_FORMAT(time_ordered, '%Y-%m-%d')";
        }
    }

    /**
     * 根据时间范围获取日期过滤条件
     */
    private String getDateFilter(String timeRange) {
        switch (timeRange) {
            case "today":
                return "DATE(time_ordered) = CURDATE()";
            case "week":
                return "time_ordered >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "month":
                return "time_ordered >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
            case "quarter":
                return "time_ordered >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)";
            case "year":
                return "time_ordered >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
            default:
                return "DATE(time_ordered) = CURDATE()";
        }
    }

    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}