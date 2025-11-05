package com.project.core.dto.response;

import lombok.Data;

/**
 * 角色 DTO
 * 用於傳遞角色相關資訊
 */
@Data
public class RoleDto {
    
    // ===== 新增：角色管理功能開始 =====
    /**
     * 角色ID
     */
    private Integer roleId;
    
    /**
     * 角色名稱
     */
    private String roleName;
    
    /**
     * 顯示名稱
     */
    private String displayName;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 是否啟用
     */
    private Boolean isActive;
    // ===== 新增：角色管理功能結束 =====
}