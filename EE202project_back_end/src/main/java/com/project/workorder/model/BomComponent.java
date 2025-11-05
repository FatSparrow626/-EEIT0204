package com.project.workorder.model;

import com.project.depot.model.Material;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工單 - 物料清單 (BOM) 實體類別
 * 對應資料表: bom_components
 */
@Entity(name = "WorkOrderBomComponent")
@Table(name = "bom_components", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parent_material_id", "component_material_id"})
})
public class BomComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bom_component_id")
    private Integer bomComponentId; // BOM 組件ID (主鍵)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_material_id", nullable = false)
    private Material parentMaterial; // 被組裝的產品 (父物料)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_material_id", nullable = false)
    private Material componentMaterial; // 組成父產品的零件 (子物料)

    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity; // 組成數量

    @Lob
    @Column(name = "notes")
    private String notes; // 備註

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 建立時間

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 更新時間

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getBomComponentId() {
        return bomComponentId;
    }

    public void setBomComponentId(Integer bomComponentId) {
        this.bomComponentId = bomComponentId;
    }

    public Material getParentMaterial() {
        return parentMaterial;
    }

    public void setParentMaterial(Material parentMaterial) {
        this.parentMaterial = parentMaterial;
    }

    public Material getComponentMaterial() {
        return componentMaterial;
    }

    public void setComponentMaterial(Material componentMaterial) {
        this.componentMaterial = componentMaterial;
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