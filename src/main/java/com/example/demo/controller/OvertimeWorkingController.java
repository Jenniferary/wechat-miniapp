package com.example.demo.controller;

import com.example.demo.entity.OvertimeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/overtime")
@CrossOrigin(origins = "http://localhost:8080")
public class OvertimeWorkingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. 员工提交加班申请
    @PostMapping("/apply")
    public Map<String, Object> apply(@RequestBody OvertimeRequest req) {
        // 校验日期是否为周六或周日
        LocalDate date = req.getDate(); // 获取提交的加班日期
        if (date.getDayOfWeek().getValue() != 6 && date.getDayOfWeek().getValue() != 7) {
            return Map.of("status", "error", "message", "加班日期只能选择周六或周日！");
        }

        // 插入加班申请数据
        String sql = """
            INSERT INTO overtime_requests (employee_id, employee_type, branch_id, date, reason, status)
            VALUES (?, ?, ?, ?, ?, '待HR审批')
        """;
        int rows = jdbcTemplate.update(sql,
                req.getEmployeeId(), req.getEmployeeType(), req.getBranchId(),
                req.getDate().toString(), req.getReason());  // 将 LocalDate 转换为 String

        return Map.of("status", rows > 0 ? "success" : "error");
    }

    @PostMapping("/hr-apply")
    public Map<String, Object> hrapply(@RequestBody OvertimeRequest req) {
        // 校验日期是否为周六或周日
        LocalDate date = req.getDate(); // 获取提交的加班日期
        if (date.getDayOfWeek().getValue() != 6 && date.getDayOfWeek().getValue() != 7) {
            return Map.of("status", "error", "message", "加班日期只能选择周六或周日！");
        }

        // 设置加班申请状态为 HR审批通过待店长审批
        String sql = """
            INSERT INTO overtime_requests (employee_id, employee_type, branch_id, date, reason, status)
            VALUES (?, ?, ?, ?, ?, 'HR审批通过待店长审批')
        """;
        int rows = jdbcTemplate.update(sql,
                req.getEmployeeId(), req.getEmployeeType(), req.getBranchId(),
                req.getDate().toString(), req.getReason());  // 将 LocalDate 转换为 String

        return Map.of("status", rows > 0 ? "success" : "error");
    }
    @GetMapping("/by-employee")
    public Map<String, Object> getOvertimeRequestsByEmployee(
            @RequestParam("employee_id") Integer employeeId,
            @RequestParam("employee_type") String employeeType) {
        String sql = "SELECT * FROM overtime_requests WHERE employee_id = ? AND employee_type = ? ORDER BY date DESC";
        System.out.println("Received employee_id: " + employeeId + ", employee_type: " + employeeType);

        try {
            List<OvertimeRequest> overtimeRequests = jdbcTemplate.query(sql, new OvertimeRequestRowMapper(), employeeId, employeeType);
            return Map.of("status", "success", "data", Map.of("records", overtimeRequests));
        } catch (Exception e) {
            return Map.of("status", "error", "message", "查询加班记录失败: " + e.getMessage());
        }
    }


    // 2. 查询当前分店待审批加班申请（HR或店长），HR可过滤掉自己提交的
    @GetMapping("/by-branch")
    public List<OvertimeRequest> getByBranchAndRole(@RequestParam Integer branchId,
                                                    @RequestParam String role) {
        // 根据角色设置加班申请的状态
        String status = role.equals("hr") ? "待HR审批" : "HR审批通过待店长审批";

        String sql;
        Object[] params;

        if ("hr".equals(role)) {
            // HR查询加班申请，不包括自己提交的加班记录
            sql = """
            SELECT * FROM overtime_requests 
            WHERE branch_id = ? AND status = ? 
            AND employee_type != 'hr'  -- 不包括HR提交的记录
            ORDER BY created_at DESC
        """;
            params = new Object[]{branchId, status};
        } else {
            // 店长查询加班申请，只显示状态为HR审批通过待店长审批的记录
            sql = "SELECT * FROM overtime_requests WHERE branch_id = ? AND status = ?";
            params = new Object[]{branchId, status};
        }

        return jdbcTemplate.query(sql, new OvertimeRequestRowMapper(), params);
    }

    // 查询所有加班申请记录
    @GetMapping("/approval-history")
    public Map<String, Object> getOvertimeApprovalHistory(@RequestParam Integer branchId) {
        String sql = "SELECT * FROM overtime_requests WHERE branch_id = ? ORDER BY created_at DESC";

        try {
            List<OvertimeRequest> overtimeRequests = jdbcTemplate.query(sql, new Object[]{branchId}, new OvertimeRequestRowMapper());
            return Map.of("status", "success", "data", Map.of("records", overtimeRequests));
        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            return Map.of("status", "error", "message", "查询加班审批记录失败: " + e.getMessage());
        }
    }

    @PutMapping("/hr-approve/{requestId}")
    public Map<String, Object> hrApprove(@PathVariable Integer requestId,
                                         @RequestParam String decision,
                                         @RequestParam Integer hrId,
                                         @RequestParam String hrType) {
        // 根据 requestId 获取加班申请记录
        String sql = "SELECT * FROM overtime_requests WHERE request_id = ?";

        try {
            // 打印 SQL 查询语句和传递的参数
            System.out.println("Executing SQL: " + sql);
            System.out.println("With requestId: " + requestId);

            // 执行查询
            OvertimeRequest req = jdbcTemplate.queryForObject(sql, new Object[]{requestId}, new OvertimeRequestRowMapper());

            if (req == null) {
                return Map.of("status", "error", "message", "加班申请不存在");
            }

            // 确保 HR 无法审批自己提交的申请
            if (req.getEmployeeId().equals(hrId) && req.getEmployeeType().equalsIgnoreCase(hrType)) {
                return Map.of("status", "error", "message", "不能审批自己提交的加班申请");
            }

            // 根据审批决策更新状态
            String newStatus = "approve".equalsIgnoreCase(decision) ? "HR审批通过待店长审批" : "已驳回";

            // 更新加班申请状态
            int rows = jdbcTemplate.update("UPDATE overtime_requests SET status = ? WHERE request_id = ?", newStatus, requestId);
            return Map.of("status", rows > 0 ? "success" : "error");
        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            return Map.of("status", "error", "message", "查询加班记录失败: " + e.getMessage());
        }
    }

    // 4. 店长终审
    @PutMapping("/manager-approve/{requestId}")
    public Map<String, Object> managerApprove(@PathVariable Integer requestId, @RequestParam String decision) {
        String newStatus;
        if ("approve".equalsIgnoreCase(decision)) {
            newStatus = "审批成功";
        } else if ("reject".equalsIgnoreCase(decision)) {
            newStatus = "已驳回";
        } else {
            return Map.of("status", "error", "message", "无效的决策");
        }

        // 使用 request_id 进行查询和更新
        int rows = jdbcTemplate.update("UPDATE overtime_requests SET status = ? WHERE request_id = ?", newStatus, requestId);

        // 返回操作结果
        return Map.of("status", rows > 0 ? "success" : "error");
    }


    // RowMapper 内部类（处理 null 安全）
    static class OvertimeRequestRowMapper implements RowMapper<OvertimeRequest> {
        @Override
        public OvertimeRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            OvertimeRequest r = new OvertimeRequest();
            r.setId(rs.getInt("request_id"));
            r.setEmployeeId(rs.getInt("employee_id"));
            r.setEmployeeType(rs.getString("employee_type"));
            r.setBranchId(rs.getInt("branch_id"));
            r.setDate(rs.getDate("date").toLocalDate());  // 将 SQL DATE 类型转换为 LocalDate
            r.setReason(rs.getString("reason"));
            r.setStatus(rs.getString("status"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            r.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
            r.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);

            return r;
        }
    }
}
