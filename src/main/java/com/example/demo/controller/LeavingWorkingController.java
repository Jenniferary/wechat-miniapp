package com.example.demo.controller;

import com.example.demo.entity.LeavingWorkingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/waiter-apply")
    @Transactional
    public Map<String, Object> applyWaiterLeavingRequest(@RequestBody LeavingWorkingRequest req) {
        // 查询 waiters 表，获取离职员工的姓名
        String sqlGetName = "SELECT username FROM waiters WHERE id = ?";
        String employeeName = jdbcTemplate.queryForObject(sqlGetName, String.class, req.getEmployeeId());

        // 插入离职申请记录，包含员工姓名
        String sqlInsertRequest = """
        INSERT INTO leaving_working_requests (employee_id, employee_type, branch_id, reason, status, name)
        VALUES (?, ?, ?, ?, '已提交待HR审批', ?)
        """;
        int rows = jdbcTemplate.update(sqlInsertRequest,
                req.getEmployeeId(), "waiter", req.getBranchId(),
                req.getReason(), employeeName);  // 将查询到的员工姓名插入

        return Map.of("status", rows > 0 ? "success" : "error");
    }
    @PostMapping("/counter-apply")
    @Transactional
    public Map<String, Object> counterApplyLeavingRequest(@RequestBody LeavingWorkingRequest req) {
        try {
            // 插入离职申请记录
            String sqlInsertRequest = """
            INSERT INTO leaving_working_requests (employee_id, employee_type, branch_id, reason, status, name)
            VALUES (?, ?, ?, ?, '已提交待HR审批', ?)
            """;
            int rows = jdbcTemplate.update(sqlInsertRequest,
                    req.getEmployeeId(), req.getEmployeeType(), req.getBranchId(),
                    req.getReason(), req.getName());  // 使用 name 字段保存员工姓名

            if (rows > 0) {
                return Map.of("status", "success", "message", "离职申请已提交");
            } else {
                return Map.of("status", "error", "message", "提交离职申请失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", "error", "message", "系统错误", "details", e.getMessage());
        }
    }
    @PostMapping("/hr-apply")
    @Transactional
    public Map<String, Object> hrApplyLeavingRequest(@RequestBody LeavingWorkingRequest req) {
        try {
            // 插入离职申请记录
            String sqlInsertRequest = """
            INSERT INTO leaving_working_requests (employee_id, employee_type, branch_id, reason, status, name)
            VALUES (?, ?, ?, ?, 'HR审批通过待店长审批', ?)
        """;
            int rows = jdbcTemplate.update(sqlInsertRequest,
                    req.getEmployeeId(), req.getEmployeeType(), req.getBranchId(),
                    req.getReason(), req.getName());  // 使用 name 字段保存HR的姓名

            if (rows > 0) {
                return Map.of("status", "success", "message", "离职申请已提交，等待店长审批");
            } else {
                return Map.of("status", "error", "message", "提交离职申请失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", "error", "message", "系统错误", "details", e.getMessage());
        }
    }


    @GetMapping("/waiter-by-applicant")
    public Map<String, Object> getWaiterLeavingRequestsByApplicant(@RequestParam Integer applicantId) {
        List<LeavingWorkingRequest> requests = jdbcTemplate.query(
                "SELECT * FROM leaving_working_requests WHERE employee_id = ? AND employee_type = 'waiter'",
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

                    // 获取并返回 name 字段
                    String name = rs.getString("name");
                    request.setName(name != null ? name : "未设置");

                    return request;
                }, applicantId);

        if (requests.isEmpty()) {
            return Map.of("status", "success", "message", "没有找到离职申请记录", "data", requests);
        }

        return Map.of("status", "success", "data", requests);
    }
    @GetMapping("/counter-by-applicant")
    public Map<String, Object> getCounterLeavingRequestsByApplicant(@RequestParam Integer applicantId) {
        // 确保这个 GET 请求能够正确地处理
        List<LeavingWorkingRequest> requests = jdbcTemplate.query(
                "SELECT * FROM leaving_working_requests WHERE employee_id = ? AND employee_type = 'counter'",
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

                    String name = rs.getString("name");
                    request.setName(name != null ? name : "未设置");

                    return request;
                }, applicantId);

        if (requests.isEmpty()) {
            return Map.of("status", "success", "message", "没有找到离职申请记录", "data", requests);
        }

        return Map.of("status", "success", "data", requests);
    }
    @GetMapping("/hr-by-applicant")
    public Map<String, Object> getHrLeavingRequestsByApplicant(@RequestParam Integer applicantId) {
        List<LeavingWorkingRequest> requests = jdbcTemplate.query(
                "SELECT * FROM leaving_working_requests WHERE employee_id = ? AND employee_type = 'hr'",
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

                    String name = rs.getString("name");
                    request.setName(name != null ? name : "未设置");

                    return request;
                }, applicantId);

        if (requests.isEmpty()) {
            return Map.of("status", "success", "message", "没有找到离职申请记录", "data", requests);
        }

        return Map.of("status", "success", "data", requests);
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
        try {
            // 修改 SQL 查询，直接从 leaving_working_requests 表获取 name 字段
            String sql = """
        SELECT l.id, l.employee_id, l.employee_type, l.name, l.branch_id, l.reason, l.status, l.created_at, l.updated_at
        FROM leaving_working_requests l
        WHERE l.branch_id = ? AND l.status = '已提交待HR审批'
        """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                LeavingWorkingRequest request = new LeavingWorkingRequest();
                request.setId(rs.getInt("id"));
                request.setEmployeeId(rs.getInt("employee_id"));
                request.setEmployeeType(rs.getString("employee_type"));
                request.setName(rs.getString("name"));  // 获取员工姓名
                request.setBranchId(rs.getInt("branch_id"));
                request.setReason(rs.getString("reason"));
                request.setStatus(rs.getString("status"));
                request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return request;
            }, branchId);

        } catch (Exception e) {
            e.printStackTrace();
            // 返回一个错误响应
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database query failed", e);
        }
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
    public ResponseEntity<Map<String, Object>> getByManagerId(@RequestParam Integer managerId) {
        try {
            String branchIdSql = "SELECT branch_id FROM branch_managers WHERE id = ?";
            Integer branchId = jdbcTemplate.queryForObject(branchIdSql, Integer.class, managerId);

            if (branchId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "未找到店长对应分支"));
            }

            String sql = """
            SELECT l.id, l.employee_id, l.employee_type, l.name, l.branch_id, l.reason, l.status, l.created_at, l.updated_at
            FROM leaving_working_requests l
            WHERE l.branch_id = ? AND l.status = 'HR审批通过待店长审批'
        """;
            List<LeavingWorkingRequest> requests = jdbcTemplate.query(sql, (rs, rowNum) -> {
                LeavingWorkingRequest request = new LeavingWorkingRequest();
                request.setId(rs.getInt("id"));
                request.setEmployeeId(rs.getInt("employee_id"));
                request.setEmployeeType(rs.getString("employee_type"));
                request.setName(rs.getString("name"));
                request.setBranchId(rs.getInt("branch_id"));
                request.setReason(rs.getString("reason"));
                request.setStatus(rs.getString("status"));
                request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return request;
            }, branchId);

            return ResponseEntity.ok(Map.of("status", "success", "data", requests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "数据库查询失败", "details", e.getMessage()));
        }
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

    @DeleteMapping("/waiters/delete-by-id/{id}")
    public Map<String, String> deleteWaiterById(@PathVariable Integer id) {
        try {
            // 确保接收到的 id 是整数类型
            System.out.println("Received waiterId: " + id);

            // 执行删除操作
            String deleteWaiterSql = "DELETE FROM waiters WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(deleteWaiterSql, id);

            if (rowsAffected > 0) {
                return Map.of("status", "success", "message", "员工已删除");
            } else {
                return Map.of("status", "error", "message", "未找到员工");
            }

        } catch (Exception e) {
            e.printStackTrace();  // 打印异常堆栈
            return Map.of("status", "error", "message", "删除操作失败", "details", e.getMessage());
        }
    }

    @DeleteMapping("/counters/delete-by-id/{id}")
    public Map<String, String> deleteCounterById(@PathVariable Integer id) {
        try {
            // 确保接收到的 id 是整数类型
            System.out.println("Received counterId: " + id);

            // 执行删除操作
            String deleteCounterSql = "DELETE FROM counters WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(deleteCounterSql, id);

            if (rowsAffected > 0) {
                return Map.of("status", "success", "message", "员工已删除");
            } else {
                return Map.of("status", "error", "message", "未找到员工");
            }

        } catch (Exception e) {
            e.printStackTrace();  // 打印异常堆栈
            return Map.of("status", "error", "message", "删除操作失败", "details", e.getMessage());
        }
    }

    @DeleteMapping("/hr-confirm-leave/{hrId}/{requestId}")
    @Transactional
    public Map<String, String> confirmHrLeave(@PathVariable Integer hrId, @PathVariable Integer requestId) {
        try {
            // 确认删除HR的离职记录
            String deleteHrSql = "DELETE FROM hr_managers WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(deleteHrSql, hrId);

            if (rowsAffected > 0) {
                // 更新离职申请状态为已离职
                String updateStatusSql = "UPDATE leaving_working_requests SET status = '已离职' WHERE id = ?";
                jdbcTemplate.update(updateStatusSql, requestId);
                return Map.of("status", "success", "message", "HR离职确认成功");
            } else {
                return Map.of("status", "error", "message", "未找到HR或删除失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", "error", "message", "系统错误", "details", e.getMessage());
        }
    }

}
