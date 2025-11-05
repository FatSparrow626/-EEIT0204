package com.project.core.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class EmployeeUserCreateRequest {
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
    private String password; // 接收明文密碼，將在服務層進行加密
    private Boolean isActive = true;
    
    // 權限相關欄位
    private List<String> roleNames; // 角色名稱列表
}