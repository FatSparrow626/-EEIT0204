package com.project.supplier.dao;

import com.project.supplier.model.PurchaseOrder;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 訂單資料存取介面 (DAO)
 * 繼承 JpaRepository 提供基本的 CRUD 操作。
 */
public interface OrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    /**
     * 查詢每月採購總金額。
     * 使用原生 SQL 查詢，將訂單日期轉換為年月格式，並按月分組計算總金額。
     * 
     * @return 包含每月採購金額的列表，每個元素是一個 Map，包含 'period' (年月) 和 'totalAmount' (總金額)。
     */
    @Query(value = "SELECT CONVERT(VARCHAR(7), order_date, 120) AS period, " +
            "SUM(sub_total) AS totalAmount " +
            "FROM purchase_orders " +
            "WHERE order_status='COMPLETED' " +
            "GROUP BY CONVERT(VARCHAR(7), order_date, 120) " +
            "ORDER BY period", nativeQuery = true)
    List<Map<String, Object>> findMonthlyTotal();

    /**
     * 查詢各供應商的採購總金額佔比。
     * 使用原生 SQL 查詢，連接 purchase_orders 和 core_suppliers 表，按供應商名稱分組計算總金額。
     * 
     * @return 包含各供應商採購總金額的列表，每個元素是一個 Map，包含 'supplierName' (供應商名稱) 和 'totalAmount'
     *         (總金額)。
     */
    @Query(value = "SELECT s.supplier_name AS supplierName, " +
            "SUM(o.sub_total) AS totalAmount " +
            "FROM purchase_orders o " +
            "JOIN core_suppliers s ON o.supplier_id = s.supplier_id " +
            "WHERE o.order_status='COMPLETED' " +
            "GROUP BY s.supplier_name " +
            "ORDER BY totalAmount DESC", nativeQuery = true)
    List<Map<String, Object>> findSupplierTotal();

}