package com.project.supplier.model;

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
 * 供應商實體類別
 * 對應資料表: core_suppliers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity 
@Table(name = "core_suppliers")
public class Supplier {

	/**
	 * 供應商ID (主鍵)
	 */
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "supplier_id")
	private int supplierId;

	/**
	 * 供應商名稱
	 */
	@Column(name = "supplier_name")
	private String supplierName;

	/**
	 * 聯絡人
	 */
	@Column(name = "pm")
	private String pm;

	/**
	 * 電話
	 */
	@Column(name = "supplier_phone")
	private String supplierPhone;

	/**
	 * 電子郵件
	 */
	@Column(name = "supplier_email")
	private String supplierEmail;

	/**
	 * 地址
	 */
	@Column(name = "supplier_address")
	private String supplierAddress;

	/**
	 * 備註
	 */
	@Column(name = "note")
	private String supplierNote;

	/**
	 * 是否啟用 (1=啟用，0=下架)
	 */
	@Column(name = "active")
	private boolean active;
	
}