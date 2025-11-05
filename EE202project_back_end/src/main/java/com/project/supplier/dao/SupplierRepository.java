package com.project.supplier.dao;
import com.project.supplier.model.Supplier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 供應商資料存取介面 (DAO)
 * 繼承 JpaRepository 提供基本的 CRUD 操作。
 */
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    /**
     * 查詢所有啟用狀態 (active = true) 的供應商，並依供應商ID升冪排序。
     * @return 啟用供應商的列表
     */
    List<Supplier> findByActiveTrueOrderBySupplierId();

    //JPA會自動生成對應的SQL語句
    //方法名稱片段對應的SQL語法如下：
    /*
        方法名稱片段	                對應 SQL 語法
        findBy	                        SELECT 開頭
        ActiveTrue	                WHERE active = 1        JPA會把1轉成true
        OrderBySupplierId	        ORDER BY supplier_id ASC(升冪排序)
     */
}