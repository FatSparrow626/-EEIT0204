package com.project.depot.pickingorder.dao;

import com.project.depot.pickingorder.model.PickingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickingOrderRepository extends JpaRepository<PickingOrder, Long> {
    List<PickingOrder> findByStatus(String status);
}