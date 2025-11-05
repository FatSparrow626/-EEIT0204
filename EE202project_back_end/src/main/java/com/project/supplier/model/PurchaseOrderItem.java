package com.project.supplier.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 採購訂單明細實體類別
 * 對應資料表: purchase_order_items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity 
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

	/**
	 * 訂單明細ID (主鍵)
	 */
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name ="order_item_id")
	private int orderItemId;

	/**
	 * 訂單 (關聯至 PurchaseOrder 實體)
	 */
	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	@JsonBackReference
	private PurchaseOrder order;

	/**
	 * 物料ID
	 */
	@Column(name ="material_id")
	private int materialId;

	/**
	 * 數量
	 */
	@Column(name ="quantity")
	private BigDecimal quantity = BigDecimal.ZERO;

	/**
	 * 單價
	 */
	@Column(name ="unit_price")
	private double unitPrice;

	/**
	 * 交貨狀態
	 */
	@Column(name ="delivery_status")
	private String deliveryStatus;
	
	/**
	 * 已收貨數量
	 */
	@Column(name = "received_quantity")
	private BigDecimal receivedQuantity = BigDecimal.ZERO;

	/**
	 * 物料名稱 (瞬態屬性，不映射到資料庫)
	 */
	@Transient
	private String materialName;

	/**
	 * 物料 (關聯至 Material 實體)
	 */
	@ManyToOne
    @JoinColumn(name = "material_id", insertable = false, updatable = false)
    @JsonBackReference  // 序列化終點，避免循環引用
	private com.project.depot.model.Material material;

	/**
	 * 取得物料名稱 (瞬態屬性，不映射到資料庫)
	 */
	@Transient
    public String getMaterialName() {
        return material != null ? material.getMaterialName() : "無資料";
    }

	// 狀態的Getter / Setter
    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}