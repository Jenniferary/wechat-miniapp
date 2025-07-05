package com.example.demo.repository;

import com.example.demo.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 根据 userId 查找优惠券
    List<Coupon> findByUserId(Integer userId);

}
