package com.example.demo.repository;

import com.example.demo.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChefRepository extends JpaRepository<Chef, Integer> {
    Optional<Chef> findByUsername(String username);
}
