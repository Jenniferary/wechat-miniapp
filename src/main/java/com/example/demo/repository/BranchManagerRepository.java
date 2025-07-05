package com.example.demo.repository;

import com.example.demo.entity.BranchManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchManagerRepository extends JpaRepository<BranchManager, Integer> {

    // 根据用户名查询店长信息
    Optional<BranchManager> findByUsername(String username);

    // 根据邮箱查询
    Optional<BranchManager> findByEmail(String email);

    // 根据门店ID查询店长（如果一个门店只对应一个店长）
    Optional<BranchManager> findByBranchId(int branchId);
}
