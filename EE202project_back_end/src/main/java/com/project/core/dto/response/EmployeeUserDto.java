package com.project.core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeUserDto {
    private Integer employeeUserId;
    private String firstName;
    private String lastName;
    private String employeeNumber;
    private String employeeType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String photoPath;
    private Integer employeeDepartmentId;
    private Integer employeePositionId;
    private Integer managerEmployeeUserId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate terminationDate;
    private String username;
    private String passwordHash;
    private boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}