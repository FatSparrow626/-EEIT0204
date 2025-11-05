package com.project.core.dao;

import com.project.employeeuser.model.EmployeeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 核心 - 員工使用者 DAO (Repository)
 */
@Repository
public interface EmployeeUserRepository extends JpaRepository<EmployeeUser, Integer> {
    Optional<EmployeeUser> findByUsername(String username);
}