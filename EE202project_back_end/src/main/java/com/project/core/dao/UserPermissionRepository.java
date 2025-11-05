package com.project.core.dao;

import com.project.core.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Integer> {
    List<UserPermission> findByEmployeeId(Integer employeeId);
    Optional<UserPermission> findByEmployeeIdAndPermissionCode(Integer employeeId, String permissionCode);
}