package com.project.workorder.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 生產工單請求物件 (DTO)
 * 用於接收前端建立生產工單的請求，包含成品資訊及所需扣除的原料清單。
 */
public class ProductionOrderRequest {
    /**
     * 工單號碼
     */
    private String woNumber;
    /**
     * 成品物料ID
     */
    private Long materialId; 
    /**
     * 成品需求數量
     */
    private BigDecimal requiredQuantity; 
    /**
     * 工單狀態
     */
    private String status;
    /**
     * 需要扣除的原料列表
     */
    private List<MaterialDeductionDto> materialsToDeduct;

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

    public BigDecimal getRequiredQuantity() {
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

    public List<MaterialDeductionDto> getMaterialsToDeduct() {
        return materialsToDeduct;
    }

    public void setMaterialsToDeduct(List<MaterialDeductionDto> materialsToDeduct) {
        this.materialsToDeduct = materialsToDeduct;
    }
}