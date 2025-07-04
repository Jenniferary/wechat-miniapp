package com.example.demo.controller;

import com.example.demo.entity.LeaveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin
public class LeaveRequestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. 员工提交请假申请
    @PostMapping("/apply")
    public Map<String, Object> apply(@RequestBody LeaveRequest req) {
        String sql = """
            INSERT INTO leave_requests (employee_id, employee_type, branch_id, start_date, end_date, reason, status)
            VALUES (?, ?, ?, ?, ?, ?, '待HR审批')
        """;
        int rows = jdbcTemplate.update(sql,
                req.getEmployeeId(), req.getEmployeeType(), req.getBranchId(),
                req.getStartDate(), req.getEndDate(), req.getReason()
        );
        return Map.of("status", rows > 0 ? "success" : "error");
    }

    // 2. 查询当前分店待审批申请（HR或店长），HR可过滤掉自己提交的
    @GetMapping("/by-branch")
    public List<LeaveRequest> getByBranchAndRole(@RequestParam Integer branchId,
                                                 @RequestParam String role,
                                                 @RequestParam(required = false) Integer hrId,
                                                 @RequestParam(required = false) String hrType) {
        String status = role.equals("hr") ? "待HR审批" : "HR审批通过待店长审批";
        String sql;
        Object[] params;
        if ("hr".equals(role) && hrId != null && hrType != null) {
            sql = """
            SELECT * FROM leave_requests 
            WHERE branch_id = ? AND status = ? 
            AND NOT (employee_id = ? AND employee_type = ?) 
            ORDER BY created_at DESC
        """;
            params = new Object[]{branchId, status, hrId, hrType};
        } else {
            sql = "SELECT * FROM leave_requests WHERE branch_id = ? AND status = ? ORDER BY created_at DESC";
            params = new Object[]{branchId, status};
        }
        return jdbcTemplate.query(sql, new LeaveRequestRowMapper(), params);
    }


    // 3. HR 初审：通过或驳回
    @PutMapping("/hr-approve/{id}")
    public Map<String, Object> hrApprove(@PathVariable Integer id,
                                         @RequestParam String decision,
                                         @RequestParam Integer hrId,
                                         @RequestParam String hrType) {
        // 查出该申请记录
        String checkSql = "SELECT * FROM leave_requests WHERE id = ?";
        List<LeaveRequest> list = jdbcTemplate.query(checkSql, new LeaveRequestRowMapper(), id);
        if (list.isEmpty()) {
            return Map.of("status", "error", "message", "请假申请不存在");
        }

        LeaveRequest req = list.get(0);
        if (req.getEmployeeId().equals(hrId) && req.getEmployeeType().equalsIgnoreCase(hrType)) {
            return Map.of("status", "error", "message", "不能审批自己提交的请假申请");
        }

        String newStatus;
        if ("approve".equalsIgnoreCase(decision)) {
            newStatus = "HR审批通过待店长审批";
        } else if ("reject".equalsIgnoreCase(decision)) {
            newStatus = "已驳回";
        } else {
            return Map.of("status", "error", "message", "无效的决策");
        }

        int rows = jdbcTemplate.update("UPDATE leave_requests SET status = ? WHERE id = ?", newStatus, id);
        return Map.of("status", rows > 0 ? "success" : "error");
    }


    // 4. 店长终审
    @PutMapping("/manager-approve/{id}")
    public Map<String, Object> managerApprove(@PathVariable Integer id, @RequestParam String decision) {
        String newStatus;
        if ("approve".equalsIgnoreCase(decision)) {
            newStatus = "审批成功";
        } else if ("reject".equalsIgnoreCase(decision)) {
            newStatus = "已驳回";
        } else {
            return Map.of("status", "error", "message", "无效的决策");
        }
        int rows = jdbcTemplate.update("UPDATE leave_requests SET status = ? WHERE id = ?", newStatus, id);
        return Map.of("status", rows > 0 ? "success" : "error");
    }

    // RowMapper 内部类（处理 null 安全）
    static class LeaveRequestRowMapper implements RowMapper<LeaveRequest> {
        @Override
        public LeaveRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeaveRequest r = new LeaveRequest();
            r.setId(rs.getInt("id"));
            r.setEmployeeId(rs.getInt("employee_id"));
            r.setEmployeeType(rs.getString("employee_type"));
            r.setBranchId(rs.getInt("branch_id"));
            r.setStartDate(rs.getDate("start_date").toLocalDate());
            r.setEndDate(rs.getDate("end_date").toLocalDate());
            r.setReason(rs.getString("reason"));
            r.setStatus(rs.getString("status"));

            // 处理 created_at 和 updated_at 可能为 null 的情况
            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            r.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
            r.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);

            return r;
        }
    }
    // 5. 查询员工请假历史（带分页或全部，方便前端展示自己的请假流程）
    @GetMapping("/history/{employeeId}")
    public Map<String, Object> getLeaveHistory(@PathVariable Integer employeeId,
                                               @RequestParam String employeeType) {
        String sql = "SELECT * FROM leave_requests WHERE employee_id = ? AND employee_type = ? ORDER BY created_at DESC";
        List<LeaveRequest> list = jdbcTemplate.query(sql, new LeaveRequestRowMapper(), employeeId, employeeType);
        return Map.of("status", "success", "data", Map.of("records", list));
    }

}