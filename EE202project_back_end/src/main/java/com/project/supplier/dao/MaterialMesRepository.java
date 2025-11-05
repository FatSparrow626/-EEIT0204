package com.project.supplier.dao;

import com.project.depot.model.Material;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 物料資料存取介面 (DAO)
 * 繼承 JpaRepository 提供基本的 CRUD 操作。
 */
public interface MaterialMesRepository extends JpaRepository<Material, Integer> {
    /**
     * 查詢所有啟用狀態 (active = true) 的物料，並依物料ID升冪排序。
     * @return 啟用物料的列表
     */
    List<Material> findByActiveTrueOrderByMaterialId();

    /**
     * 查詢指定父物料 (parentMaterialId) 的 BOM 中未使用的啟用狀態物料。
     * @param parentMaterialId 父物料的ID
     * @return 未使用的物料列表
     */
    @Query("SELECT m FROM Material m WHERE m.active = true AND m.materialId NOT IN (SELECT bc.componentMaterialId FROM BomComponent bc WHERE bc.parentMaterialId = :parentMaterialId)")
    List<Material> findUnusedMaterials(@Param("parentMaterialId") Long parentMaterialId);
}
