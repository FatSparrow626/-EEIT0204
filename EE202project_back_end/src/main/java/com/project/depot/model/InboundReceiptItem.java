package com.project.depot.model;

import com.project.supplier.model.PurchaseOrderItem;
import jakarta.persistence.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference; // 引入 JsonBackReference

/**
 * 倉庫 - 入庫明細實體類別
 * 對應資料表: inbound_receipt_items
 */
@Entity
@Table(name = "inbound_receipt_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"inbound_id", "material_id"})
})
public class InboundReceiptItem {

    /**
     * 入庫明細ID (主鍵)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_item_id")
    private Long inboundItemId; 

    /**
     * 入庫主檔
     */
    @JsonBackReference // 解決循環引用問題
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", nullable = false)
    private InboundReceipt inboundReceipt; 

    /**
     * 物料ID，用於接收前端傳遞的 ID (瞬態屬性，JPA 將忽略此字段)
     */
    @Transient 
    private Integer materialId; 

    /**
     * 物料
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material; 

    /**
     * 接收數量
     */
    @Column(name = "received_quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal receivedQuantity; 

    /**
     * 採購訂單明細 (可為空)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id")
    private PurchaseOrderItem purchaseOrderItem; 

    // Getters and Setters
    public Long getInboundItemId() {
        return inboundItemId;
    }

    public void setInboundItemId(Long inboundItemId) {
        this.inboundItemId = inboundItemId;
    }

    public InboundReceipt getInboundReceipt() {
        return inboundReceipt;
    }

    public void setInboundReceipt(InboundReceipt inboundReceipt) {
        this.inboundReceipt = inboundReceipt;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public BigDecimal getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public PurchaseOrderItem getPurchaseOrderItem() {
        return purchaseOrderItem;
    }

    public void setPurchaseOrderItem(PurchaseOrderItem purchaseOrderItem) {
        this.purchaseOrderItem = purchaseOrderItem;
    }
}