package com.example.demo.repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.entity.Waiter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WaiterRepository extends JpaRepository<Waiter, Integer> {
    Optional<Waiter> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("INSERT INTO Waiter (username, passwordHash, name, phone, email, branchId) " +
            "VALUES (:username, :passwordHash, :name, :phone, :email, :branchId)")
    void addWaiter(@Param("username") String username,
                   @Param("passwordHash") String passwordHash,
                   @Param("name") String name,
                   @Param("phone") String phone,
                   @Param("email") String email,
                   @Param("branchId") Integer branchId);
}
