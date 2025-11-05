package com.project.depot.dto;

import com.project.depot.model.InboundReceiptStatus;
import com.project.depot.pickingorder.dto.PickingOrderDto;
import java.time.LocalDate;
import java.util.List;

public class InboundReceiptDTO {
    private LocalDate inboundDate;
    private InboundReceiptStatus status;
    private String handledBy; // 新增經手人欄位
    private String note; // 新增備註欄位
    private PickingOrderDto pickingOrder; // 新增領料單欄位
    private List<InboundReceiptItemDTO> items;

    public LocalDate getInboundDate() {
        return inboundDate;
    }

    public void setInboundDate(LocalDate inboundDate) {
        this.inboundDate = inboundDate;
    }

    public InboundReceiptStatus getStatus() {
        return status;
    }

    public void setStatus(InboundReceiptStatus status) {
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

    public List<InboundReceiptItemDTO> getItems() {
        return items;
    }

    public void setItems(List<InboundReceiptItemDTO> items) {
        this.items = items;
    }

    public PickingOrderDto getPickingOrder() {
        return pickingOrder;
    }

    public void setPickingOrder(PickingOrderDto pickingOrder) {
        this.pickingOrder = pickingOrder;
    }
}