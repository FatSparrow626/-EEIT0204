package com.project.bom.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BOM (物料清單) 組件實體類別
 * 對應資料表: bom_components
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bom_components")
public class BomComponent {

    /**
     * BOM 組件ID (主鍵)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bom_component_id")
    private Long bomComponentId;

    /**
     * 被組裝的產品 (父物料) ID
     */
    @Column(name = "parent_material_id")
    private Long parentMaterialId;

    /**
     * 組成父產品的零件 (子物料) ID
     */
    @Column(name = "component_material_id")
    private Long componentMaterialId;

    /**
     * 組成數量
     */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /**
     * 位置編號
     */
    @Column(name = "position_number")
    private Integer positionNumber;

    /**
     * 備註
     */
    @Column(name = "notes")
    private String notes;

    /**
     * 生效開始日期
     */
    @Column(name = "effective_start_date")
    private LocalDateTime effectiveStartDate;

    /**
     * 生效結束日期
     */
    @Column(name = "effective_end_date")
    private LocalDateTime effectiveEndDate;

    /**
     * 建立時間
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters for new fields
    public Long getParentMaterialId() {
        return parentMaterialId;
    }

    public void setParentMaterialId(Long parentMaterialId) {
        this.parentMaterialId = parentMaterialId;
    }

    public Long getComponentMaterialId() {
        return componentMaterialId;
    }

    public void setComponentMaterialId(Long componentMaterialId) {
        this.componentMaterialId = componentMaterialId;
    }

    public Long getBomComponentId() {
        return bomComponentId;
    }

    public void setBomComponentId(Long bomComponentId) {
        this.bomComponentId = bomComponentId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(LocalDateTime effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public LocalDateTime getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(LocalDateTime effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}