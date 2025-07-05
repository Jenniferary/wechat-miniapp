package com.example.demo.repository;

import com.example.demo.entity.AfternoonTeaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AfternoonTeaRepository extends JpaRepository<AfternoonTeaItem, Long> {

    @Query(value = "SELECT * FROM afternoon_tea LIMIT 0, 9", nativeQuery = true)
    List<AfternoonTeaItem> findDesserts();

    @Query(value = "SELECT * FROM afternoon_tea LIMIT 9, 999", nativeQuery = true)
    List<AfternoonTeaItem> findDrinks();
}
