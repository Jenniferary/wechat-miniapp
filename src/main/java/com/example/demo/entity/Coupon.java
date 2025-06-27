package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;  // 优惠券ID

    @Column(name = "user_id")
    private Integer userId;  // 用户ID

    @Column(name = "start_date")
    private Timestamp startDate;  // 开始日期

    @Column(name = "expiry_date")
    private Timestamp expiryDate;  // 过期日期

    @Column(name = "min_threshold")
    private Double minThreshold;  // 最低消费金额

    @Column(name = "discount_amount")
    private Double discountAmount;  // 折扣金额
}
