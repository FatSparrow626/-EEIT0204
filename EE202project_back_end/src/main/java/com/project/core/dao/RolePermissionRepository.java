package com.project.core.dao;

import com.project.core.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    List<RolePermission> findByRoleId(Integer roleId);
    Optional<RolePermission> findByRoleIdAndPermissionCode(Integer roleId, String permissionCode);
}