package com.project.core.dao;

import com.project.core.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
    
    // ===== 新增：角色管理功能開始 =====
    /**
     * 查詢角色名稱是否存在
     */
    boolean existsByRoleName(String roleName);
    
    /**
     * 根據角色名稱列表查詢角色
     */
    List<Role> findByRoleNameIn(List<String> roleNames);
    // ===== 新增：角色管理功能結束 =====
}