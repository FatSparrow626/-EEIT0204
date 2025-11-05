package com.project.depot.dto;

import java.math.BigDecimal;

/**
 * 物料資料傳輸物件 (DTO)
 * 用於在服務層和控制器層之間傳輸物料資訊。
 */
public class MaterialDto {
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 物料名稱
     */
    private String materialName;
    /**
     * 價格
     */
    private BigDecimal price; 
    /**
     * 類別
     */
    private String category; 
    /**
     * 物料類型 (例如: PRODUCT, RAW_MATERIAL)
     */
    private String materialType; 
    /**
     * 現有庫存
     */
    private BigDecimal stockCurrent;
    /**
     * 單位
     */
    private String unit;
    /**
     * 物料描述
     */
    private String materialDescription;
    /**
     * 儲位
     */
    private String location;
    /**
     * 安全庫存
     */
    private BigDecimal safetyStock;
    /**
     * 再訂購點
     */
    private BigDecimal reorderLevel;
    /**
     * 是否啟用
     */
    private Boolean active = true;

    // Getters and Setters
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

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public BigDecimal getStockCurrent() {
        return stockCurrent;
    }

    public void setStockCurrent(BigDecimal stockCurrent) {
        this.stockCurrent = stockCurrent;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(BigDecimal safetyStock) {
        this.safetyStock = safetyStock;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}