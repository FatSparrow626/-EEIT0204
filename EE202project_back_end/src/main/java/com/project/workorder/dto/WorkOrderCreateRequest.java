package com.project.workorder.dto;

import java.math.BigDecimal;

/**
 * 工單建立請求物件 (DTO)
 * 用於接收前端建立工單的請求。
 */
public class WorkOrderCreateRequest {
    /**
     * 工單號碼
     */
    private String woNumber;
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 需求數量
     */
    private java.math.BigDecimal requiredQuantity;
    /**
     * 狀態
     */
    private String status;

    // Getters and Setters
    public String getWoNumber() {
        return woNumber;
    }

    public void setWoNumber(String woNumber) {
        this.woNumber = woNumber;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public java.math.BigDecimal getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(BigDecimal requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}