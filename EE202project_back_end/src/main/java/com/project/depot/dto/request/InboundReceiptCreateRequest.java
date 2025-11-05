package com.project.depot.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * 入庫單建立請求物件 (DTO)
 * 用於接收前端建立入庫單時傳遞的資料。
 */
public class InboundReceiptCreateRequest {
    /**
     * 入庫日期
     */
    private LocalDate inboundDate;
    /**
     * 入庫單狀態
     */
    private String status;
    /**
     * 經手人 (使用者名稱)
     */
    private String handledBy;
    /**
     * 備註
     */
    private String note;
    /**
     * 關聯的領料單ID (可選)
     */
    private Long pickingOrderId; 
    /**
     * 關聯的採購訂單ID (可選)
     */
    private Long purchaseOrderId;
    /**
     * 入庫項目列表
     */
    private List<InboundReceiptItemCreateRequest> items;

    // Getters and Setters
    public LocalDate getInboundDate() {
        return inboundDate;
    }

    public void setInboundDate(LocalDate inboundDate) {
        this.inboundDate = inboundDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(Long pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public List<InboundReceiptItemCreateRequest> getItems() {
        return items;
    }

    public void setItems(List<InboundReceiptItemCreateRequest> items) {
        this.items = items;
    }
}