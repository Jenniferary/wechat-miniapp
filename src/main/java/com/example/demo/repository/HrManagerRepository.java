package com.example.demo.repository;

import com.example.demo.entity.HrManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HrManagerRepository extends JpaRepository<HrManager, Integer> {
    Optional<HrManager> findByUsername(String username);
}
