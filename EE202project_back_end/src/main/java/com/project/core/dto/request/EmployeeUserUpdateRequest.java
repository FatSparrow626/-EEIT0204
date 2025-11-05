package com.project.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeUserUpdateRequest {
    private String firstName;
    private String lastName;
    private String employeeNumber;
    private String employeeType;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String photoPath;
    private Integer employeeDepartmentId;
    private Integer employeePositionId;
    private Integer managerEmployeeUserId;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String username;
    private String password; // 可選，如果需要修改密碼（明文，將在服務層加密）
    @JsonProperty("isActive")
    private Boolean isActive;
}