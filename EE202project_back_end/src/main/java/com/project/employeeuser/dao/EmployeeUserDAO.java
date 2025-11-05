package com.project.employeeuser.dao;

import com.project.employeeuser.model.EmployeeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmployeeUserDAO extends JpaRepository<EmployeeUser, Long> {
    Optional<EmployeeUser> findByUsername(String username);
    Optional<EmployeeUser> findByEmployeeNumber(String employeeNumber);
    Optional<EmployeeUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmployeeNumber(String employeeNumber);
    boolean existsByEmail(String email);
    
    // 用於忘記密碼功能的身份驗證查詢
    Optional<EmployeeUser> findByEmployeeNumberAndEmailAndHireDate(String employeeNumber, String email, LocalDate hireDate);
}
