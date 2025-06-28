package com.example.demo.controller;

import com.example.demo.entity.AttendanceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    @Autowired
    private JdbcTemplate jdbc;

    // 新增打卡记录
    @PostMapping("/checkin")
    public Map<String, Object> checkIn(@RequestBody AttendanceRecord record) {
        String sql = "INSERT INTO attendance_records (employee_id, employee_type, branch_id, check_in_time, latitude, longitude, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        int rows = jdbc.update(sql,
                record.getEmployeeId(),
                record.getEmployeeType(),
                record.getBranchId(),
                record.getCheckInTime(),
                record.getLatitude(),
                record.getLongitude(),
                record.getStatus() == null ? "normal" : record.getStatus());

        if (rows > 0) {
            return Map.of("status", "success", "message", "打卡成功");
        } else {
            return Map.of("status", "error", "message", "打卡失败");
        }
    }

    // 查询某员工的打卡记录列表，按时间降序
    @GetMapping("/records")
    public Map<String, Object> getRecords(@RequestParam Integer employeeId, @RequestParam String employeeType) {
        String sql = "SELECT * FROM attendance_records WHERE employee_id = ? AND employee_type = ? ORDER BY check_in_time DESC";

        List<AttendanceRecord> list = jdbc.query(sql, new Object[]{employeeId, employeeType}, new AttendanceRowMapper());

        return Map.of("status", "success", "data", list);
    }

    private static class AttendanceRowMapper implements RowMapper<AttendanceRecord> {
        @Override
        public AttendanceRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            AttendanceRecord record = new AttendanceRecord();
            record.setId(rs.getInt("id"));
            record.setEmployeeId(rs.getInt("employee_id"));
            record.setEmployeeType(rs.getString("employee_type"));
            record.setBranchId(rs.getInt("branch_id"));
            record.setCheckInTime(rs.getTimestamp("check_in_time").toLocalDateTime());
            record.setLatitude(rs.getDouble("latitude"));
            record.setLongitude(rs.getDouble("longitude"));
            record.setStatus(rs.getString("status"));
            return record;
        }
    }
    // 返回入职日期和历史打卡记录接口
    @GetMapping("/history/{employeeId}")
    public Map<String, Object> getAttendanceHistory(
            @PathVariable Integer employeeId,
            @RequestParam String employeeType // ← 新增参数
    ) {
        String hireDateSql = "SELECT hire_date FROM hr_managers WHERE id = ?";
        LocalDate hireDate;

        try {
            hireDate = jdbc.queryForObject(hireDateSql, new Object[]{employeeId}, LocalDate.class);
        } catch (EmptyResultDataAccessException e) {
            hireDate = null;
        }

        String recordsSql = "SELECT id, employee_id, employee_type, branch_id, check_in_time, latitude, longitude, status " +
                "FROM attendance_records WHERE employee_id = ? AND employee_type = ? ORDER BY check_in_time ASC";

        List<AttendanceRecord> records = jdbc.query(recordsSql, new Object[]{employeeId, employeeType}, new AttendanceRowMapper());

        Map<String, Object> data = new HashMap<>();
        data.put("hireDate", hireDate != null ? hireDate.toString() : null);
        data.put("records", records);

        return Map.of("status", "success", "data", data);
    }

}
