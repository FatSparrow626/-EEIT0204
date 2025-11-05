package com.project.HR.repository;

import com.project.HR.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveStatusRepository extends JpaRepository<LeaveStatus, Integer> {
    Optional<LeaveStatus> findByCode(String code);
}