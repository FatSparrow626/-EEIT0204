package com.project.depot.dao;

import com.project.depot.model.InboundReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 倉庫 - 入庫主檔 DAO (Repository)
 */
@Repository
public interface InboundReceiptRepository extends JpaRepository<InboundReceipt, Integer> {

    @Query("SELECT DISTINCT ir FROM InboundReceipt ir LEFT JOIN FETCH ir.purchaseOrder LEFT JOIN FETCH ir.handledBy LEFT JOIN FETCH ir.items item LEFT JOIN FETCH item.material")
    List<InboundReceipt> findAllWithDetails();
}