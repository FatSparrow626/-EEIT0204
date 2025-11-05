package com.project.core.service;

import com.project.core.dto.request.EmployeeUserCreateRequest;
import com.project.core.dto.request.EmployeeUserUpdateRequest;
import com.project.core.dto.response.EmployeeUserDto;
import com.project.core.dto.response.RoleDto;
import java.util.List;
import java.util.Optional;

/**
 * 核心 - 員工使用者服務介面
 * 定義員工使用者相關的業務邏輯操作。
 */
public interface EmployeeUserService {

    /**
     * 查詢所有員工使用者。
     * @return 員工使用者列表
     */
    List<EmployeeUserDto> findAllEmployeeUsers();

    /**
     * 根據ID查詢單一員工使用者。
     * @param id 員工使用者ID
     * @return Optional<EmployeeUserDto> 如果找到則返回員工使用者，否則為空
     */
    Optional<EmployeeUserDto> findEmployeeUserById(Integer id);

    /**
     * 根據使用者名稱查詢單一員工使用者。
     * @param username 使用者名稱
     * @return Optional<EmployeeUserDto> 如果找到則返回員工使用者，否則為空
     */
    Optional<EmployeeUserDto> findEmployeeUserByUsername(String username);

    /**
     * 新增員工使用者。
     * @param request 包含員工使用者資訊的請求DTO
     * @return 儲存後的員工使用者DTO
     */
    EmployeeUserDto createEmployeeUser(EmployeeUserCreateRequest request);

    /**
     * 更新員工使用者。
     * @param id 員工使用者ID
     * @param request 包含更新資訊的請求DTO
     * @return 更新後的員工使用者DTO
     */
    EmployeeUserDto updateEmployeeUser(Integer id, EmployeeUserUpdateRequest request);

    /**
     * 根據ID刪除員工使用者。
     * @param id 員工使用者ID
     */
    void deleteEmployeeUserById(Integer id);
    
    // ===== 新增：角色管理功能開始 =====
    /**
     * 查詢員工的所有角色名稱
     * @param employeeId 員工ID
     * @return 角色名稱列表
     */
    List<String> getEmployeeRoles(Long employeeId);
    
    /**
     * 更新員工的角色
     * @param employeeId 員工ID
     * @param roleNames 角色名稱列表
     * @param updatedBy 更新者
     */
    void updateEmployeeRoles(Long employeeId, List<String> roleNames, String updatedBy);
    
    /**
     * 查詢所有可用的角色
     * @return 可用角色列表
     */
    List<RoleDto> getAvailableRoles();
    // ===== 新增：角色管理功能結束 =====
}