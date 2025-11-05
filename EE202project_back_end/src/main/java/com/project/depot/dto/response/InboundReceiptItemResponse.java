package com.project.depot.dto.response;

import java.math.BigDecimal;

/**
 * 入庫單項目回應物件 (DTO)
 * 用於向前端傳輸入庫單項目資訊。
 */
public class InboundReceiptItemResponse {
    /**
     * 入庫項目ID
     */
    private Integer inboundItemId;
    /**
     * 物料ID
     */
    private Integer materialId;
    /**
     * 物料名稱
     */
    private String materialName;
    /**
     * 接收數量
     */
    private BigDecimal receivedQuantity;

    // Getters and Setters
    public Integer getInboundItemId() {
        return inboundItemId;
    }

    public void setInboundItemId(Integer inboundItemId) {
        this.inboundItemId = inboundItemId;
    }

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public BigDecimal getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}