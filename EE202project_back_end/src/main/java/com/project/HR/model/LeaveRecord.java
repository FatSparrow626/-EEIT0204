package com.project.HR.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.employeeuser.model.EmployeeUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hr_leave_record")
@Getter
@Setter
@ToString(exclude = { "employee", "agent", "leaveType", "status", "attachments" })
@EqualsAndHashCode(of = "uuid")
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRecord implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "leave_uuid", length = 36, nullable = false)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", referencedColumnName = "employee_user_id", nullable = false)
    private EmployeeUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", referencedColumnName = "employee_user_id")
    private EmployeeUser agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private LeaveType leaveType;

    @Column(length = 200)
    private String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy/MM/dd HH")
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy/MM/dd HH")
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal hours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private LeaveStatus status;

    @OneToMany(mappedBy = "leaveRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LeaveAttachment> attachments = new HashSet<>();

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "amendment_count", nullable = false)
    private int amendmentCount = 0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}