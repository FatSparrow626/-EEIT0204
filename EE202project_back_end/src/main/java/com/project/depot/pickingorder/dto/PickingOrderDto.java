package com.project.depot.pickingorder.dto;

import com.project.core.dto.response.EmployeeUserDto; // Assuming EmployeeUserDto is in com.project.core.dto.response
import java.time.LocalDate;
import java.util.List;

public class PickingOrderDto {
    private Long pickingOrderId;
    private String supplierName;
    private String orderNumber;
    private LocalDate expectedReceiptDate;
    private EmployeeUserDto handledBy; // Use EmployeeUserDto for handledBy
    private String status;
    private String note;
    private List<PickingOrderItemDto> items;

    // Getters and Setters
    public Long getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(Long pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDate getExpectedReceiptDate() {
        return expectedReceiptDate;
    }

    public void setExpectedReceiptDate(LocalDate expectedReceiptDate) {
        this.expectedReceiptDate = expectedReceiptDate;
    }

    public EmployeeUserDto getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(EmployeeUserDto handledBy) {
        this.handledBy = handledBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<PickingOrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<PickingOrderItemDto> items) {
        this.items = items;
    }
}