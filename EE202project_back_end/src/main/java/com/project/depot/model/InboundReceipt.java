package com.project.depot.model;

import com.project.employeeuser.model.EmployeeUser;
import com.project.supplier.model.PurchaseOrder;
import com.project.depot.pickingorder.model.PickingOrder;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet; // 引入 HashSet
import java.util.Set;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import com.fasterxml.jackson.annotation.JsonManagedReference; // 引入 JsonManagedReference

/**
 * 倉庫 - 入庫主表實體類別
 * 對應資料表: inbound_receipts
 */
@Entity
@Table(name = "inbound_receipts")
public class InboundReceipt {

    /**
     * 入庫ID (主鍵)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_id")
    private Long inboundId;

    /**
     * 採購訂單 (可為空)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private PurchaseOrder purchaseOrder;

    /**
     * 領料單 (可為空)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picking_order_id")
    private PickingOrder pickingOrder;

    /**
     * 入庫日期
     */
    
    @Column(name = "inbound_date", nullable = false)
    private LocalDate inboundDate;

    /**
     * 單據類型
     */
    @Column(name = "receipt_type", nullable = false, length = 50)
    private String receiptType = "PURCHASE";

    /**
     * 狀態
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 處理人員
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by_user_id")
    private EmployeeUser handledBy;

    /**
     * 備註
     */
    @Column(name = "note", length = 200)
    private String note;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 入庫明細
     */
    @JsonManagedReference // 解決循環引用問題
    @OneToMany(mappedBy = "inboundReceipt", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<InboundReceiptItem> items;

    @PrePersist
    protected void onCreate() {
        // inboundDate = LocalDate.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getInboundId() {
        return inboundId;
    }

    public void setInboundId(Long inboundId) {
        this.inboundId = inboundId;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public PickingOrder getPickingOrder() {
        return pickingOrder;
    }

    public void setPickingOrder(PickingOrder pickingOrder) {
        this.pickingOrder = pickingOrder;
    }

    public LocalDate getInboundDate() {
        return inboundDate;
    }

    public void setInboundDate(LocalDate inboundDate) {
        this.inboundDate = inboundDate;
    }

    public String getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(String receiptType) {
        this.receiptType = receiptType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EmployeeUser getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(EmployeeUser handlerUser) {
        this.handledBy = handlerUser;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public Set<InboundReceiptItem> getItems() {
        return items;
    }

    public void setItems(Set<InboundReceiptItem> items) {
        this.items = items;
    }

    /**
     * 輔助方法：添加一個入庫明細項目，並自動設定反向關聯。
     * 
     * @param item 要添加的入庫明細項目
     */
    public void addItem(InboundReceiptItem item) {
        if (items == null) {
            items = new HashSet<>();
        }
        items.add(item);
        item.setInboundReceipt(this); // 設定反向關聯
    }
}