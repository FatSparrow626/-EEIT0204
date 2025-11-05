package com.project.workorder.dto;

import java.math.BigDecimal;

/**
 * 領料請求物件 (DTO)
 * 用於接收前端領料的請求。
 */
public class PickingRequest {
    /**
     * 工單ID
     */
    private Long woId;
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 請求數量
     */
    private java.math.BigDecimal requestedQuantity;

    // Getters and Setters
    public Long getWoId() {
        return woId;
    }

    public void setWoId(Long woId) {
        this.woId = woId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public java.math.BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(BigDecimal requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }
}