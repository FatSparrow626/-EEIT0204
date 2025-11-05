package com.project.workorder.model;

import com.project.depot.model.Material;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 工單 - 工單領料明細實體類別
 * 對應資料表: outbound_work_order_materials
 */
@Entity
@Table(name = "outbound_work_order_materials", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "wo_id", "material_id" })
})
public class WorkOrderMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wo_material_id")
    private Long woMaterialId; // 領料ID (主鍵)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wo_id", nullable = false)
    private WorkOrder workOrder; // 工單

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material; // 需要領用的原料

    @Column(name = "requested_quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal requestedQuantity; // 需求數量

    @Column(name = "issued_quantity", precision = 18, scale = 4)
    private BigDecimal issuedQuantity; // 已發料數量

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // 狀態

    // Getters and Setters
    public Long getWoMaterialId() {
        return woMaterialId;
    }

    public void setWoMaterialId(Long woMaterialId) {
        this.woMaterialId = woMaterialId;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(BigDecimal requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public BigDecimal getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(BigDecimal issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}