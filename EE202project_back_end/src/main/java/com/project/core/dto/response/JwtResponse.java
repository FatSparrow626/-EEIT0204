package com.project.core.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer employeeId;
    private String username;
    private String email;
    private String employeeType;
    private List<String> authorities;

    public JwtResponse(String accessToken, Integer employeeId, String username, String email, String employeeType, List<String> authorities) {
        this.token = accessToken;
        this.employeeId = employeeId;
        this.username = username;
        this.email = email;
        this.employeeType = employeeType;
        this.authorities = authorities;
    }
}
