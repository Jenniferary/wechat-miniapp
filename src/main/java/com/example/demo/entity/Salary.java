package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salaries",
        uniqueConstraints = @UniqueConstraint(name = "uk_emp_month",
                columnNames = {"employee_id", "employee_type", "salary_month"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Integer id;

    @Column(name = "employee_id",   nullable = false)
    private Integer employeeId;

    @Column(name = "employee_type", nullable = false)
    private String  employeeType;  // chef / waiter / counter / hr

    @Column(name = "branch_id",     nullable = false)
    private Integer branchId;

    /** 格式：2025-07 */
    @Column(name = "salary_month",  nullable = false, length = 7)
    private String  salaryMonth;

    @Column(name = "base_salary",       nullable = false)
    private BigDecimal baseSalary;

    @Column(name = "performance_bonus")
    private BigDecimal performanceBonus;

    @Column(name = "overtime_bonus")
    private BigDecimal overtimeBonus;

    @Column(name = "leave_deduction")
    private BigDecimal leaveDeduction;

    @Column(name = "total_salary")
    private BigDecimal totalSalary;

    @Column(name = "net_salary")
    private BigDecimal netSalary;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
