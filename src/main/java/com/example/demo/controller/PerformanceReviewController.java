package com.example.demo.controller;

import com.example.demo.entity.PerformanceReview;
import com.example.demo.entity.AttendanceRecord;
import com.example.demo.repository.PerformanceReviewRepository;
import com.example.demo.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/performance-reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceReviewRepository repo;
    private final AttendanceRecordRepository attendanceRepo;

    private int calculateAttendanceScore(PerformanceReview pr) {
        List<AttendanceRecord> records = attendanceRepo.findByEmployeeIdAndEmployeeTypeAndBranchIdAndCheckInDateBetween(
                pr.getEmployeeId(),
                pr.getEmployeeType(),
                pr.getBranchId(),
                pr.getReviewPeriodStart(),
                pr.getReviewPeriodEnd()
        );
        long totalDays = ChronoUnit.DAYS.between(pr.getReviewPeriodStart(), pr.getReviewPeriodEnd()) + 1;
        long attendanceDays = records.stream()
                .map(AttendanceRecord::getCheckInDate)
                .distinct()
                .count();
        double ratio = (double) attendanceDays / totalDays;
        return (int) (ratio * 100);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerformanceReview create(@RequestBody PerformanceReview pr) {
        pr.setAttendanceScore(calculateAttendanceScore(pr));
        return repo.save(pr);
    }

    @PutMapping("/{id}")
    public PerformanceReview update(@PathVariable Integer id,
                                    @RequestBody PerformanceReview pr) {
        pr.setId(id);
        pr.setAttendanceScore(calculateAttendanceScore(pr));
        return repo.save(pr);
    }

    @GetMapping("/{id}")
    public PerformanceReview get(@PathVariable Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    @GetMapping
    public List<PerformanceReview> list() {
        return repo.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }

    @GetMapping("/calculate-attendance-score")
    public int calculateAttendanceScoreFor(
            @RequestParam Integer employeeId,
            @RequestParam String employeeType,
            @RequestParam Integer branchId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        PerformanceReview pr = new PerformanceReview();
        pr.setEmployeeId(employeeId);
        pr.setEmployeeType(employeeType);
        pr.setBranchId(branchId);
        pr.setReviewPeriodStart(LocalDate.parse(startDate));
        pr.setReviewPeriodEnd(LocalDate.parse(endDate));

        return calculateAttendanceScore(pr);
    }

    // 新增：获取历史考核记录，避免与 /{id} 冲突
    @GetMapping("/history")
    public List<PerformanceReview> getHistoryByBranch(@RequestParam Integer branchId) {
        return repo.findByBranchIdOrderByReviewPeriodStartDesc(branchId);
    }
}

