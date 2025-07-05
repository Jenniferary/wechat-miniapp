package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "salary_config")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryConfig {

    @Id
    @Column(name = "employee_type", length = 20)
    private String employeeType;

    private BigDecimal baseSalary;
    private BigDecimal overtimeRate;
    private BigDecimal leaveDeductionRate;
}
