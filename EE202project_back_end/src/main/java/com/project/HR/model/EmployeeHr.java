package com.project.HR.model;

import com.project.employeeuser.model.EmployeeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A Plain Old Java Object (POJO) representing an employee within the HR module.
 * This class mirrors the structure of the EmployeeUser entity but uses Integer for ID fields
 * to serve as an internal, type-safe data container for HR-related services.
 * It is NOT a JPA entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHr {

    private Integer employeeUserId;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String username;
    private String passwordHash;
    private EmployeeType employeeType;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String photoPath;
    private Integer employeeDepartmentId;
    private Integer employeePositionId;
    private Integer managerEmployeeUserId;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper method for full name, consistent with the original entity's logic
    public String getFullName() {
        if (lastName == null && firstName == null) {
            return "";
        }
        if (lastName == null) {
            return firstName;
        }
        if (firstName == null) {
            return lastName;
        }
        return this.lastName + " " + this.firstName;
    }
}