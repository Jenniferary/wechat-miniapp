package com.example.demo.repository;

import com.example.demo.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Integer> {
    Optional<Counter> findByUsername(String username);
    List<Counter> findByBranchId(Integer branchId);
}
