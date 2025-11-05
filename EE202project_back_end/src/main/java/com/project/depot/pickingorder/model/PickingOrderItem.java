package com.project.depot.pickingorder.model;

import com.project.depot.model.Material; // Assuming Material model is in com.project.depot.model
import jakarta.persistence.*;

@Entity
@Table(name = "picking_order_items")
public class PickingOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "picking_order_id")
    private PickingOrder pickingOrder;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    private java.math.BigDecimal expectedQuantity;
    private java.math.BigDecimal receivedQuantity; // To track how much has been received against this item

    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public PickingOrder getPickingOrder() {
        return pickingOrder;
    }

    public void setPickingOrder(PickingOrder pickingOrder) {
        this.pickingOrder = pickingOrder;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public java.math.BigDecimal getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(java.math.BigDecimal expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public java.math.BigDecimal getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(java.math.BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}