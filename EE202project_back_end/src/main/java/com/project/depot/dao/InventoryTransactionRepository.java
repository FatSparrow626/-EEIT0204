package com.project.depot.dao;

import com.project.depot.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 倉庫 - 庫存交易紀錄 DAO (Repository)
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    /**
     * 查詢所有庫存交易紀錄，並使用 JOIN FETCH 立即載入相關的 Material 實體。
     * 這有助於避免 N+1 問題，並確保 Material 資訊在序列化時可用。
     * @return 包含 Material 資訊的庫存交易紀錄列表
     */
    @Query("SELECT t FROM InventoryTransaction t JOIN FETCH t.material")
    List<InventoryTransaction> findAllWithMaterial();
}