package com.project.depot.pickingorder.service;

import com.project.depot.pickingorder.dao.PickingOrderRepository;
import com.project.depot.pickingorder.model.PickingOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PickingOrderServiceImpl implements PickingOrderService {

    @Autowired
    private PickingOrderRepository pickingOrderRepository;

    @Override
    public List<PickingOrder> getAllPickingOrders(String status) {
        if (status != null && !status.isEmpty()) {
            return pickingOrderRepository.findByStatus(status);
        } else {
            return pickingOrderRepository.findAll();
        }
    }

    @Override
    public Optional<PickingOrder> getPickingOrderById(Long id) {
        return pickingOrderRepository.findById(id);
    }

    @Override
    public PickingOrder createPickingOrder(PickingOrder pickingOrder) {
        return pickingOrderRepository.save(pickingOrder);
    }

    @Override
    public PickingOrder updatePickingOrder(Long id, PickingOrder pickingOrder) {
        if (pickingOrderRepository.existsById(id)) {
            pickingOrder.setPickingOrderId(id);
            return pickingOrderRepository.save(pickingOrder);
        }
        return null; // Or throw an exception
    }

    @Override
    public void deletePickingOrder(Long id) {
        pickingOrderRepository.deleteById(id);
    }
}