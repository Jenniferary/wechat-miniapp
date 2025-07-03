package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews",
        indexes = {
                @Index(name = "idx_employee", columnList = "employee_id, employee_type"),
                @Index(name = "idx_branch",   columnList = "branch_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 员工信息 **/
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    /** waiter / chef / hr ... **/
    @Column(name = "employee_type", length = 20, nullable = false)
    private String employeeType;

    /** 门店 */
    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    /** 考核周期 */
    @Column(name = "review_period_start", nullable = false)
    private LocalDate reviewPeriodStart;

    @Column(name = "review_period_end", nullable = false)
    private LocalDate reviewPeriodEnd;

    /** 各项分数 */
    @Column(name = "punctuality_score", nullable = false)
    private Integer punctualityScore = 0;

    @Column(name = "attendance_score", nullable = false)
    private Integer attendanceScore = 0;

    @Column(name = "performance_score", nullable = false)
    private Integer performanceScore = 0;

    @Column(name = "total_score", updatable = false, insertable = false)
    private Integer totalScore;

    /** 评语 */
    @Column(columnDefinition = "TEXT")
    private String comments;

    /** 审计字段 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
