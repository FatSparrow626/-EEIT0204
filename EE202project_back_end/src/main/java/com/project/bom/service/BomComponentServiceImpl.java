package com.project.bom.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.project.bom.dao.BomComponentDAO;
import com.project.bom.model.BomComponent;

import org.springframework.transaction.annotation.Transactional;

/**
 * BOM 組件服務實作類別
 * 實作 BOM 組件相關的業務邏輯操作。
 */
@Service
public class BomComponentServiceImpl implements BomComponentService {

    private static final Logger logger = LoggerFactory.getLogger(BomComponentServiceImpl.class);

    @Autowired
    private BomComponentDAO bomComponentDAO;

    /**
     * 插入一個新的 BOM 組件。
     * 設定生效開始日期為當前時間，生效結束日期為 null。
     * 
     * @param bomComponent 要插入的 BomComponent 實體。
     * @return 插入後的 BomComponent 實體。
     */
    @Override
    public BomComponent insertBomComponent(BomComponent bomComponent) {
        bomComponent.setEffectiveStartDate(LocalDateTime.now());
        bomComponent.setEffectiveEndDate(null);
        bomComponentDAO.insertBomComponent(bomComponent);
        return bomComponent;
    }

    /**
     * 根據 BOM 組件ID獲取 BOM 組件。
     * 
     * @param bomComponentId BOM 組件的唯一識別ID。
     * @return 匹配的 BomComponent 實體，如果不存在則返回 null。
     */
    @Override
    public BomComponent getBomComponentById(Long bomComponentId) {
        return bomComponentDAO.getBomComponentById(bomComponentId);
    }

    /**
     * 根據父物料ID獲取所有相關的 BOM 組件。
     * 過濾掉 componentMaterialId 為 null 的組件。
     * 
     * @param parentMaterialId 父物料的唯一識別ID。
     * @return 相關的 BomComponent 實體列表。
     */
    @Override
    public List<BomComponent> getBomComponentsByParentMaterialId(Long parentMaterialId) {
        List<BomComponent> bomComponents = bomComponentDAO.getBomComponentsByParentMaterialId(parentMaterialId);
        System.out.println("BomComponentServiceImpl: Raw BOM components from DAO: " + bomComponents);
        // Filter out components where componentMaterialId is null
        List<BomComponent> filteredBomComponents = bomComponents.stream()
                .filter(component -> component.getComponentMaterialId() != null)
                .collect(java.util.stream.Collectors.toList());
        System.out.println("BomComponentServiceImpl: Filtered BOM components: " + filteredBomComponents);
        return filteredBomComponents;
    }

    /**
     * 更新一個現有的 BOM 組件。
     * 
     * @param bomComponent 要更新的 BomComponent 實體，必須包含有效的 bomComponentId。
     */
    @Override
    @Transactional
    public void updateBomComponent(BomComponent bomComponent) {
        logger.info("Attempting to update BOM component. Incoming data: {}", bomComponent);
        if (bomComponent == null || bomComponent.getBomComponentId() == null) {
            logger.warn("Update request received with null component or null ID. Aborting.");
            return;
        }

        BomComponent existingComponent = bomComponentDAO.getBomComponentById(bomComponent.getBomComponentId());

        if (existingComponent != null) {
            logger.info("Found existing component to update: {}", existingComponent);

            existingComponent.setComponentMaterialId(bomComponent.getComponentMaterialId());
            existingComponent.setQuantity(bomComponent.getQuantity());
            existingComponent.setNotes(bomComponent.getNotes());

            logger.info("Component state before calling DAO update: {}", existingComponent);
            bomComponentDAO.updateBomComponent(existingComponent);
            logger.info("Successfully called DAO to update component with ID: {}",
                    existingComponent.getBomComponentId());
        } else {
            logger.warn("No BOM component found with ID: {}. Update cannot be performed.",
                    bomComponent.getBomComponentId());
        }
    }

    /**
     * 根據 BOM 組件ID刪除 BOM 組件。
     * 
     * @param bomComponentId 要刪除的 BOM 組件的唯一識別ID。
     */
    @Override
    public void deleteBomComponent(Long bomComponentId) {
        bomComponentDAO.deleteBomComponent(bomComponentId);
    }
}