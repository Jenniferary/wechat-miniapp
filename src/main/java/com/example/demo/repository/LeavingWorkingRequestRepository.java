package com.example.demo.repository;

import com.example.demo.entity.LeavingWorkingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavingWorkingRequestRepository extends JpaRepository<LeavingWorkingRequest, Integer> {

    // 根据分店ID和状态查找待审批的离职申请
    List<LeavingWorkingRequest> findByBranchIdAndStatus(Integer branchId, String status);

    // 更新申请状态
    @Query("UPDATE LeavingWorkingRequest l SET l.status = ?2 WHERE l.id = ?1")
    int updateStatus(Integer id, String status);
}
