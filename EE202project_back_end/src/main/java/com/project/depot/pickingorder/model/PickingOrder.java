package com.project.depot.pickingorder.model;

import com.project.employeeuser.model.EmployeeUser; // Assuming EmployeeUser model is in com.project.core.model
import com.project.depot.model.Material; // Assuming Material model is in com.project.depot.model
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "picking_orders")
public class PickingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pickingOrderId;

    private String supplierName;
    private String orderNumber;
    private LocalDate expectedReceiptDate;

    @ManyToOne
    @JoinColumn(name = "handled_by_user_id")
    private EmployeeUser handledBy;

    private String status; // e.g., "PENDING", "PARTIALLY_RECEIVED", "COMPLETED", "CANCELLED"
    private String note;

    @OneToMany(mappedBy = "pickingOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PickingOrderItem> items;

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

    public EmployeeUser getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(EmployeeUser handledBy) {
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

    public List<PickingOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PickingOrderItem> items) {
        this.items = items;
        if (items != null) {
            for (PickingOrderItem item : items) {
                item.setPickingOrder(this);
            }
        }
    }
}