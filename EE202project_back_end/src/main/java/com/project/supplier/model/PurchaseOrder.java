package com.project.supplier.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 採購訂單主表實體類別
 * 對應資料表: purchase_orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity 
@Table(name = "purchase_orders")

public class PurchaseOrder {

	/**
	 * 訂單ID (主鍵)
	 */
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name ="order_id")
	private int orderId;

	/**
	 * 供應商 (關聯至 Supplier 實體)
	 */
	@ManyToOne
	@JoinColumn(name = "supplier_id" , nullable = false)
	private Supplier supplier;

	/**
	 * 訂單日期
	 */
	@Column(name ="order_date")
	private String orderDate;

	/**
	 * 訂單狀態 (例如: PENDING, COMPLETED)
	 */
	@Column(name ="order_status")
	private String orderStatus;

	/**
	 * 訂單總金額
	 */
	@Column(name ="sub_total")
	private double subTotal;

	/**
	 * 訂單明細列表
	 */
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<PurchaseOrderItem> itemList;

	/**
	 * 取得供應商名稱 (瞬態屬性，不映射到資料庫)
	 */
	@Transient
	private String getSupplierName() {
		return supplier != null ? supplier.getSupplierName() : "無資料";
	}

}
