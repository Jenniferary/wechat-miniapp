package com.example.demo.controller;

import com.example.demo.entity.AttendanceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    @Autowired
    private JdbcTemplate jdbc;

    /* ---------- 1. 打卡 ---------- */
    @PostMapping("/checkin")
    public Map<String,Object> checkIn(@RequestBody AttendanceRecord dto) {

        // 判断当天是否已有记录
        String sel = """
            SELECT id FROM attendance_records
            WHERE employee_id=? AND check_in_date=CURDATE()
        """;
        Integer id = jdbc.query(sel, rs -> rs.next() ? rs.getInt("id") : null,
                dto.getEmployeeId());

        if (id != null) {
            return Map.of("status","error","message","今日已打过卡");
        }

        String ins = """
            INSERT INTO attendance_records
              (employee_id, employee_type, branch_id,
               check_in_time, check_in_lat, check_in_lng, check_in_status,
               status)
            VALUES (?,?,?,?,?,?,?,?)
        """;
        jdbc.update(ins,
                dto.getEmployeeId(), dto.getEmployeeType(), dto.getBranchId(),
                dto.getCheckInTime(), dto.getCheckInLat(), dto.getCheckInLng(), dto.getCheckInStatus(),
                "normal");

        return Map.of("status","success","message","打卡成功");
    }

    /* ---------- 2. 签退 ---------- */
    @PostMapping("/checkout")
    public Map<String,Object> checkOut(@RequestBody AttendanceRecord dto) {

        // 查找当天记录
        String sel = """
            SELECT id, check_out_time FROM attendance_records
            WHERE employee_id=? AND check_in_date=CURDATE()
        """;
        AttendanceRecord rec = jdbc.query(sel, rs -> {
            if (rs.next()) {
                AttendanceRecord r = new AttendanceRecord();
                r.setId(rs.getInt("id"));
                r.setCheckOutTime(rs.getTimestamp("check_out_time") != null
                        ? rs.getTimestamp("check_out_time").toLocalDateTime()
                        : null);
                return r;
            }
            return null;
        }, dto.getEmployeeId());

        if (rec == null) {
            return Map.of("status","error","message","今日未打卡，无法签退");
        }
        if (rec.getCheckOutTime() != null) {
            return Map.of("status","error","message","今日已签退，请勿重复操作");
        }

        String upd = """
            UPDATE attendance_records SET
              check_out_time=?, check_out_lat=?, check_out_lng=?, check_out_status=?,
              updated_at=NOW()
            WHERE id=?
        """;
        jdbc.update(upd,
                dto.getCheckOutTime(), dto.getCheckOutLat(), dto.getCheckOutLng(), dto.getCheckOutStatus(),
                rec.getId());

        return Map.of("status","success","message","签退成功");
    }

    /* ---------- 3. 历史 ---------- */
    @GetMapping("/history/{empId}")
    public Map<String,Object> history(@PathVariable Integer empId,
                                      @RequestParam String employeeType) {

        // 可选：查询入职日期（示例 HR 表）
        LocalDate hireDate;
        try {
            hireDate = jdbc.queryForObject(
                    "SELECT hire_date FROM hr_managers WHERE id=?", LocalDate.class, empId);
        } catch (EmptyResultDataAccessException ex) {
            hireDate = null;
        }

        String sql = """
            SELECT * FROM attendance_records
            WHERE employee_id=? AND employee_type=?
            ORDER BY check_in_time DESC
        """;
        List<AttendanceRecord> list = jdbc.query(sql,
                new BeanPropertyRowMapper<>(AttendanceRecord.class), empId, employeeType);

        return Map.of("status","success",
                "data", Map.of("hireDate", hireDate, "records", list));
    }
}
