package com.project.core.dao;

import com.project.core.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByPermissionCode(String permissionCode);
    List<Permission> findByPermissionCodeIn(List<String> permissionCodes);
}