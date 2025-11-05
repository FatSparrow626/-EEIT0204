package com.project.HR.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateLeaveRequestWithAttachments {
    // 原有的表單資料
    private String reason;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private BigDecimal hours;

    // 新增的欄位：要刪除的附件 storedFileName 列表
    private List<String> attachmentsToDelete;
}