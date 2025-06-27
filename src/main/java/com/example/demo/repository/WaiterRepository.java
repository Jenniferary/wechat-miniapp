package com.example.demo.repository;

import com.example.demo.entity.Waiter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WaiterRepository extends JpaRepository<Waiter, Integer> {
    Optional<Waiter> findByUsername(String username);
}
