package com.project.employeeuser.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String email;
    private String phone;
    private String birthDate; // 字串格式 "YYYY-MM-DD"
    
    @Override
    public String toString() {
        return "ProfileUpdateRequest{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", birthDate='" + birthDate + '\'' +
                '}';
    }
}