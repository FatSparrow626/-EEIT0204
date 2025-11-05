package com.project.depot.service;

import com.project.depot.dto.MaterialDto;
import java.util.List;
import java.util.Optional;

/**
 * 物料服務介面
 * 定義物料相關的業務邏輯操作。
 */
public interface MaterialService {
    /**
     * 查詢所有物料。
     * @return 物料DTO列表
     */
    List<MaterialDto> findAllMaterials();
    
    /**
     * 根據物料類型查詢物料。
     * @param materialType 物料類型 (例如: PRODUCT, RAW_MATERIAL)
     * @return 物料DTO列表
     */
    List<MaterialDto> findAllMaterialsByType(String materialType);
    
    /**
     * 根據ID查詢單一物料。
     * @param id 物料ID
     * @return Optional<MaterialDto> 如果找到則返回物料DTO，否則為空
     */
    Optional<MaterialDto> findMaterialById(Long id);
    
    /**
     * 新增物料。
     * @param materialDto 包含物料資訊的請求DTO
     * @return 儲存後的物料DTO
     */
    MaterialDto createMaterial(MaterialDto materialDto);
    
    /**
     * 更新物料。
     * @param id 物料ID
     * @param materialDto 包含更新資訊的請求DTO
     * @return 更新後的物料DTO
     */
    MaterialDto updateMaterial(Long id, MaterialDto materialDto);
    
    /**
     * 根據ID刪除物料。
     * @param id 物料ID
     */
    void deleteMaterialById(Long id);
    
    /**
     * 扣除物料庫存。
     * @param materialId 物料ID
     * @param quantity 扣除數量
     */
    void deductMaterialStock(Long materialId, java.math.BigDecimal quantity);
}