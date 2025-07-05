package com.example.demo.repository;

import com.example.demo.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {

    // 查询指定员工、岗位、门店、周期内的打卡记录
    List<AttendanceRecord> findByEmployeeIdAndEmployeeTypeAndBranchIdAndCheckInDateBetween(
            Integer employeeId,
            String employeeType,
            Integer branchId,
            LocalDate startDate,
            LocalDate endDate
    );
}
