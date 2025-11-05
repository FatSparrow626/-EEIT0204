package com.project.workorder.dao;

import com.project.workorder.model.WorkOrderMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工單 - 工單領料明細資料存取介面 (DAO)
 * 繼承 JpaRepository 提供基本的 CRUD 操作。
 */
@Repository
public interface WorkOrderMaterialRepository extends JpaRepository<WorkOrderMaterial, Long> {
    /**
     * 根據工單ID查詢所有領料明細。
     * @param woId 工單ID
     * @return 領料明細列表
     */
    List<WorkOrderMaterial> findByWorkOrder_WoId(Long woId);
}