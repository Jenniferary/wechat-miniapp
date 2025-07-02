package com.example.demo.repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChefRepository extends JpaRepository<Chef, Integer> {
    Optional<Chef> findByUsername(String username);
    List<Chef> findByBranchId(Integer branchId);
    // 自定义保存 Chef 方法
    @Modifying
    @Transactional
    @Query("INSERT INTO Chef (username, passwordHash, name, phone, email, branchId) " +
            "VALUES (:username, :passwordHash, :name, :phone, :email, :branchId)")
    void addChef(@Param("username") String username,
                 @Param("passwordHash") String passwordHash,
                 @Param("name") String name,
                 @Param("phone") String phone,
                 @Param("email") String email,
                 @Param("branchId") Integer branchId);
}
