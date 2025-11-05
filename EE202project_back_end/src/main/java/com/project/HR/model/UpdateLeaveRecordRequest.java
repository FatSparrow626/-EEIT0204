package com.project.HR.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateLeaveRecordRequest {
    private String reason;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private BigDecimal hours;
}
