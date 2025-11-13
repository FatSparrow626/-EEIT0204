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
@CrossOrigin(origins = { "http://localhost:5173", "http://172.22.34.82:5173" })
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
        public ResponseEntity<?> authenticateUser(
                        @Parameter(description = "登入請求物件", required = true) @RequestBody LoginRequest loginRequest,
                        HttpServletRequest request) {
                try {
                        /**
                         * 呼叫AuthenticationManager(授權經理)執行認證，請他建立一個帳號、密碼表單，呼叫的是UsernamePasswordAuthenticationToken(Object
                         * principal, Object credentials)這個建構版本，會將isAuthenticated設為false
                         * principal: 使用者名稱、credentials: 明文密碼
                         * 授權經理會自動派發工作給SecurityConfig中，透過@Bean向IoC容器註冊DaoAuthenticationProvider(授權操作員職位)，跟IoC容器約定好撈資料的方式、驗證密碼的加密方法，回傳一個帶有資料的授權操作員實體(authProvider)給IoC容器。
                         * -> 現在操作員接到授權經理的表單後，執行以下步驟:
                         * 1. userDetailsService.loadUserByUsername(username) 得到以下資料
                         *   a. 明文密碼(使用者輸入的，後端loginRequest.getPassword()接到)
                         *   b. 加密過的密碼(loadUserByUsername(...)從DB找到的，帶在UserDetails物件身上，也就是EmployeeUserDetails(employeeUser, authorities))
                         * -> 接著執行
                         * 2. passwordEncoder的matches(rawPassword, encodedPassword) 比對密碼
                         * -> 若密碼比對成功
                         * 3. 操作員會自動呼叫 new UsernamePasswordAuthenticationToken(EmployeeUserDetails, null, EmployeeUserDetails.getAuthorities())
                         *   -> UsernamePasswordAuthenticationToken就是Authentication的子類，可以想成各種申請表單(...Token)都是一種證明(Authentication)
                         *     a. 驗證失敗會拋出例外
                         *     b. 驗證成功會回傳一個isAuthenticated為true的Authentication物件
                         * 4. 最後操作員->經理->將剛才的物件"原封不動"回傳Controller
                         **/
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                loginRequest.getUsername(),loginRequest.getPassword()));
                        /**
                         * SecurityContextHolder可以想像是Spring Security提供的手機池，.getContext()跟他借用一隻專屬於本次request(Thread local)的手機，.setAuthentication(..)傳入身分證明，啟用這支手機內部功能
                         **/ 
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        /**
                         * 回傳jwt字串
                         */
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
                                        "AUTH");

                        return ResponseEntity.ok(
                                        new JwtResponse(
                                        // JwtResponse唯一必須的
                                        jwt,
                                        // 剩下端看前端需求
                                        employeeUser.getEmployeeUserId().intValue(),
                                        employeeUser.getUsername(),
                                        employeeUser.getEmail(),
                                        employeeUser.getEmployeeType().name(),
                                        userDetails.getAuthorities().stream()
                                                        .map(authority -> authority.getAuthority())
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
                                        "AUTH");
                        // 重新拋出異常，讓 Spring 的統一異常處理機制回傳 401 Unauthorized
                        throw e;
                }
        }
}