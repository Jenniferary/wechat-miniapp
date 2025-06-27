package com.example.demo.repository;

import com.example.demo.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Integer> {
    Optional<Applicant> findByUsername(String username);
    Optional<Applicant> findByEmail(String email);
}
