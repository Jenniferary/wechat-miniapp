package com.example.demo.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://localhost:8081")
public class DataController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 数据导入 - 通过managerId验证权限
     */
    @PostMapping("/import")
    public Map<String, Object> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam Integer managerId,
            @RequestParam Integer branchId) {
        try {
            if (file.isEmpty()) {
                return createErrorResponse("文件不能为空");
            }

            // 验证权限
            if (!hasAccessToBranch(managerId, branchId)) {
                return createErrorResponse("无权限导入数据到该分店");
            }

            String result = processImportData(file, type, branchId);
            return createSuccessResponse(result);
        } catch (Exception e) {
            return createErrorResponse("导入失败: " + e.getMessage());
        }
    }

    /**
     * 数据导出 - 通过managerId验证权限
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportData(
            @RequestParam("type") String type,
            @RequestParam("format") String format,
            @RequestParam(defaultValue = "today") String timeRange,
            @RequestParam Integer managerId,
            @RequestParam Integer branchId) {
        try {
            // 验证权限
            if (!hasAccessToBranch(managerId, branchId)) {
                return ResponseEntity.badRequest().build();
            }

            ByteArrayResource resource = exportDataToResource(type, format, timeRange, branchId);

            String branchName = getBranchName(branchId);
            String filename = type + "_" + branchName + "_" + LocalDate.now() + "." + format;
            String contentType = format.equals("csv") ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
            return count != null && count > 0;
        } catch (Exception e) {
            System.out.println("验证权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 根据分店店长权限获取实际可访问的分店ID
     * 通过managerId验证权限
     */
    private Integer getAuthorizedBranchId(Integer managerId) {
        try {
            String query = "SELECT branch_id FROM branch_managers WHERE id = ?";
            return jdbcTemplate.queryForObject(query, Integer.class, managerId);
        } catch (Exception e) {
            System.out.println("获取授权分店ID失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取分店名称
     */
    private String getBranchName(Integer branchId) {
        try {
            // 根据您的数据库结构，应该查询restaurant_branches表
            String query = "SELECT name FROM restaurant_branches WHERE id = ?";
            return jdbcTemplate.queryForObject(query, String.class, branchId);
        } catch (Exception e) {
            return "分店" + branchId;
        }
    }

    /**
     * 处理文件导入 - 强制使用指定分店ID
     */
    private String processImportData(MultipartFile file, String type, Integer branchId) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new Exception("文件名不能为空");
        }

        List<Map<String, Object>> data;
        if (filename.endsWith(".csv")) {
            data = parseCSV(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            data = parseExcel(file);
        } else {
            throw new Exception("不支持的文件格式");
        }

        return processDataByType(data, type, branchId);
    }

    /**
     * 解析CSV文件
     */
    private List<Map<String, Object>> parseCSV(MultipartFile file) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            String[] headers = null;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (isFirstLine) {
                    headers = values;
                    isFirstLine = false;
                } else {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        row.put(headers[i].trim(), values[i].trim());
                    }
                    data.add(row);
                }
            }
        }
        return data;
    }

    /**
     * 解析Excel文件
     */
    private List<Map<String, Object>> parseExcel(MultipartFile file) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, Object> rowData = new HashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        String value = cell != null ? cell.toString().trim() : "";
                        rowData.put(headers.get(j), value);
                    }
                    data.add(rowData);
                }
            }
        }
        return data;
    }

    /**
     * 根据类型处理导入数据 - 强制使用指定分店ID
     */
    private String processDataByType(List<Map<String, Object>> data, String type, Integer branchId) {
        switch (type) {
            case "dishes":
                return importDishes(data, branchId);
            case "users":
                return importUsers(data, branchId);
            case "coupons":
                return importCoupons(data, branchId);
            case "orders":
                return importOrders(data, branchId);
            default:
                throw new RuntimeException("不支持的导入类型: " + type);
        }
    }

    /**
     * 导入菜品数据 - 强制使用指定分店ID
     */
    private String importDishes(List<Map<String, Object>> data, Integer branchId) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                String dishName = (String) row.get("dish_name");
                Double dishPrice = Double.valueOf(row.get("dish_price").toString());
                Integer dishStock = Integer.valueOf(row.get("dish_stock").toString());

                // 强制使用指定的分店ID，忽略文件中的branch_id
                jdbcTemplate.update(
                        "INSERT INTO dishes (dish_name, dish_price, dish_stock, branch_id) VALUES (?, ?, ?, ?)",
                        dishName, dishPrice, dishStock, branchId
                );
                count++;
            } catch (Exception e) {
                continue;
            }
        }
        String branchName = getBranchName(branchId);
        return "成功导入 " + count + " 条菜品数据到 " + branchName;
    }

    /**
     * 导入用户数据 - 强制使用指定分店ID
     */
    private String importUsers(List<Map<String, Object>> data, Integer branchId) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                String username = (String) row.get("username");
                String password = (String) row.get("password");
                String memberLevel = (String) row.getOrDefault("member_level", "VIP0");

                jdbcTemplate.update(
                        "INSERT INTO users (username, password, member_level, branch_id) VALUES (?, ?, ?, ?)",
                        username, password, memberLevel, branchId
                );
                count++;
            } catch (Exception e) {
                continue;
            }
        }
        String branchName = getBranchName(branchId);
        return "成功导入 " + count + " 条用户数据到 " + branchName;
    }

    /**
     * 导入优惠券数据 - 强制使用指定分店ID
     */
    private String importCoupons(List<Map<String, Object>> data, Integer branchId) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                Integer userId = Integer.valueOf(row.get("user_id").toString());
                String expiryDate = (String) row.get("expiry_date");
                Double minThreshold = Double.valueOf(row.get("min_threshold").toString());
                Double discountAmount = Double.valueOf(row.get("discount_amount").toString());

                jdbcTemplate.update(
                        "INSERT INTO coupons (user_id, expiry_date, min_threshold, discount_amount, branch_id) VALUES (?, ?, ?, ?, ?)",
                        userId, expiryDate, minThreshold, discountAmount, branchId
                );
                count++;
            } catch (Exception e) {
                continue;
            }
        }
        String branchName = getBranchName(branchId);
        return "成功导入 " + count + " 条优惠券数据到 " + branchName;
    }

    /**
     * 导入订单数据 - 强制使用指定分店ID
     */
    private String importOrders(List<Map<String, Object>> data, Integer branchId) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                Integer userId = Integer.valueOf(row.get("user_id").toString());
                String tableNumber = (String) row.get("table_number");
                String dishList = (String) row.get("dish_list");
                Double price = Double.valueOf(row.get("price").toString());
                String timeOrdered = (String) row.get("time_ordered");
                Integer isPaid = Integer.valueOf(row.getOrDefault("is_paid", "0").toString());

                jdbcTemplate.update(
                        "INSERT INTO orders (user_id, table_number, dish_list, price, time_ordered, is_paid, branch_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        userId, tableNumber, dishList, price, timeOrdered, isPaid, branchId
                );
                count++;
            } catch (Exception e) {
                System.out.println("导入订单行失败: " + e.getMessage());
                continue;
            }
        }
        String branchName = getBranchName(branchId);
        return "成功导入 " + count + " 条订单数据到 " + branchName;
    }

    /**
     * 导出数据为资源 - 强制使用指定分店ID
     */
    private ByteArrayResource exportDataToResource(String type, String format, String timeRange, Integer branchId) throws Exception {
        List<Map<String, Object>> data = getExportData(type, timeRange, branchId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (format.equals("csv")) {
            writeCSV(data, outputStream);
        } else if (format.equals("xlsx")) {
            writeExcel(data, outputStream);
        } else {
            throw new Exception("不支持的导出格式");
        }

        return new ByteArrayResource(outputStream.toByteArray());
    }

    /**
     * 获取导出数据 - 强制使用指定分店ID
     */
    private List<Map<String, Object>> getExportData(String type, String timeRange, Integer branchId) {
        String dateFilter = getDateFilter(timeRange);
        String branchFilter = " AND branch_id = " + branchId;

        switch (type) {
            case "orders":
                return jdbcTemplate.queryForList(
                        "SELECT order_id, user_id, table_number, dish_list, price, time_ordered, is_paid, branch_id FROM orders WHERE " + dateFilter + branchFilter + " ORDER BY time_ordered DESC"
                );
            case "takeaway":
                return jdbcTemplate.queryForList(
                        "SELECT order_id, user_id, delivery_address, dish_list, price, time_ordered, delivery_status, branch_id FROM takeaway_orders WHERE " + dateFilter + branchFilter + " ORDER BY time_ordered DESC"
                );

            case "revenue":
                String revenueQuery = """
                    SELECT 
                        DATE(time_ordered) as date,
                        SUM(price) as revenue,
                        COUNT(*) as order_count,
                        'orders' as source,
                        branch_id
                    FROM orders 
                    WHERE %s%s AND is_paid = 1
                    GROUP BY DATE(time_ordered), branch_id
                    
                    UNION ALL
                    
                    SELECT 
                        DATE(time_ordered) as date,
                        SUM(price) as revenue,
                        COUNT(*) as order_count,
                        'takeaway' as source,
                        branch_id
                    FROM takeaway_orders 
                    WHERE %s%s AND is_paid = 1
                    GROUP BY DATE(time_ordered), branch_id
                    
                    ORDER BY date DESC, source
                    """;

                return jdbcTemplate.queryForList(String.format(revenueQuery,
                        dateFilter, branchFilter,
                        dateFilter, branchFilter));

            case "dishes":
                return jdbcTemplate.queryForList(
                        "SELECT dish_id, dish_name, dish_price, dish_stock, branch_id FROM dishes WHERE branch_id = " + branchId + " ORDER BY dish_id"
                );

            case "users":
                return jdbcTemplate.queryForList(
                        "SELECT user_id, username, member_level, member_balance, member_points, branch_id FROM users WHERE branch_id = " + branchId + " ORDER BY user_id"
                );

            case "branches":
                return jdbcTemplate.queryForList(
                        "SELECT id as branch_id, name as branch_name FROM restaurant_branches WHERE id = " + branchId
                );

            default:
                throw new RuntimeException("不支持的导出类型: " + type);
        }
    }

    /**
     * 写入CSV文件
     */
    private void writeCSV(List<Map<String, Object>> data, ByteArrayOutputStream outputStream) throws Exception {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
            if (!data.isEmpty()) {
                // 写入BOM以支持中文
                outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

                // 写入表头
                String headers = String.join(",", data.get(0).keySet());
                writer.println(headers);

                // 写入数据
                for (Map<String, Object> row : data) {
                    String line = row.values().stream()
                            .map(value -> {
                                if (value == null) return "";
                                String str = value.toString();
                                // 如果包含逗号、引号或换行符，需要用引号包围并转义
                                if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
                                    str = "\"" + str.replace("\"", "\"\"") + "\"";
                                }
                                return str;
                            })
                            .collect(Collectors.joining(","));
                    writer.println(line);
                }
            }
        }
    }

    /**
     * 写入Excel文件
     */
    private void writeExcel(List<Map<String, Object>> data, ByteArrayOutputStream outputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            if (!data.isEmpty()) {
                // 创建表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // 创建表头
                Row headerRow = sheet.createRow(0);
                List<String> headers = new ArrayList<>(data.get(0).keySet());
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerStyle);
                }

                // 写入数据
                for (int i = 0; i < data.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    Map<String, Object> rowData = data.get(i);
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = rowData.get(headers.get(j));
                        if (value != null) {
                            // 尝试转换为数字
                            try {
                                if (value instanceof Number) {
                                    cell.setCellValue(((Number) value).doubleValue());
                                } else {
                                    String str = value.toString();
                                    if (str.matches("-?\\d+(\\.\\d+)?")) {
                                        cell.setCellValue(Double.parseDouble(str));
                                    } else {
                                        cell.setCellValue(str);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                cell.setCellValue(value.toString());
                            }
                        }
                    }
                }

                // 自动调整列宽
                for (int i = 0; i < headers.size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(outputStream);
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