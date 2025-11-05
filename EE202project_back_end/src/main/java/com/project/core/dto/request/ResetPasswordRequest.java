package com.project.core.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "重設密碼請求")
public class ResetPasswordRequest {
    
    @NotBlank(message = "員工編號不能為空")
    @Schema(description = "員工編號", required = true, example = "EMP001")
    private String employeeNumber;
    
    @NotBlank(message = "電子信箱不能為空")
    @Email(message = "電子信箱格式不正確")
    @Schema(description = "電子信箱", required = true, example = "employee@example.com")
    private String email;
    
    @NotNull(message = "入職日期不能為空")
    @Schema(description = "入職日期", required = true, example = "2023-01-15")
    private LocalDate hireDate;
    
    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "密碼長度至少6位")
    @Schema(description = "新密碼", required = true, example = "newPassword123")
    private String newPassword;
    
    // 預設建構子
    public ResetPasswordRequest() {}
    
    // 全參數建構子
    public ResetPasswordRequest(String employeeNumber, String email, LocalDate hireDate, String newPassword) {
        this.employeeNumber = employeeNumber;
        this.email = email;
        this.hireDate = hireDate;
        this.newPassword = newPassword;
    }
    
    // Getter 和 Setter 方法
    public String getEmployeeNumber() {
        return employeeNumber;
    }
    
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "employeeNumber='" + employeeNumber + '\'' +
                ", email='" + email + '\'' +
                ", hireDate=" + hireDate +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}