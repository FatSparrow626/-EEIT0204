package com.project.depot.dao;

import com.project.depot.model.InboundReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 倉庫 - 入庫明細 DAO (Repository)
 */
@Repository
public interface InboundReceiptItemRepository extends JpaRepository<InboundReceiptItem, Integer> {
}