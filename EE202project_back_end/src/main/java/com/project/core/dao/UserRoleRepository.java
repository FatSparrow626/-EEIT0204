package com.project.core.dao;

import com.project.core.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findByEmployeeId(Integer employeeId);
    Optional<UserRole> findByEmployeeIdAndRoleId(Integer employeeId, Integer roleId);
    
    // ===== 新增：角色管理功能開始 =====
    /**
     * 查詢具有特定角色的所有員工
     */
    List<UserRole> findByRoleId(Integer roleId);
    
    /**
     * 刪除員工的特定角色
     */
    void deleteByEmployeeIdAndRoleId(Integer employeeId, Integer roleId);
    
    /**
     * 刪除員工的所有角色
     */
    void deleteByEmployeeId(Integer employeeId);
    
    /**
     * 檢查員工是否具有特定角色
     */
    boolean existsByEmployeeIdAndRoleId(Integer employeeId, Integer roleId);
    
    /**
     * 計算具有特定角色的員工數量
     */
    long countByRoleId(Integer roleId);
    // ===== 新增：角色管理功能結束 =====


    // === 0808 測試 新增：用角色「名稱」找員工 IDs（重點） ===
    // r.roleName 對照 com.project.core.model.Role 的欄位

    //多角色
      @Query("""
           SELECT DISTINCT ur.employeeId
           FROM UserRole ur
           JOIN Role r ON ur.roleId = r.roleId
           WHERE r.roleName IN :roleNames
           """)
    List<Integer> findEmployeeIdsByRoleNames(@Param("roleNames") List<String> roleNames);

}