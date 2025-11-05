package com.project.supplier.dao;

import com.project.supplier.model.PurchaseOrder;
import com.project.supplier.model.PurchaseOrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 訂單明細資料存取介面 (DAO)
 * 繼承 JpaRepository 提供基本的 CRUD 操作。
 */
public interface OrderItemRepository extends JpaRepository<PurchaseOrderItem, Integer> {
    /**
     * 根據採購訂單查詢其所有訂單明細。
     * @param order 採購訂單實體
     * @return 該訂單的所有訂單明細列表
     */
    List<PurchaseOrderItem> findByOrder(PurchaseOrder order);
}