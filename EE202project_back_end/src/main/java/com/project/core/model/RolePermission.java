package com.project.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "RolePermissions")
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "permission_code", nullable = false, length = 50)
    private String permissionCode;

    @Column(name = "granted_by", length = 50)
    private String grantedBy;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}
