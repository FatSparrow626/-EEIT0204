package com.project.core.dto.request;

import lombok.Data;
import java.util.List;

/**
 * 更新員工角色請求 DTO
 */
@Data
public class UpdateEmployeeRolesRequest {
    
    // ===== 新增：角色管理功能開始 =====
    /**
     * 角色名稱列表
     */
    private List<String> roleNames;
    
    /**
     * 更新者
     */
    private String updatedBy;
    // ===== 新增：角色管理功能結束 =====
}