package com.project.workorder.dto;

/**
 * 工單領料明細資料傳輸物件 (DTO)
 * 用於向前端傳輸領料明細資訊。
 */
public class WorkOrderMaterialDto {
    /**
     * 領料ID
     */
    private Integer woMaterialId;
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 物料名稱
     */
    private String materialName;
    /**
     * 需求數量
     */
    private java.math.BigDecimal requestedQuantity;
    /**
     * 已發料數量
     */
    private java.math.BigDecimal issuedQuantity;
    /**
     * 狀態
     */
    private String status;

    // Constructors
    public WorkOrderMaterialDto() {
    }

    public WorkOrderMaterialDto(Integer woMaterialId, Long materialId, String materialName,
            java.math.BigDecimal requestedQuantity, java.math.BigDecimal issuedQuantity, String status) {
        this.woMaterialId = woMaterialId;
        this.materialId = materialId;
        this.materialName = materialName;
        this.requestedQuantity = requestedQuantity;
        this.issuedQuantity = issuedQuantity;
        this.status = status;
    }

    // Getters and Setters
    public Integer getWoMaterialId() {
        return woMaterialId;
    }

    public void setWoMaterialId(Integer woMaterialId) {
        this.woMaterialId = woMaterialId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public java.math.BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(java.math.BigDecimal requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public java.math.BigDecimal getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(java.math.BigDecimal issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}