package com.project.workorder.dao;

import com.project.workorder.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 工單 - 工單主檔資料存取介面 (DAO)
 * 繼承 JpaRepository 提供基本的 CRUD 操作。
 */
@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
}