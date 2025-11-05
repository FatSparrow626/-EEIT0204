package com.project.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_logs")
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "log_type", nullable = false, length = 50)
    private String logType;

    @Column(name = "operation", nullable = false, length = 100)
    private String operation;

    @Column(name = "operator_employee_id")
    private Integer operatorEmployeeId;

    @Column(name = "operator_username", nullable = false, length = 50)
    private String operatorUsername;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id", length = 50)
    private String targetId;

    @Column(name = "target_name", length = 100)
    private String targetName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "module", length = 50)
    private String module;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
