package com.project.depot.dto.request;

import java.math.BigDecimal;

/**
 * 入庫單項目建立請求物件 (DTO)
 * 用於接收前端建立入庫單項目時傳遞的資料。
 */
public class InboundReceiptItemCreateRequest {
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 接收數量
     */
    private BigDecimal receivedQuantity;

    // Getters and Setters
    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public BigDecimal getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}