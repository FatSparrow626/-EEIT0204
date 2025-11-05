package com.project.employeeuser.controller;

import com.project.employeeuser.model.EmployeeUser;
import com.project.employeeuser.service.EmployeeUserService;

import com.project.core.dto.response.RoleDto;
import com.project.core.dto.request.UpdateEmployeeRolesRequest;
import com.project.core.dto.request.EmployeeUserCreateRequest;
import com.project.core.dto.request.ResetPasswordRequest;
import com.project.core.dto.response.EmployeeUserDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employee-users")
@Tag(name = "員工使用者管理", description = "處理員工使用者帳戶的相關操作")
public class EmployeeUserController {

    @Autowired
    private EmployeeUserService employeeUserService;

    // ===== 新增：角色管理功能開始 =====
    @Autowired
    private com.project.core.service.EmployeeUserService coreEmployeeUserService;
    // ===== 新增：角色管理功能結束 =====

    @Operation(summary = "獲取所有員工使用者", description = "返回所有員工使用者帳戶的列表")
    @GetMapping
    public ResponseEntity<List<EmployeeUser>> getAllEmployeeUsers() {
        List<EmployeeUser> employeeUsers = employeeUserService.getAllEmployeeUsers();
        return ResponseEntity.ok(employeeUsers);
    }

    @Operation(summary = "根據ID獲取員工使用者", description = "根據員工使用者ID查詢單一帳戶的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeUser> getEmployeeUserById(
            @Parameter(description = "員工使用者ID", required = true) @PathVariable Long id) {
        return employeeUserService.getEmployeeUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "建立員工使用者", description = "建立一個新的員工使用者帳戶")
    @PostMapping
    public ResponseEntity<?> createEmployeeUser(
            @Parameter(description = "員工使用者物件", required = true) @RequestBody EmployeeUser employeeUser) {
        if (employeeUserService.existsByUsername(employeeUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }
        if (employeeUser.getEmployeeNumber() != null
                && employeeUserService.existsByEmployeeNumber(employeeUser.getEmployeeNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee number already exists.");
        }
        if (employeeUser.getEmail() != null && employeeUserService.existsByEmail(employeeUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }
        EmployeeUser createdUser = employeeUserService.createEmployeeUser(employeeUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "更新員工使用者", description = "根據員工使用者ID更新帳戶資訊")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeUser> updateEmployeeUser(
            @Parameter(description = "員工使用者ID", required = true) @PathVariable Long id,
            @Parameter(description = "包含更新資料的員工使用者物件", required = true) @RequestBody EmployeeUser employeeUserDetails) {
        try {
            EmployeeUser updatedUser = employeeUserService.updateEmployeeUser(id, employeeUserDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "刪除員工使用者", description = "根據員工使用者ID刪除指定的帳戶")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployeeUser(
            @Parameter(description = "要刪除的員工使用者ID", required = true) @PathVariable Long id) {
        try {
            employeeUserService.deleteEmployeeUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== 新增：角色管理功能開始 =====
    @Operation(summary = "獲取員工角色", description = "根據員工使用者ID獲取其擁有的所有角色名稱")
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<String>> getEmployeeRoles(
            @Parameter(description = "員工使用者ID", required = true) @PathVariable Long id) {
        try {
            List<String> roles = coreEmployeeUserService.getEmployeeRoles(id);
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "更新員工角色", description = "根據員工使用者ID更新其角色配置")
    @PutMapping("/{id}/roles")
    public ResponseEntity<?> updateEmployeeRoles(
            @Parameter(description = "員工使用者ID", required = true) @PathVariable Long id,
            @Parameter(description = "角色更新請求", required = true) @RequestBody UpdateEmployeeRolesRequest request) {
        try {
            coreEmployeeUserService.updateEmployeeRoles(id, request.getRoleNames(), request.getUpdatedBy());
            return ResponseEntity.ok().body("員工角色更新成功");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("找不到員工")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("找不到角色")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新員工角色失敗: " + e.getMessage());
            }
        }
    }

    @Operation(summary = "獲取可用角色", description = "獲取系統中所有可用的角色列表")
    @GetMapping("/available-roles")
    public ResponseEntity<List<RoleDto>> getAvailableRoles() {
        try {
            List<RoleDto> roles = coreEmployeeUserService.getAvailableRoles();
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "建立員工使用者(支援權限指派)", description = "建立一個新的員工使用者帳戶並同時指派角色和權限")
    @PostMapping("/with-permissions")
    public ResponseEntity<?> createEmployeeUserWithPermissions(
            @Parameter(description = "員工使用者建立請求", required = true) @RequestBody EmployeeUserCreateRequest request) {
        try {
            EmployeeUserDto createdUser = coreEmployeeUserService.createEmployeeUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("使用者名稱已存在")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("使用者名稱已存在: " + request.getUsername());
            } else if (e.getMessage().contains("員工編號已存在")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("員工編號已存在: " + request.getEmployeeNumber());
            } else if (e.getMessage().contains("電子郵件已存在")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("電子郵件已存在: " + request.getEmail());
            } else if (e.getMessage().contains("找不到角色") || e.getMessage().contains("找不到權限")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("建立員工使用者失敗: " + e.getMessage());
            }
        }
    }
    // ===== 新增：角色管理功能結束 =====

    @Operation(summary = "重設密碼", description = "透過員工編號、電子信箱、入職日期驗證身份並重設密碼")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "重設密碼請求", required = true) @Valid @RequestBody ResetPasswordRequest request) {
        try {
            // 呼叫服務層處理重設密碼邏輯
            employeeUserService.resetPassword(
                    request.getEmployeeNumber(),
                    request.getEmail(),
                    request.getHireDate(),
                    request.getNewPassword());

            // 成功回應
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "密碼重設成功");
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // 錯誤處理
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            String errorMessage = e.getMessage();

            if (errorMessage.contains("找不到符合條件的員工資料")) {
                errorResponse.put("message", "身份驗證失敗：員工編號、電子信箱或入職日期不匹配");
                errorResponse.put("code", "IDENTITY_VERIFICATION_FAILED");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

            } else if (errorMessage.contains("該員工帳戶已被停用")) {
                errorResponse.put("message", "該員工帳戶已被停用，無法重設密碼");
                errorResponse.put("code", "ACCOUNT_DISABLED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

            } else {
                errorResponse.put("message", "密碼重設失敗：" + errorMessage);
                errorResponse.put("code", "RESET_PASSWORD_FAILED");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

        } catch (Exception e) {
            // 未預期的錯誤
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "系統發生未預期的錯誤，請稍後再試");
            errorResponse.put("code", "UNEXPECTED_ERROR");
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}