package com.project.HR.repository;

import com.project.HR.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Integer> {
    /**
     * Finds a leave type by its name.
     * @param name The name of the leave type (e.g., "特休", "病假").
     * @return an Optional containing the LeaveType if found.
     */
    Optional<LeaveType> findByName(String name);
}
