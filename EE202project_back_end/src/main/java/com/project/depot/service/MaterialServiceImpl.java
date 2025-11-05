package com.project.depot.service;

import com.project.depot.dao.MaterialRepository;
import com.project.depot.dto.MaterialDto;
import com.project.depot.model.Material;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.project.bom.service.BomComponentService; // Add this import
import com.project.bom.model.BomComponent; // Add this import

/**
 * 物料服務實作類別
 * 實作物料相關的業務邏輯操作。
 */
@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final BomComponentService bomComponentService; // 注入 BomComponentService

    /**
     * 建構子注入依賴。
     * @param materialRepository 物料資料庫操作介面
     * @param bomComponentService BOM 組件服務
     */
    @Autowired
    public MaterialServiceImpl(MaterialRepository materialRepository, BomComponentService bomComponentService) {
        this.materialRepository = materialRepository;
        this.bomComponentService = bomComponentService;
    }

    /**
     * 查詢所有物料。
     * @return 物料DTO列表
     */
    @Override
    public List<MaterialDto> findAllMaterials() {
        return materialRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據物料類型查詢物料。
     * @param materialType 物料類型 (例如: PRODUCT, RAW_MATERIAL)
     * @return 物料DTO列表
     */
    @Override
    public List<MaterialDto> findAllMaterialsByType(String materialType) {
        return materialRepository.findByMaterialType(materialType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據ID查詢單一物料。
     * @param id 物料ID
     * @return Optional<MaterialDto> 如果找到則返回物料DTO，否則為空
     */
    @Override
    public Optional<MaterialDto> findMaterialById(Long id) {
        return materialRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 新增物料。
     * @param materialDto 包含物料資訊的請求DTO
     * @return 儲存後的物料DTO
     */
    @Override
    @Transactional
    public MaterialDto createMaterial(MaterialDto materialDto) {
        Material material = convertToEntity(materialDto);
        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    /**
     * 更新物料。
     * @param id 物料ID
     * @param materialDto 包含更新資訊的請求DTO
     * @return 更新後的物料DTO
     */
    @Override
    @Transactional
    public MaterialDto updateMaterial(Long id, MaterialDto materialDto) {
        Material existingMaterial = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with ID: " + id));

        existingMaterial.setMaterialName(materialDto.getMaterialName());
        existingMaterial.setMaterialType(materialDto.getMaterialType()); // Set materialType
        existingMaterial.setStockCurrent(materialDto.getStockCurrent());
        existingMaterial.setUnit(materialDto.getUnit());
        existingMaterial.setMaterialDescription(materialDto.getMaterialDescription());
        existingMaterial.setLocation(materialDto.getLocation());
        existingMaterial.setSafetyStock(materialDto.getSafetyStock());
        existingMaterial.setReorderLevel(materialDto.getReorderLevel());
        existingMaterial.setActive(materialDto.getActive());

        Material updatedMaterial = materialRepository.save(existingMaterial);
        return convertToDto(updatedMaterial);
    }

    /**
     * 根據ID刪除物料。
     * @param id 物料ID
     */
    @Override
    @Transactional
    public void deleteMaterialById(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new EntityNotFoundException("Material not found with ID: " + id);
        }

        // 刪除所有引用該物料作為 parent_material_id 或 component_material_id 的 BOM 組件
        List<BomComponent> bomComponentsAsParent = bomComponentService.getBomComponentsByParentMaterialId(id);
        for (BomComponent bomComponent : bomComponentsAsParent) {
            bomComponentService.deleteBomComponent(bomComponent.getBomComponentId());
        }

        // 刪除所有引用該物料作為 component_material_id 的 BOM 組件 (如果需要)
        // List<BomComponent> bomComponentsAsComponent =
        // bomComponentService.getBomComponentsByComponentProductId(id);
        // for (BomComponent bomComponent : bomComponentsAsComponent) {
        // bomComponentService.deleteBomComponent(bomComponent.getBomComponentId());
        // }

        materialRepository.deleteById(id);
    }

    /**
     * 扣除物料庫存。
     * @param materialId 物料ID
     * @param quantity 扣除數量
     */
    @Override
    @Transactional
    public void deductMaterialStock(Long materialId, BigDecimal quantity) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with ID: " + materialId));

        if (material.getStockCurrent().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Insufficient stock for material ID: " + materialId + ". Available: "
                    + material.getStockCurrent() + ", Requested: " + quantity);
        }

        material.setStockCurrent(material.getStockCurrent().subtract(quantity));
        materialRepository.save(material);
    }

    /**
     * 將 Material 實體轉換為 MaterialDto DTO。
     * @param material Material 實體
     * @return MaterialDto DTO
     */
    private MaterialDto convertToDto(Material material) {
        MaterialDto dto = new MaterialDto();
        dto.setMaterialId(material.getMaterialId());
        dto.setMaterialName(material.getMaterialName()); // Corrected: Use material.getMaterialName()
        dto.setPrice(material.getPrice()); // Set price
        dto.setCategory(material.getCategory()); // Set category
        dto.setMaterialType(material.getMaterialType()); // Set materialType
        dto.setStockCurrent(material.getStockCurrent());
        dto.setUnit(material.getUnit());
        dto.setMaterialDescription(material.getMaterialDescription());
        dto.setLocation(material.getLocation());
        dto.setSafetyStock(material.getSafetyStock());
        dto.setReorderLevel(material.getReorderLevel());
        dto.setActive(material.isActive());
        return dto;
    }

    /**
     * 將 MaterialDto DTO 轉換為 Material 實體。
     * @param materialDto MaterialDto DTO
     * @return Material 實體
     */
    private Material convertToEntity(MaterialDto materialDto) {
        Material material = new Material();
        material.setMaterialId(materialDto.getMaterialId());
        material.setMaterialName(materialDto.getMaterialName());
        material.setPrice(materialDto.getPrice()); // Set price
        material.setCategory(materialDto.getCategory()); // Set category
        material.setMaterialType(materialDto.getMaterialType()); // Set materialType
        material.setStockCurrent(materialDto.getStockCurrent());
        material.setUnit(materialDto.getUnit());
        material.setMaterialDescription(materialDto.getMaterialDescription());
        material.setLocation(materialDto.getLocation());
        material.setSafetyStock(materialDto.getSafetyStock());
        material.setReorderLevel(materialDto.getReorderLevel());
        material.setActive(materialDto.getActive());
        return material;
    }
}