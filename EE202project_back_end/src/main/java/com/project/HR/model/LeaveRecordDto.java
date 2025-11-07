package com.project.HR.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRecordDto {

    private String uuid;
    private String reason;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private BigDecimal hours;
    private String statusCode;
    private String statusName;
    private Integer employeeId;
    private String employeeName;
    private Integer agentId;
    private String agentName;
    private String leaveTypeName;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private List<LeaveAttachmentDto> attachments; // 假單附件List 因為附件可能有多筆
}
