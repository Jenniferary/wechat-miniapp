package com.example.demo.controller;

import com.example.demo.entity.BranchManager;
import com.example.demo.repository.BranchManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * BrController：登录 + 店长信息 + 实时概览 + 考勤统计 + CSV 导出
 */
@RestController
@RequestMapping("/api/branch-managers")
public class BrController {

    @Autowired
    private BranchManagerRepository branchManagerRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /* ---------- 1. 登录 / 店长信息 ---------- */

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        BranchManager m = branchManagerRepository.findByUsername(username)
                .orElse(null);
        if (m == null) {
            return Map.of("status", "error", "message", "用户名不存在");
        }
        if (!encoder.matches(password, m.getPassword())) {
            return Map.of("status", "error", "message", "密码错误");
        }

        return Map.of("status", "success",
                "data", Map.of(
                        "id", m.getId(),
                        "branchId", m.getBranchId(),
                        "username", m.getUsername(),
                        "email", m.getEmail(),
                        "phone", m.getPhone(),
                        "name", m.getName()
                ));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getManager(@PathVariable Integer id) {
        BranchManager m = branchManagerRepository.findById(id).orElse(null);
        if (m == null) return Map.of("status", "error", "message", "店长不存在");

        return Map.of("status", "success",
                "data", Map.of(
                        "id", m.getId(),
                        "branchId", m.getBranchId(),
                        "username", m.getUsername(),
                        "email", m.getEmail(),
                        "phone", m.getPhone(),
                        "name", m.getName()
                ));
    }

    /* ---------- 2. 实时概览 ---------- */

    @GetMapping("/{id}/dashboard")
    public Map<String, Object> dashboard(@PathVariable Integer id) {
        BranchManager bm = branchManagerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("店长不存在"));
        int branchId = bm.getBranchId();

        int employeeCnt = countEmployees(branchId);

        // 获取当月考勤数据
        int attendance = jdbc.queryForObject(
                "SELECT COUNT(*) FROM attendance_records " +
                        "WHERE branch_id=? " +
                        "AND DATE_FORMAT(check_in_date,'%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')",
                Integer.class, branchId);

        int overtime = jdbc.queryForObject(
                "SELECT COUNT(*) FROM overtime_requests " +
                        "WHERE branch_id=? " +
                        "AND DATE_FORMAT(date,'%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')",
                Integer.class, branchId);

        int totalHours = (attendance + overtime) * 8;
        double avg = employeeCnt == 0 ? 0 : 1.0 * totalHours / employeeCnt;

        return Map.of("status", "success",
                "data", Map.of(
                        "employeeCount", employeeCnt,
                        "totalHours", totalHours,
                        "avgHours", avg));
    }

    /* ---------- 3. 考勤统计 ---------- */

    @GetMapping("/{id}/attendance-stats")
    public Map<String, Object> getAttendanceStats(
            @PathVariable Integer id,
            @RequestParam String month) {  // 格式: 2024-01

        BranchManager bm = branchManagerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("店长不存在"));
        int branchId = bm.getBranchId();

        // 解析月份
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);

        // 计算该月的第一天和最后一天
        LocalDate firstDay = LocalDate.of(year, monthValue, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        // 计算该月工作日天数（排除周六周日）
        int shouldWorkDays = calculateWorkingDays(firstDay, lastDay);

        // 获取该门店所有员工
        List<Map<String, Object>> employees = getAllEmployees(branchId);

        List<Map<String, Object>> statsList = new ArrayList<>();

        for (Map<String, Object> emp : employees) {
            int empId = (Integer) emp.get("id");
            String empName = (String) emp.get("name");
            String empRole = (String) emp.get("role");

            // 根据角色确定employee_type
            String employeeType = getEmployeeType(empRole);

            // 计算实际出勤天数（需要加上employee_type和branch_id条件）
            int actualWorkDays = jdbc.queryForObject(
                    "SELECT COUNT(DISTINCT DATE(check_in_date)) FROM attendance_records " +
                            "WHERE employee_id=? AND employee_type=? AND branch_id=? " +
                            "AND DATE(check_in_date) BETWEEN ? AND ?",
                    Integer.class, empId, employeeType, branchId, firstDay, lastDay);

            // 计算请假天数（需要加上employee_type和branch_id条件）
            int leaveDays = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(DATEDIFF(end_date, start_date) + 1), 0) FROM leave_requests " +
                            "WHERE employee_id=? AND employee_type=? AND branch_id=? AND status='审批成功' " +
                            "AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?) " +
                            "OR (start_date <= ? AND end_date >= ?))",
                    Integer.class, empId, employeeType, branchId, firstDay, lastDay, firstDay, lastDay, firstDay, lastDay);

            // 计算旷工天数
            int absentDays = shouldWorkDays - actualWorkDays - leaveDays;
            absentDays = Math.max(0, absentDays); // 确保不为负数

            // 计算加班次数（需要加上employee_type和branch_id条件）
            int overtimeCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM overtime_requests " +
                            "WHERE employee_id=? AND employee_type=? AND branch_id=? " +
                            "AND status='审批成功' AND date BETWEEN ? AND ?",
                    Integer.class, empId, employeeType, branchId, firstDay, lastDay);

            // 计算出勤率
            double attendanceRate = shouldWorkDays > 0 ?
                    (double) actualWorkDays / shouldWorkDays * 100 : 0;

            // 计算工时统计（实际出勤天数+加班天数）* 8小时
            int totalWorkHours = (actualWorkDays + overtimeCount) * 8;

            Map<String, Object> stats = new HashMap<>();
            stats.put("employeeId", empId);
            stats.put("employeeName", empName);
            stats.put("shouldWorkDays", shouldWorkDays);
            stats.put("actualWorkDays", actualWorkDays);
            stats.put("leaveDays", leaveDays);
            stats.put("absentDays", absentDays);
            stats.put("overtimeCount", overtimeCount);
            stats.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            stats.put("totalWorkHours", totalWorkHours);

            statsList.add(stats);
        }

        return Map.of("status", "success", "data", statsList);
    }

    /* ---------- 4. 历史数据分析 ---------- */

    @GetMapping("/{id}/attendance-history")
    public Map<String, Object> getAttendanceHistory(
            @PathVariable Integer id,
            @RequestParam String granularity,  // week | month | quarter | year
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso =
                    org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso =
                    org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate to) {

        BranchManager bm = branchManagerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("店长不存在"));
        int branchId = bm.getBranchId();

        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            LocalDate end = segmentEnd(cursor, granularity, to);
            String label = labelOf(cursor, granularity);

            // 统计该时间段的考勤数据
            int totalAttendance = jdbc.queryForObject(
                    "SELECT COUNT(DISTINCT employee_id, employee_type, DATE(check_in_date)) FROM attendance_records " +
                            "WHERE branch_id=? AND DATE(check_in_date) BETWEEN ? AND ?",
                    Integer.class, branchId, cursor, end);

            int totalOvertime = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM overtime_requests " +
                            "WHERE branch_id=? AND status='审批成功' AND date BETWEEN ? AND ?",
                    Integer.class, branchId, cursor, end);

            int totalLeave = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM leave_requests " +
                            "WHERE branch_id=? AND status='审批成功' " +
                            "AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?) " +
                            "OR (start_date <= ? AND end_date >= ?))",
                    Integer.class, branchId, cursor, end, cursor, end, cursor, end);

            int employeeCount = countEmployees(branchId);
            int workingDays = calculateWorkingDays(cursor, end);
            int expectedAttendance = employeeCount * workingDays;

            double attendanceRate = expectedAttendance > 0 ?
                    (double) totalAttendance / expectedAttendance * 100 : 0;

            Map<String, Object> point = new HashMap<>();
            point.put("label", label);
            point.put("totalAttendance", totalAttendance);
            point.put("totalOvertime", totalOvertime);
            point.put("totalLeave", totalLeave);
            point.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            point.put("totalWorkHours", (totalAttendance + totalOvertime) * 8);

            list.add(point);

            cursor = end.plusDays(1);
        }

        return Map.of("status", "success", "data", list);
    }

    /* ---------- 5. 批量导入导出功能 ---------- */

    @GetMapping("/{id}/employees/export")
    public ResponseEntity<InputStreamResource> exportEmployees(@PathVariable Integer id) {
        BranchManager bm = branchManagerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("店长不存在"));
        int branchId = bm.getBranchId();

        List<Map<String, Object>> employees = getAllEmployeesForExport(branchId);

        StringBuilder sb = new StringBuilder();
        sb.append("姓名,用户名,手机号,邮箱,职位,门店ID,入职日期\n");

        for (Map<String, Object> emp : employees) {
            sb.append(emp.get("name")).append(',')
                    .append(emp.get("username")).append(',')
                    .append(emp.get("phone")).append(',')
                    .append(emp.get("email")).append(',')
                    .append(emp.get("role")).append(',')
                    .append(emp.get("branchId")).append(',')
                    .append(emp.get("hireDate")).append('\n');
        }

        String bom = "\uFEFF";  // UTF-8 BOM
        byte[] bytes = (bom + sb.toString()).getBytes(StandardCharsets.UTF_8);

        String filename = "employees_branch_" + branchId + ".csv";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    @PostMapping("/{id}/employees/import")
    @Transactional
    public Map<String, Object> importEmployees(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {

        BranchManager bm = branchManagerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("店长不存在"));
        int branchId = bm.getBranchId();

        try {
            // 尝试不同编码读取文件
            String content;
            try {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
                // 移除BOM标记
                if (content.startsWith("\ufeff")) {
                    content = content.substring(1);
                }
            } catch (Exception e) {
                // 如果UTF-8失败，尝试GBK
                content = new String(file.getBytes(), "GBK");
            }

            System.out.println("文件内容预览: " + content.substring(0, Math.min(200, content.length())));

            String[] lines = content.split("\\r?\\n"); // 处理不同的换行符
            System.out.println("总行数: " + lines.length);

            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            // 跳过标题行
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                System.out.println("处理第" + (i+1) + "行: " + line);

                try {
                    String[] fields = line.split(",");
                    System.out.println("字段数量: " + fields.length);
                    for (int j = 0; j < fields.length; j++) {
                        System.out.println("字段" + j + ": " + fields[j]);
                    }

                    if (fields.length < 5) {
                        errors.add("第" + (i+1) + "行：字段不完整，需要至少5个字段，实际只有" + fields.length + "个");
                        failCount++;
                        continue;
                    }

                    // 解析字段：姓名,用户名,手机号,邮箱,职位,门店ID,入职日期
                    String name = fields[0].trim();
                    String username = fields[1].trim();
                    String phone = fields[2].trim();
                    String email = fields[3].trim();
                    String position = fields[4].trim();

                    System.out.println("解析的数据 - 姓名: " + name + ", 用户名: " + username + ", 职位: " + position);

                    // 验证必填字段
                    if (name.isEmpty() || username.isEmpty() || position.isEmpty()) {
                        errors.add("第" + (i+1) + "行：姓名、用户名、职位不能为空");
                        failCount++;
                        continue;
                    }

                    // 检查用户名是否已存在
                    if (isUsernameExists(username)) {
                        errors.add("第" + (i+1) + "行：用户名 " + username + " 已存在");
                        failCount++;
                        continue;
                    }

                    // 根据职位创建对应员工
                    boolean created = createEmployee(name, username, phone, email, position, branchId);
                    if (created) {
                        System.out.println("成功创建员工: " + name);
                        successCount++;
                    } else {
                        errors.add("第" + (i+1) + "行：创建员工失败，可能是职位类型不支持");
                        failCount++;
                    }

                } catch (Exception e) {
                    System.err.println("处理第" + (i+1) + "行时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    errors.add("第" + (i+1) + "行：" + e.getMessage());
                    failCount++;
                }
            }

            System.out.println("导入完成 - 成功: " + successCount + ", 失败: " + failCount);

            return Map.of(
                    "status", "success",
                    "message", "导入完成",
                    "data", Map.of(
                            "successCount", successCount,
                            "failCount", failCount,
                            "errors", errors
                    )
            );

        } catch (Exception e) {
            System.err.println("文件处理失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("status", "error", "message", "文件处理失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户名是否已存在
     */
    private boolean isUsernameExists(String username) {
        try {
            int count = 0;
            count += jdbc.queryForObject("SELECT COUNT(*) FROM chefs WHERE username=?", Integer.class, username);
            count += jdbc.queryForObject("SELECT COUNT(*) FROM waiters WHERE username=?", Integer.class, username);
            count += jdbc.queryForObject("SELECT COUNT(*) FROM counters WHERE username=?", Integer.class, username);
            count += jdbc.queryForObject("SELECT COUNT(*) FROM hr_managers WHERE username=?", Integer.class, username);
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据职位创建员工
     */
    private boolean createEmployee(String name, String username, String phone, String email, String position, int branchId) {
        try {
            // 生成默认密码哈希（密码为123456）
            String defaultPassword = encoder.encode("123456");
            LocalDate hireDate = LocalDate.now();

            String sql;
            // 修复switch语法，使用传统的case语句
            switch (position) {
                case "厨师":
                case "后厨":
                    sql = "INSERT INTO chefs (username, password_hash, name, phone, email, branch_id, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    break;
                case "服务员":
                    sql = "INSERT INTO waiters (username, password_hash, name, phone, email, branch_id, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    break;
                case "收银员":
                case "前台":
                    sql = "INSERT INTO counters (username, password_hash, name, phone, email, branch_id, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    break;
                case "HR":
                    sql = "INSERT INTO hr_managers (username, password_hash, name, phone, email, branch_id, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    break;
                default:
                    System.out.println("未知职位类型: " + position);
                    return false;
            }

            System.out.println("准备插入员工: " + name + ", 用户名: " + username + ", 职位: " + position);
            System.out.println("SQL: " + sql);

            int result = jdbc.update(sql, username, defaultPassword, name, phone, email, branchId, hireDate);
            System.out.println("插入结果: " + result);

            return result > 0;

        } catch (Exception e) {
            System.err.println("创建员工失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /* ---------- 6. 考勤统计CSV导出 ---------- */

    @GetMapping("/{id}/attendance-stats/export")
    public ResponseEntity<InputStreamResource> exportAttendanceStats(
            @PathVariable Integer id,
            @RequestParam String month) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>)
                getAttendanceStats(id, month).get("data");

        StringBuilder sb = new StringBuilder();
        sb.append("员工姓名,应上班天数,实际出勤天数,请假天数,旷工天数,加班次数,出勤率(%),工时统计(小时)\n");

        for (Map<String, Object> stats : data) {
            sb.append(stats.get("employeeName")).append(',')
                    .append(stats.get("shouldWorkDays")).append(',')
                    .append(stats.get("actualWorkDays")).append(',')
                    .append(stats.get("leaveDays")).append(',')
                    .append(stats.get("absentDays")).append(',')
                    .append(stats.get("overtimeCount")).append(',')
                    .append(stats.get("attendanceRate")).append(',')
                    .append(stats.get("totalWorkHours")).append('\n');
        }

        String bom = "\uFEFF";  // UTF-8 BOM
        byte[] bytes = (bom + sb.toString()).getBytes(StandardCharsets.UTF_8);


        String filename = "attendance_stats_" + month + ".csv";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    @GetMapping("/{id}/attendance-history/export")
    public ResponseEntity<InputStreamResource> exportAttendanceHistory(
            @PathVariable Integer id,
            @RequestParam String granularity,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso =
                    org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso =
                    org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate to) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>)
                getAttendanceHistory(id, granularity, from, to).get("data");

        StringBuilder sb = new StringBuilder();
        sb.append("时间段,总出勤次数,总加班次数,总请假次数,出勤率(%),总工时(小时)\n");

        for (Map<String, Object> point : data) {
            sb.append(point.get("label")).append(',')
                    .append(point.get("totalAttendance")).append(',')
                    .append(point.get("totalOvertime")).append(',')
                    .append(point.get("totalLeave")).append(',')
                    .append(point.get("attendanceRate")).append(',')
                    .append(point.get("totalWorkHours")).append('\n');
        }

        String bom = "\uFEFF";  // UTF-8 BOM
        byte[] bytes = (bom + sb.toString()).getBytes(StandardCharsets.UTF_8);


        String filename = "attendance_history_" + granularity + "_" + from + "_to_" + to + ".csv";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    /* ---------- 私有辅助方法 ---------- */

    /**
     * 计算指定门店的员工总数
     */
    private int countEmployees(int branchId) {
        return jdbc.queryForObject(
                "SELECT " +
                        "(SELECT COUNT(*) FROM chefs WHERE branch_id=?) +" +
                        "(SELECT COUNT(*) FROM waiters WHERE branch_id=?) +" +
                        "(SELECT COUNT(*) FROM counters WHERE branch_id=?) +" +
                        "(SELECT COUNT(*) FROM hr_managers WHERE branch_id=?)",
                Integer.class, branchId, branchId, branchId, branchId);
    }

    /**
     * 获取指定门店的所有员工信息（仅获取基本信息用于考勤统计）
     */
    private List<Map<String, Object>> getAllEmployees(int branchId) {
        List<Map<String, Object>> employees = new ArrayList<>();

        // 查询厨师
        List<Map<String, Object>> chefs = jdbc.queryForList(
                "SELECT id, name FROM chefs WHERE branch_id=?", branchId);
        for (Map<String, Object> chef : chefs) {
            chef.put("role", "厨师");
        }
        employees.addAll(chefs);

        // 查询服务员
        List<Map<String, Object>> waiters = jdbc.queryForList(
                "SELECT id, name FROM waiters WHERE branch_id=?", branchId);
        for (Map<String, Object> waiter : waiters) {
            waiter.put("role", "服务员");
        }
        employees.addAll(waiters);

        // 查询收银员
        List<Map<String, Object>> counters = jdbc.queryForList(
                "SELECT id, name FROM counters WHERE branch_id=?", branchId);
        for (Map<String, Object> counter : counters) {
            counter.put("role", "收银员");
        }
        employees.addAll(counters);

        // 查询HR经理
        List<Map<String, Object>> hrManagers = jdbc.queryForList(
                "SELECT id, name FROM hr_managers WHERE branch_id=?", branchId);
        for (Map<String, Object> hr : hrManagers) {
            hr.put("role", "HR");
        }
        employees.addAll(hrManagers);

        return employees;
    }

    /**
     * 获取指定门店的所有员工信息（用于导出，包含更多字段）
     */
    private List<Map<String, Object>> getAllEmployeesForExport(int branchId) {
        List<Map<String, Object>> employees = new ArrayList<>();

        // 查询厨师
        List<Map<String, Object>> chefs = jdbc.queryForList(
                "SELECT id, name, username, phone, email, 'chef' as employee_type, '厨师' as role, " +
                        "branch_id as branchId, hire_date as hireDate FROM chefs WHERE branch_id=?", branchId);
        employees.addAll(chefs);

        // 查询服务员
        List<Map<String, Object>> waiters = jdbc.queryForList(
                "SELECT id, name, username, phone, email, 'waiter' as employee_type, '服务员' as role, " +
                        "branch_id as branchId, hire_date as hireDate FROM waiters WHERE branch_id=?", branchId);
        employees.addAll(waiters);

        // 查询收银员
        List<Map<String, Object>> counters = jdbc.queryForList(
                "SELECT id, name, username, phone, email, 'counter' as employee_type, '收银员' as role, " +
                        "branch_id as branchId, hire_date as hireDate FROM counters WHERE branch_id=?", branchId);
        employees.addAll(counters);

        // 查询HR经理
        List<Map<String, Object>> hrManagers = jdbc.queryForList(
                "SELECT id, name, username, phone, email, 'hr' as employee_type, 'HR' as role, " +
                        "branch_id as branchId, hire_date as hireDate FROM hr_managers WHERE branch_id=?", branchId);
        employees.addAll(hrManagers);

        return employees;
    }

    /**
     * 计算指定日期范围内的工作日天数（排除周六周日）
     */
    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    private LocalDate segmentEnd(LocalDate start, String g, LocalDate max) {
        LocalDate end;
        switch (g) {
            case "week":
                end = start.with(DayOfWeek.SUNDAY);
                break;
            case "month":
                end = start.withDayOfMonth(start.lengthOfMonth());
                break;
            case "quarter":
                end = start.plusMonths(3 - ((start.getMonthValue() - 1) % 3))
                        .withDayOfMonth(1).minusDays(1);
                break;
            case "year":
                end = start.withDayOfYear(start.lengthOfYear());
                break;
            default:
                throw new IllegalArgumentException("粒度参数错误");
        }
        return end.isAfter(max) ? max : end;
    }

    private String labelOf(LocalDate d, String g) {
        switch (g) {
            case "week":
                return d.format(DateTimeFormatter.ofPattern("YYYY-'W'ww"));
            case "month":
                return d.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "quarter":
                return "Q" + ((d.getMonthValue() - 1) / 3 + 1) + "-" + d.getYear();
            case "year":
                return String.valueOf(d.getYear());
            default:
                return "";
        }
    }

    /**
     * 将中文角色名称转换为数据库中的employee_type
     */
    private String getEmployeeType(String role) {
        switch (role) {
            case "厨师":
                return "chef";
            case "服务员":
                return "waiter";
            case "收银员":
                return "counter";
            case "HR":
                return "hr";
            default:
                return role.toLowerCase();
        }
    }
}