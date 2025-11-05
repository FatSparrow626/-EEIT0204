package com.project.depot.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 入庫單回應物件 (DTO)
 * 用於向前端傳輸入庫單資訊。
 */
public class InboundReceiptResponse {
    /**
     * 入庫ID
     */
    private Integer inboundId;
    /**
     * 訂單ID
     */
    private Long purchaseOrderId;
    /**
     * 入庫日期
     */
    private LocalDate inboundDate;
    /**
     * 入庫單狀態
     */
    private String status;
    /**
     * 經手人 (姓名或使用者名稱)
     */
    private UserDto handledBy;
    /**
     * 備註
     */
    private String note;
    /**
     * 入庫項目列表
     */
    private List<InboundReceiptItemResponse> items;

    // Getters and Setters
    public Integer getInboundId() {
        return inboundId;
    }

    public void setInboundId(Integer inboundId) {
        this.inboundId = inboundId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

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

    public UserDto getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(UserDto handledBy) {
        this.handledBy = handledBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<InboundReceiptItemResponse> getItems() {
        return items;
    }

    public void setItems(List<InboundReceiptItemResponse> items) {
        this.items = items;
    }
}