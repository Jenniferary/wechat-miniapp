package com.example.demo.controller; // 根据你的包名修改

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
     * 数据导入
     */
    @PostMapping("/import")
    public Map<String, Object> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        try {
            if (file.isEmpty()) {
                return createErrorResponse("文件不能为空");
            }

            String result = processImportData(file, type);
            return createSuccessResponse(result);
        } catch (Exception e) {
            return createErrorResponse("导入失败: " + e.getMessage());
        }
    }

    /**
     * 数据导出
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportData(
            @RequestParam("type") String type,
            @RequestParam("format") String format,
            @RequestParam(defaultValue = "today") String timeRange) {
        try {
            ByteArrayResource resource = exportDataToResource(type, format, timeRange);

            String filename = type + "_" + LocalDate.now() + "." + format;
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
     * 处理文件导入
     */
    private String processImportData(MultipartFile file, String type) throws Exception {
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

        return processDataByType(data, type);
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
     * 根据类型处理导入数据
     */
    private String processDataByType(List<Map<String, Object>> data, String type) {
        switch (type) {
            case "dishes":
                return importDishes(data);
            case "users":
                return importUsers(data);
            case "coupons":
                return importCoupons(data);
            default:
                throw new RuntimeException("不支持的导入类型: " + type);
        }
    }

    /**
     * 导入菜品数据
     */
    private String importDishes(List<Map<String, Object>> data) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                String dishName = (String) row.get("dish_name");
                Double dishPrice = Double.valueOf(row.get("dish_price").toString());
                Integer dishStock = Integer.valueOf(row.get("dish_stock").toString());

                jdbcTemplate.update(
                        "INSERT INTO dishes (dish_name, dish_price, dish_stock) VALUES (?, ?, ?)",
                        dishName, dishPrice, dishStock
                );
                count++;
            } catch (Exception e) {
                // 跳过错误行
                continue;
            }
        }
        return "成功导入 " + count + " 条菜品数据";
    }

    /**
     * 导入用户数据
     */
    private String importUsers(List<Map<String, Object>> data) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                String username = (String) row.get("username");
                String password = (String) row.get("password");
                String memberLevel = (String) row.getOrDefault("member_level", "VIP0");

                jdbcTemplate.update(
                        "INSERT INTO users (username, password, member_level) VALUES (?, ?, ?)",
                        username, password, memberLevel
                );
                count++;
            } catch (Exception e) {
                // 跳过错误行
                continue;
            }
        }
        return "成功导入 " + count + " 条用户数据";
    }

    /**
     * 导入优惠券数据
     */
    private String importCoupons(List<Map<String, Object>> data) {
        int count = 0;
        for (Map<String, Object> row : data) {
            try {
                Integer userId = Integer.valueOf(row.get("user_id").toString());
                String expiryDate = (String) row.get("expiry_date");
                Double minThreshold = Double.valueOf(row.get("min_threshold").toString());
                Double discountAmount = Double.valueOf(row.get("discount_amount").toString());

                jdbcTemplate.update(
                        "INSERT INTO coupons (user_id, expiry_date, min_threshold, discount_amount) VALUES (?, ?, ?, ?)",
                        userId, expiryDate, minThreshold, discountAmount
                );
                count++;
            } catch (Exception e) {
                // 跳过错误行
                continue;
            }
        }
        return "成功导入 " + count + " 条优惠券数据";
    }

    /**
     * 导出数据为资源
     */
    private ByteArrayResource exportDataToResource(String type, String format, String timeRange) throws Exception {
        List<Map<String, Object>> data = getExportData(type, timeRange);

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
     * 获取导出数据
     */
    private List<Map<String, Object>> getExportData(String type, String timeRange) {
        String dateFilter = getDateFilter(timeRange);

        switch (type) {
            case "orders":
                return jdbcTemplate.queryForList(
                        "SELECT order_id, user_id, table_number, dish_list, price, time_ordered, is_paid FROM orders WHERE " + dateFilter
                );
            case "takeaway":
                return jdbcTemplate.queryForList(
                        "SELECT order_id, user_id, delivery_address, dish_list, price, time_ordered, delivery_status FROM takeaway_orders WHERE " + dateFilter
                );
            case "revenue":
                return jdbcTemplate.queryForList(String.format("""
                    SELECT 
                        DATE(time_ordered) as date,
                        SUM(price) as revenue,
                        COUNT(*) as order_count
                    FROM (
                        SELECT time_ordered, price FROM orders WHERE %s AND is_paid = 1
                        UNION ALL
                        SELECT time_ordered, price FROM takeaway_orders WHERE %s AND is_paid = 1
                    ) as all_orders
                    GROUP BY DATE(time_ordered)
                    ORDER BY date
                    """, dateFilter, dateFilter));
            case "dishes":
                return jdbcTemplate.queryForList(
                        "SELECT dish_name, dish_price, dish_stock FROM dishes"
                );
            case "users":
                return jdbcTemplate.queryForList(
                        "SELECT user_id, username, member_level, member_balance, member_points FROM users"
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
                // 写入表头
                String headers = String.join(",", data.get(0).keySet());
                writer.println(headers);

                // 写入数据
                for (Map<String, Object> row : data) {
                    String line = row.values().stream()
                            .map(value -> value != null ? value.toString() : "")
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
                // 创建表头
                Row headerRow = sheet.createRow(0);
                List<String> headers = new ArrayList<>(data.get(0).keySet());
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                }

                // 写入数据
                for (int i = 0; i < data.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    Map<String, Object> rowData = data.get(i);
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = rowData.get(headers.get(j));
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
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