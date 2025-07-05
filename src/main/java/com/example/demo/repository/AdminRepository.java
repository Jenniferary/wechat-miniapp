package com.example.demo.repository;

import com.example.demo.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    
    // 根据用户名查询管理员
    Optional<Admin> findByUsername(String username);
    
    // 根据用户名和密码查询管理员（用于登录验证）
    Optional<Admin> findByUsernameAndPassword(String username, String password);
}