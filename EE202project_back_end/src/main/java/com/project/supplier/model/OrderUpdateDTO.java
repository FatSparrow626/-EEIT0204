package com.project.supplier.model;

import java.util.List;

import lombok.Data;

/**
 * 訂單更新資料傳輸物件 (DTO)
 * 用於接收前端更新訂單時傳遞的資料。
 */
@Data
public class OrderUpdateDTO {
    /**
     * 訂單ID
     */
    private int orderId;
    /**
     * 供應商ID
     */
    private int supplierId;
    /**
     * 訂單日期 (字串格式，例如: "YYYY-MM-DD")
     */
    private String orderDate;
    /**
     * 訂單狀態 (例如: "PENDING", "COMPLETED", "CANCELLED")
     */
    private String orderStatus;
    /**
     * 物料ID列表
     */
    private List<String> materialIds;
    /**
     * 數量列表
     */
    private List<String> quantities;
    /**
     * 單價列表
     */
    private List<String> unitPrices;
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	public List<String> getMaterialIds() {
		return materialIds;
	}
	public void setMaterialIds(List<String> materialIds) {
		this.materialIds = materialIds;
	}
	public List<String> getQuantities() {
		return quantities;
	}
	public void setQuantities(List<String> quantities) {
		this.quantities = quantities;
	}
	public List<String> getUnitPrices() {
		return unitPrices;
	}
	public void setUnitPrices(List<String> unitPrices) {
		this.unitPrices = unitPrices;
	}
    
}