package com.project.depot.pickingorder.service;

import com.project.depot.pickingorder.model.PickingOrder;
import java.util.List;
import java.util.Optional;

public interface PickingOrderService {
    List<PickingOrder> getAllPickingOrders(String status);
    Optional<PickingOrder> getPickingOrderById(Long id);
    PickingOrder createPickingOrder(PickingOrder pickingOrder);
    PickingOrder updatePickingOrder(Long id, PickingOrder pickingOrder);
    void deletePickingOrder(Long id);
}