package com.project.core.controller;

import com.project.core.dto.request.LoginRequest;
import com.project.core.dto.response.JwtResponse;
import com.project.employeeuser.model.EmployeeUser;
import com.project.core.security.EmployeeUserDetails;
import com.project.core.security.EmployeeUserDetailsService;
import com.project.core.security.jwt.JwtUtils;
import com.project.core.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "認證與授權", description = "使用者登入與JWT管理")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://172.22.34.82:5173"})
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmployeeUserDetailsService userDetailsService;

    @Autowired
    SystemLogService systemLogService;

    @Operation(summary = "使用者登入流程第一站", description = "透過使用者名稱和密碼進行登入，並返回JWT")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Parameter(description = "登入請求物件", required = true) @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // 呼叫AuthenticationManager執行認證
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            EmployeeUserDetails userDetails = (EmployeeUserDetails) authentication.getPrincipal();
            EmployeeUser employeeUser = userDetails.getEmployeeUser();

            systemLogService.log(
                    "LOGIN",
                    "使用者登入成功",
                    employeeUser.getEmployeeUserId().intValue(),
                    employeeUser.getUsername(),
                    "EMPLOYEE",
                    String.valueOf(employeeUser.getEmployeeUserId()),
                    employeeUser.getFirstName() + " " + employeeUser.getLastName(),
                    null,
                    null,
                    "使用者 " + employeeUser.getUsername() + " 成功登入",
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "AUTH"
            );

            return ResponseEntity.ok(new JwtResponse(jwt,
                    employeeUser.getEmployeeUserId().intValue(),
                    employeeUser.getUsername(),
                    employeeUser.getEmail(),
                    employeeUser.getEmployeeType().name(),
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList())));
        } catch (Exception e) {
            systemLogService.log(
                    "LOGIN_FAILED",
                    "使用者登入失敗",
                    null,
                    loginRequest.getUsername(),
                    "EMPLOYEE",
                    null,
                    null,
                    null,
                    null, // old value
                    "使用者 " + loginRequest.getUsername() + " 登入失敗: " + e.getMessage(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "AUTH"
            );
            // 重新拋出異常，讓 Spring 的統一異常處理機制回傳 401 Unauthorized
            throw e;
        }
    }
}