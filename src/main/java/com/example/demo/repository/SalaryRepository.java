package com.example.demo.repository;

import com.example.demo.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Integer> {

    Optional<Salary> findByEmployeeIdAndEmployeeTypeAndSalaryMonth(Integer employeeId, String employeeType, String salaryMonth);

    List<Salary> findByBranchIdAndSalaryMonth(Integer branchId, String salaryMonth);

    // 新增查询方法
    List<Salary> findByEmployeeIdAndEmployeeType(Integer employeeId, String employeeType);

    List<Salary> findByEmployeeId(Integer employeeId);

    List<Salary> findByEmployeeType(String employeeType);
}
