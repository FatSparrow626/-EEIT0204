package com.project.HR.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LeaveRecordFilter {
    private String viewMode;
    private String statusFilter;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
}
