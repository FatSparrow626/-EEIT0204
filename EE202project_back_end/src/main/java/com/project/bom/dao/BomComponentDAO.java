package com.project.bom.dao;

import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.project.bom.model.BomComponent;

/**
 * BOM 組件資料存取介面 (DAO)
 * 使用 MyBatis 映射到資料庫操作。
 */
@Mapper
@Repository
public interface BomComponentDAO {
    /**
     * 插入一個新的 BOM 組件。
     * @param bomComponent 要插入的 BomComponent 實體。
     */
    void insertBomComponent(BomComponent bomComponent);

    /**
     * 根據 BOM 組件ID獲取 BOM 組件。
     * @param bomComponentId BOM 組件的唯一識別ID。
     * @return 匹配的 BomComponent 實體，如果不存在則返回 null。
     */
    BomComponent getBomComponentById(@Param("bomComponentId") Long bomComponentId);

    /**
     * 根據父物料ID獲取所有相關的 BOM 組件。
     * @param parentMaterialId 父物料的唯一識別ID。
     * @return 相關的 BomComponent 實體列表。
     */
    List<BomComponent> getBomComponentsByParentMaterialId(@Param("parentMaterialId") Long parentMaterialId);

    /**
     * 更新一個現有的 BOM 組件。
     * @param bomComponent 要更新的 BomComponent 實體，必須包含有效的 bomComponentId。
     */
    void updateBomComponent(BomComponent bomComponent);

    /**
     * 根據 BOM 組件ID刪除 BOM 組件。
     * @param bomComponentId 要刪除的 BOM 組件的唯一識別ID。
     */
    void deleteBomComponent(@Param("bomComponentId") Long bomComponentId);
}