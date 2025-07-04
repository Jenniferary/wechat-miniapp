package com.example.demo.controller;

import com.example.demo.entity.Salary;
import com.example.demo.entity.SalaryConfig;
import com.example.demo.repository.SalaryConfigRepository;
import com.example.demo.repository.SalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryRepository       salaryRepo;
    private final SalaryConfigRepository cfgRepo;
    private final JdbcTemplate           jdbc;

    /** 每 1 分绩效兑换 5 元，可改为读取配置 */
    private static final BigDecimal PERFORMANCE_RATE = BigDecimal.valueOf(2);

    // ───────────────────────────────────────────────────────────────
    // 生成 / 更新 单人单月工资
    // POST /api/salaries/generate?employeeId=1&employeeType=chef&branchId=1&month=2025-06
    // ───────────────────────────────────────────────────────────────
    @PostMapping("/generate")
    public Map<String, Object> generateOne(@RequestParam Integer employeeId,
                                           @RequestParam String  employeeType,
                                           @RequestParam Integer branchId,
                                           @RequestParam String  month) {

        YearMonth ym = YearMonth.parse(month);
        int year = ym.getYear(), mon = ym.getMonthValue();

        // ───── 读取工资配置 ─────
        SalaryConfig cfg = cfgRepo.findById(employeeType)
                .orElseThrow(() -> new RuntimeException("未配置该员工类型薪资规则: " + employeeType));

        // ───── 查询考勤 / 加班 / 请假 / 绩效 ─────
        Integer attendanceDays = optInt(jdbc.queryForObject("""
        SELECT COUNT(DISTINCT check_in_date)
        FROM attendance_records
        WHERE employee_id=? AND employee_type=? AND status='normal'
          AND YEAR(check_in_date)=? AND MONTH(check_in_date)=?
    """, Integer.class, employeeId, employeeType, year, mon));

        Integer overtimeCount = optInt(jdbc.queryForObject("""
        SELECT COUNT(*) FROM overtime_requests
        WHERE employee_id=? AND employee_type=? AND status='审批成功'
          AND YEAR(date)=? AND MONTH(date)=?
    """, Integer.class, employeeId, employeeType, year, mon));

        Integer leaveDays = optInt(jdbc.queryForObject("""
        SELECT COALESCE(SUM(DATEDIFF(end_date,start_date)+1),0)
        FROM leave_requests
        WHERE employee_id=? AND employee_type=? AND status='审批成功'
          AND YEAR(start_date)=? AND MONTH(start_date)=?
    """, Integer.class, employeeId, employeeType, year, mon));

        Integer totalScore = optInt(jdbc.queryForObject("""
        SELECT COALESCE(SUM(total_score),0)
        FROM performance_reviews
        WHERE employee_id=? AND employee_type=?
          AND review_period_start<=? AND review_period_end>=?
    """, Integer.class, employeeId, employeeType,
                ym.atEndOfMonth(), ym.atDay(1)));

        // ───── 工作日天数 & 旷工计算 ─────
        int workDaysInMonth = countWorkDays(ym);
        int absentDays      = Math.max(workDaysInMonth - attendanceDays - leaveDays, 0);

        int effectiveDays   = attendanceDays + leaveDays;

        // ───── 计算工资各项金额 ─────
        BigDecimal baseSalary = cfg.getBaseSalary()
                .multiply(BigDecimal.valueOf(effectiveDays))
                .divide(BigDecimal.valueOf(workDaysInMonth), 2, RoundingMode.HALF_UP);

        BigDecimal overtimeBonus     = cfg.getOvertimeRate().multiply(BigDecimal.valueOf(overtimeCount));
        BigDecimal leaveDeduction    = cfg.getLeaveDeductionRate().multiply(BigDecimal.valueOf(leaveDays));
        BigDecimal performanceBonus  = BigDecimal.valueOf(totalScore).multiply(PERFORMANCE_RATE);

        BigDecimal totalSalary = baseSalary.add(performanceBonus).add(overtimeBonus);
        BigDecimal netSalary   = totalSalary.subtract(leaveDeduction);

        // ───── 保存 / 更新记录 ─────
        Salary sal = salaryRepo
                .findByEmployeeIdAndEmployeeTypeAndSalaryMonth(employeeId, employeeType, month)
                .orElseGet(() -> Salary.builder()
                        .employeeId(employeeId)
                        .employeeType(employeeType)
                        .branchId(branchId)
                        .salaryMonth(month)
                        .build());

        sal.setBaseSalary(baseSalary);
        sal.setPerformanceBonus(performanceBonus);
        sal.setOvertimeBonus(overtimeBonus);
        sal.setLeaveDeduction(leaveDeduction);
        sal.setTotalSalary(totalSalary);
        sal.setNetSalary(netSalary);

        salaryRepo.save(sal);

        // ───── 返回结果 ─────
        return Map.of(
                "status", "success",
                "data",   sal,
                "meta", Map.of(
                        "attendanceDays",   attendanceDays,
                        "leaveDays",        leaveDays,
                        "overtimeCount",    overtimeCount,
                        "totalScore",       totalScore,
                        "performanceBonus", performanceBonus,
                        "workDaysInMonth",  workDaysInMonth,
                        "absentDays",       absentDays
                )
        );
    }

    // ───────────────────────────────────────────────────────────────
    // 其它简单查询接口
    // ───────────────────────────────────────────────────────────────
    @GetMapping("/list")
    public Map<String, Object> listAll(@RequestParam(required = false) Integer employeeId,
                                       @RequestParam(required = false) String employeeType) {
        List<Salary> list;

        if (employeeId != null && employeeType != null && !employeeType.isEmpty()) {
            list = salaryRepo.findByEmployeeIdAndEmployeeType(employeeId, employeeType);
        } else if (employeeId != null) {
            list = salaryRepo.findByEmployeeId(employeeId);
        } else if (employeeType != null && !employeeType.isEmpty()) {
            list = salaryRepo.findByEmployeeType(employeeType);
        } else {
            list = salaryRepo.findAll();
        }

        return Map.of("status", "success", "data", list);
    }


    @GetMapping("/by-branch")
    public Map<String, Object> byBranch(@RequestParam Integer branchId,
                                        @RequestParam String  month) {
        return Map.of("status", "success",
                "data", salaryRepo.findByBranchIdAndSalaryMonth(branchId, month));
    }

    // ───────────────────────────────────────────────────────────────
    // 工具方法
    // ───────────────────────────────────────────────────────────────
    /** 将 null 转 0 */
    private int optInt(Integer v) { return Optional.ofNullable(v).orElse(0); }

    /** 统计某月（周一~周五）的工作日数量 */
    private int countWorkDays(YearMonth ym) {
        int work = 0;
        for (LocalDate d = ym.atDay(1); !d.isAfter(ym.atEndOfMonth()); d = d.plusDays(1)) {
            DayOfWeek w = d.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) work++;
        }
        return work;
    }
    /* ============== 新增：获取“明细”接口 ============== */
    @GetMapping("/detail")
    public Map<String, Object> detail(@RequestParam Integer employeeId,
                                      @RequestParam String  employeeType,
                                      @RequestParam String  month) {

        YearMonth ym   = YearMonth.parse(month);
        int       year = ym.getYear(), mon = ym.getMonthValue();

        // 复用与 generateOne 完全相同的统计 SQL
        Integer attendanceDays = optInt(jdbc.queryForObject("""
            SELECT COUNT(DISTINCT check_in_date)
            FROM attendance_records
            WHERE employee_id=? AND employee_type=? AND status='normal'
              AND YEAR(check_in_date)=? AND MONTH(check_in_date)=?
        """, Integer.class, employeeId, employeeType, year, mon));

        Integer leaveDays = optInt(jdbc.queryForObject("""
            SELECT COALESCE(SUM(DATEDIFF(end_date,start_date)+1),0)
            FROM leave_requests
            WHERE employee_id=? AND employee_type=? AND status='审批成功'
              AND YEAR(start_date)=? AND MONTH(start_date)=?
        """, Integer.class, employeeId, employeeType, year, mon));

        Integer overtimeCount = optInt(jdbc.queryForObject("""
            SELECT COUNT(*) FROM overtime_requests
            WHERE employee_id=? AND employee_type=? AND status='审批成功'
              AND YEAR(date)=? AND MONTH(date)=?
        """, Integer.class, employeeId, employeeType, year, mon));

        Integer totalScore = optInt(jdbc.queryForObject("""
            SELECT COALESCE(SUM(total_score),0)
            FROM performance_reviews
            WHERE employee_id=? AND employee_type=?
              AND review_period_start<=? AND review_period_end>=?
        """, Integer.class, employeeId, employeeType,
                ym.atEndOfMonth(), ym.atDay(1)));

        int workDaysInMonth = countWorkDays(ym);
        int absentDays      = Math.max(workDaysInMonth - attendanceDays - leaveDays, 0);

        return Map.of(
                "status", "success",
                "data", Map.of(
                        "workDaysInMonth", workDaysInMonth,
                        "attendanceDays",  attendanceDays,
                        "leaveDays",       leaveDays,
                        "absentDays",      absentDays,
                        "overtimeCount",   overtimeCount,
                        "totalScore",      totalScore
                )
        );
    }


}
