package com.example.demo.controller;

import com.example.demo.entity.LeavingWorkingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaving-working")
@CrossOrigin
public class LeavingWorkingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. 员工提交离职申请
    // 修改后的离职申请插入方法，增加传入的 username 和 name
    @PostMapping("/apply")
    @Transactional
    public Map<String, Object> applyLeavingRequest(@RequestBody LeavingWorkingRequest req) {
        // 查询 chefs 表，获取离职员工的姓名
        String sqlGetName = "SELECT username FROM chefs WHERE id = ?";
        String employeeName = jdbcTemplate.queryForObject(sqlGetName, String.class, req.getEmployeeId());

        // 插入离职申请记录，包含员工姓名
        String sqlInsertRequest = """
        INSERT INTO leaving_working_requests (employee_id, employee_type, branch_id, reason, status, name)
        VALUES (?, ?, ?, ?, '已提交待HR审批', ?)
    """;
        int rows = jdbcTemplate.update(sqlInsertRequest,
                req.getEmployeeId(), req.getEmployeeType(), req.getBranchId(),
                req.getReason(), employeeName);  // 将查询到的员工姓名插入

        return Map.of("status", rows > 0 ? "success" : "error");
    }



    // 2. HR审批离职申请
    @PutMapping("/{id}/{decision}")
    @Transactional
    public Map<String, String> handleLeaveRequestDecision(
            @PathVariable Integer id,
            @PathVariable String decision) {

        // 校验传入的 decision 是否有效
        if (!"approve".equals(decision) && !"reject".equals(decision)) {
            return Map.of("status", "error", "message", "无效的操作");
        }

        String newStatus;
        if ("approve".equals(decision)) {
            newStatus = "HR审批通过待店长审批";  // 修改为审批通过待店长审批
        } else {
            newStatus = "已驳回";  // 修改为已驳回
        }

        // 更新离职申请状态
        String sql = "UPDATE leaving_working_requests SET status = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, newStatus, id);

        if (rows > 0) {
            return Map.of("status", "success", "message", "操作成功");
        } else {
            return Map.of("status", "error", "message", "离职申请更新失败");
        }
    }


    // 3. 店长审批离职申请
    @PutMapping("/{id}/manager-approve")
    @Transactional
    public Map<String, String> managerApproveRequest(@PathVariable Integer id) {
        String sql = "UPDATE leaving_working_requests SET status = '审批成功' WHERE id = ? AND status = 'HR审批通过待店长审批'";
        int rows = jdbcTemplate.update(sql, id);
        if (rows > 0) {
            return Map.of("status", "success", "message", "店长审批通过，离职申请完成");
        } else {
            return Map.of("status", "error", "message", "申请状态不符合，无法进行店长审批");
        }
    }

    // 4. 离职申请被驳回
    @PutMapping("/{id}/reject")
    @Transactional
    public Map<String, String> rejectRequest(@PathVariable Integer id) {
        String sql = "UPDATE leaving_working_requests SET status = '已驳回' WHERE id = ? AND (status = '已提交待HR审批' OR status = 'HR审批通过待店长审批')";
        int rows = jdbcTemplate.update(sql, id);
        if (rows > 0) {
            return Map.of("status", "success", "message", "离职申请已被驳回");
        } else {
            return Map.of("status", "error", "message", "申请状态不符合，无法驳回");
        }
    }

    // 5. 获取所有离职申请
    @GetMapping("/all")
    public List<LeavingWorkingRequest> getAllLeaveRequests() {
        return jdbcTemplate.query("SELECT * FROM leaving_working_requests",
                (rs, rowNum) -> {
                    LeavingWorkingRequest request = new LeavingWorkingRequest();
                    request.setId(rs.getInt("id"));
                    request.setEmployeeId(rs.getInt("employee_id"));
                    request.setEmployeeType(rs.getString("employee_type"));
                    request.setBranchId(rs.getInt("branch_id"));
                    request.setReason(rs.getString("reason"));
                    request.setStatus(rs.getString("status"));
                    request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return request;
                });
    }
    @GetMapping("/by-branch")
    public List<LeavingWorkingRequest> getByBranchId(@RequestParam Integer branchId) {
        String sql = """
        SELECT l.*, e.username AS name
        FROM leaving_working_requests l
        JOIN chefs e ON l.employee_id = e.id
        WHERE l.branch_id = ? AND l.status = '已提交待HR审批'
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LeavingWorkingRequest request = new LeavingWorkingRequest();
            request.setId(rs.getInt("id"));
            request.setEmployeeId(rs.getInt("employee_id"));
            request.setEmployeeType(rs.getString("employee_type"));
            request.setBranchId(rs.getInt("branch_id"));
            request.setReason(rs.getString("reason"));
            request.setStatus(rs.getString("status"));
            request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            request.setName(rs.getString("name"));  // 获取员工姓名
            return request;
        }, branchId);
    }


    // 6. 获取某个员工的所有离职申请
    @GetMapping("/by-applicant")
    public List<LeavingWorkingRequest> getByApplicantId(@RequestParam Integer applicantId) {
        return jdbcTemplate.query("SELECT * FROM leaving_working_requests WHERE employee_id = ?",
                (rs, rowNum) -> {
                    LeavingWorkingRequest request = new LeavingWorkingRequest();
                    request.setId(rs.getInt("id"));
                    request.setEmployeeId(rs.getInt("employee_id"));
                    request.setEmployeeType(rs.getString("employee_type"));
                    request.setBranchId(rs.getInt("branch_id"));
                    request.setReason(rs.getString("reason"));
                    request.setStatus(rs.getString("status"));
                    request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return request;
                }, applicantId);
    }
    @GetMapping("/by-manager")
    public List<LeavingWorkingRequest> getByManagerId(@RequestParam Integer managerId) {
        // 获取店长的分支 ID
        String branchIdSql = "SELECT branch_id FROM branch_managers WHERE id = ?";
        Integer branchId = jdbcTemplate.queryForObject(branchIdSql, Integer.class, managerId);

        if (branchId == null) {
            return List.of();  // 如果没有找到分支 ID，则返回空列表
        }

        // 获取该分支下的待审批离职申请
        String sql = """
            SELECT l.*, e.username AS name
            FROM leaving_working_requests l
            JOIN chefs e ON l.employee_id = e.id
            WHERE l.branch_id = ? AND l.status = 'HR审批通过待店长审批'
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LeavingWorkingRequest request = new LeavingWorkingRequest();
            request.setId(rs.getInt("id"));
            request.setEmployeeId(rs.getInt("employee_id"));
            request.setEmployeeType(rs.getString("employee_type"));
            request.setBranchId(rs.getInt("branch_id"));
            request.setReason(rs.getString("reason"));
            request.setStatus(rs.getString("status"));
            request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            request.setName(rs.getString("name"));  // 获取员工姓名
            return request;
        }, branchId);
    }

    @GetMapping("/progress-by-manager")
    public List<Map<String, Object>> getApprovalProgressByManager(@RequestParam Integer managerId) {
        // 获取店长的分支ID
        String branchIdSql = "SELECT branch_id FROM branch_managers WHERE id = ?";
        Integer branchId = jdbcTemplate.queryForObject(branchIdSql, Integer.class, managerId);

        if (branchId == null) {
            return List.of();  // 如果没有找到分支ID，则返回空列表
        }

        // 获取该分支下的所有离职申请及其状态和进度，包括已离职员工的记录
        String sql = """
        SELECT l.id, l.name, l.reason, l.created_at, l.status,
               CASE
                   WHEN l.status = '已提交待HR审批' THEN 25
                   WHEN l.status = 'HR审批通过待店长审批' THEN 50
                   WHEN l.status = '审批成功' THEN 75
                   WHEN l.status = '已驳回' THEN 100
                   WHEN l.status = '已离职' THEN 100
                   ELSE 0
               END AS progress
        FROM leaving_working_requests l
        WHERE l.branch_id = ?
    """;

        return jdbcTemplate.queryForList(sql, branchId);
    }



    @PutMapping("/chef/{id}/confirm-leave")
    @Transactional
    public Map<String, String> confirmLeave(@PathVariable Integer id) {
        // 先删除员工信息
        String deleteChefSql = "DELETE FROM chefs WHERE id = ?";
        int deleteRows = jdbcTemplate.update(deleteChefSql, id);

        if (deleteRows > 0) {
            // 然后更新离职申请状态
            String updateStatusSql = "UPDATE leaving_working_requests SET status = '已离职' WHERE employee_id = ?";
            jdbcTemplate.update(updateStatusSql, id);
            return Map.of("status", "success", "message", "员工离职确认成功");
        } else {
            return Map.of("status", "error", "message", "删除员工信息失败");
        }
    }

}
