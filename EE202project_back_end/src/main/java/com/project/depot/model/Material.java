package com.project.depot.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 倉庫 - 物料實體類別 (整合產品與庫存資訊)
 * 對應資料表: core_materials
 */
@Entity
@Table(name = "core_materials")
public class Material {

    /**
     * 物料ID (主鍵)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Re-add IDENTITY for Material
    @Column(name = "material_id")
    private Long materialId; 

    /**
     * 物料名稱
     */
    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName; 

    /**
     * 價格
     */
    @Column(name = "price", precision = 18, scale = 4) 
    private BigDecimal price;

    /**
     * 類別
     */
    @Column(name = "category", length = 255) 
    private String category;

    /**
     * 物料類型 (PRODUCT, RAW_MATERIAL)
     */
    @Column(name = "material_type", nullable = false, length = 20) 
    private String materialType; 

    /**
     * 單位
     */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit; 

    /**
     * 物料描述
     */
    @Column(name = "material_description", length = 200)
    private String materialDescription; 

    /**
     * 儲位
     */
    @Column(name = "location", length = 50)
    private String location; 

    /**
     * 現有庫存
     */
    @Column(name = "stock_current", precision = 18, scale = 4)
    private BigDecimal stockCurrent = BigDecimal.ZERO; 

    /**
     * 已預留庫存
     */
    @Column(name = "stock_reserved", precision = 18, scale = 4)
    private BigDecimal stockReserved = BigDecimal.ZERO; 

    /**
     * 在途庫存
     */
    @Column(name = "stock_in_shipping", precision = 18, scale = 4)
    private BigDecimal stockInShipping = BigDecimal.ZERO; 

    /**
     * 安全庫存
     */
    @Column(name = "safety_stock")
    private BigDecimal safetyStock; 

    /**
     * 再訂購點
     */
    @Column(name = "reorder_level")
    private BigDecimal reorderLevel; 

    /**
     * 是否啟用
     */
    @Column(name = "active")
    private boolean active = true; 

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

    public BigDecimal getStockCurrent() {
        return stockCurrent;
    }

    public void setStockCurrent(BigDecimal stockCurrent) {
        this.stockCurrent = stockCurrent;
    }

    public BigDecimal getStockReserved() {
        return stockReserved;
    }

    public void setStockReserved(BigDecimal stockReserved) {
        this.stockReserved = stockReserved;
    }

    public BigDecimal getStockInShipping() {
        return stockInShipping;
    }

    public void setStockInShipping(BigDecimal stockInShipping) {
        this.stockInShipping = stockInShipping;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
