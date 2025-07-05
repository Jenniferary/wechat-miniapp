package com.example.demo.controller; // 根据你的包名修改

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(originPatterns = "http://localhost:*")
public class AnalyticsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    public Map<String, Object> getRealtimeStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 今日堂食订单数
            try {
                String dineInQuery = "SELECT COUNT(*) FROM orders WHERE DATE(time_ordered) = CURDATE()";
                Integer dineInOrders = jdbcTemplate.queryForObject(dineInQuery, Integer.class);
                stats.put("dineInOrders", dineInOrders != null ? dineInOrders : 0);
                System.out.println("堂食订单数: " + dineInOrders);
            } catch (Exception e) {
                System.out.println("获取堂食订单失败: " + e.getMessage());
                stats.put("dineInOrders", 0);
            }

            // 今日外卖订单数
            try {
                String takeawayQuery = "SELECT COUNT(*) FROM takeaway_orders WHERE DATE(time_ordered) = CURDATE()";
                Integer takeawayOrders = jdbcTemplate.queryForObject(takeawayQuery, Integer.class);
                stats.put("takeawayOrders", takeawayOrders != null ? takeawayOrders : 0);
                System.out.println("外卖订单数: " + takeawayOrders);
            } catch (Exception e) {
                System.out.println("获取外卖订单失败: " + e.getMessage());
                stats.put("takeawayOrders", 0);
            }

            // 餐桌使用情况
            try {
                String occupiedTablesQuery = "SELECT COUNT(*) FROM tables WHERE status = 'occupied'";
                Integer occupiedTables = jdbcTemplate.queryForObject(occupiedTablesQuery, Integer.class);
                stats.put("occupiedTables", occupiedTables != null ? occupiedTables : 0);

                String totalTablesQuery = "SELECT COUNT(*) FROM tables";
                Integer totalTables = jdbcTemplate.queryForObject(totalTablesQuery, Integer.class);
                stats.put("totalTables", totalTables != null ? totalTables : 0);

                System.out.println("餐桌使用情况: " + occupiedTables + "/" + totalTables);
            } catch (Exception e) {
                System.out.println("获取餐桌信息失败: " + e.getMessage());
                stats.put("occupiedTables", 0);
                stats.put("totalTables", 0);
            }

            // 今日总营收
            try {
                String revenueQuery = """
                    SELECT COALESCE(
                        (SELECT SUM(price) FROM orders WHERE DATE(time_ordered) = CURDATE() AND is_paid = 1) +
                        (SELECT SUM(price) FROM takeaway_orders WHERE DATE(time_ordered) = CURDATE() AND is_paid = 1),
                        0
                    ) as total_revenue
                    """;
                Double todayRevenue = jdbcTemplate.queryForObject(revenueQuery, Double.class);
                stats.put("todayRevenue", String.format("%.2f", todayRevenue != null ? todayRevenue : 0.0));
                System.out.println("今日营收: " + todayRevenue);
            } catch (Exception e) {
                System.out.println("获取营收失败: " + e.getMessage());
                stats.put("todayRevenue", "0.00");
            }

            System.out.println("最终统计数据: " + stats);
            return createSuccessResponse(stats);
        } catch (Exception e) {
            System.out.println("整体获取失败: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("获取实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/charts")
    public Map<String, Object> getChartsData(@RequestParam(defaultValue = "today") String timeRange) {
        try {
            Map<String, Object> chartsData = new HashMap<>();

            chartsData.put("orderTrend", getOrderTrendData(timeRange));
            chartsData.put("revenue", getRevenueData(timeRange));
            chartsData.put("orderType", getOrderTypeData(timeRange));
            chartsData.put("popularDishes", getPopularDishesData(timeRange));

            return createSuccessResponse(chartsData);
        } catch (Exception e) {
            System.out.println("获取图表数据失败: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("获取图表数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单趋势数据 - 简化版
     */
    private List<Map<String, Object>> getOrderTrendData(String timeRange) {
        String dateFormat = getDateFormat(timeRange);
        String dateFilter = getDateFilter(timeRange);

        try {
            // 分别查询堂食和外卖数据，然后合并
            String dineInQuery = String.format(
                    "SELECT %s as date, COUNT(*) as count FROM orders WHERE %s GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            String takeawayQuery = String.format(
                    "SELECT %s as date, COUNT(*) as count FROM takeaway_orders WHERE %s GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            List<Map<String, Object>> dineInData = jdbcTemplate.queryForList(dineInQuery);
            List<Map<String, Object>> takeawayData = jdbcTemplate.queryForList(takeawayQuery);

            // 合并数据
            Map<String, Map<String, Object>> mergedData = new HashMap<>();

            // 添加堂食数据
            for (Map<String, Object> row : dineInData) {
                String date = row.get("date").toString();
                Map<String, Object> dateData = new HashMap<>();
                dateData.put("date", date);
                dateData.put("dineIn", row.get("count"));
                dateData.put("takeaway", 0);
                mergedData.put(date, dateData);
            }

            // 添加外卖数据
            for (Map<String, Object> row : takeawayData) {
                String date = row.get("date").toString();
                Map<String, Object> dateData = mergedData.getOrDefault(date, new HashMap<>());
                dateData.put("date", date);
                dateData.put("dineIn", dateData.getOrDefault("dineIn", 0));
                dateData.put("takeaway", row.get("count"));
                mergedData.put(date, dateData);
            }

            // 转换为列表并排序
            List<Map<String, Object>> result = new ArrayList<>(mergedData.values());
            result.sort((a, b) -> a.get("date").toString().compareTo(b.get("date").toString()));

            return result;
        } catch (Exception e) {
            System.out.println("获取订单趋势数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取营收数据 - 简化版
     */
    private List<Map<String, Object>> getRevenueData(String timeRange) {
        String dateFormat = getDateFormat(timeRange);
        String dateFilter = getDateFilter(timeRange);

        try {
            String dineInQuery = String.format(
                    "SELECT %s as date, SUM(price) as revenue FROM orders WHERE %s AND is_paid = 1 GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            String takeawayQuery = String.format(
                    "SELECT %s as date, SUM(price) as revenue FROM takeaway_orders WHERE %s AND is_paid = 1 GROUP BY %s ORDER BY %s",
                    dateFormat, dateFilter, dateFormat, dateFormat
            );

            List<Map<String, Object>> dineInData = jdbcTemplate.queryForList(dineInQuery);
            List<Map<String, Object>> takeawayData = jdbcTemplate.queryForList(takeawayQuery);

            // 合并营收数据
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

            // 转换为结果格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, Double> entry : mergedRevenue.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", entry.getKey());
                item.put("revenue", entry.getValue());
                result.add(item);
            }

            // 排序
            result.sort((a, b) -> a.get("date").toString().compareTo(b.get("date").toString()));

            return result;
        } catch (Exception e) {
            System.out.println("获取营收数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取订单类型分布数据
     */
    private List<Map<String, Object>> getOrderTypeData(String timeRange) {
        String dateFilter = getDateFilter(timeRange);

        try {
            List<Map<String, Object>> result = new ArrayList<>();

            // 堂食订单数
            String dineInQuery = "SELECT COUNT(*) as count FROM orders WHERE " + dateFilter;
            Integer dineInCount = jdbcTemplate.queryForObject(dineInQuery, Integer.class);

            Map<String, Object> dineInData = new HashMap<>();
            dineInData.put("name", "堂食订单");
            dineInData.put("value", dineInCount != null ? dineInCount : 0);
            result.add(dineInData);

            // 外卖订单数
            String takeawayQuery = "SELECT COUNT(*) as count FROM takeaway_orders WHERE " + dateFilter;
            Integer takeawayCount = jdbcTemplate.queryForObject(takeawayQuery, Integer.class);

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
     * 获取热门菜品数据 - 简化版
     */
    private List<Map<String, Object>> getPopularDishesData(String timeRange) {
        String dateFilter = getDateFilter(timeRange);

        try {
            // 简化版：直接从菜品名称统计
            String query = String.format("""
                SELECT dish_name as name, COUNT(*) as count FROM (
                    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(dish_list, ',', 1), ',', -1)) as dish_name FROM orders WHERE %s
                    UNION ALL
                    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(dish_list, ',', 2), ',', -1)) as dish_name FROM orders WHERE %s AND CHAR_LENGTH(dish_list) - CHAR_LENGTH(REPLACE(dish_list, ',', '')) >= 1
                    UNION ALL
                    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(dish_list, ',', 1), ',', -1)) as dish_name FROM takeaway_orders WHERE %s
                    UNION ALL
                    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(dish_list, ',', 2), ',', -1)) as dish_name FROM takeaway_orders WHERE %s AND CHAR_LENGTH(dish_list) - CHAR_LENGTH(REPLACE(dish_list, ',', '')) >= 1
                ) as dishes 
                WHERE dish_name != '' AND dish_name IS NOT NULL
                GROUP BY dish_name 
                ORDER BY count DESC 
                LIMIT 10
                """, dateFilter, dateFilter, dateFilter, dateFilter);

            return jdbcTemplate.queryForList(query);
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