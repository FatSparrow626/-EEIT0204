package com.project.bom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.project.bom.model.BomComponent;
import com.project.bom.service.BomComponentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * BOM 組件模組 API 控制器
 * 提供 BOM (物料清單) 組件的 RESTful 介面，包含查詢、新增、更新、刪除等操作。
 */
@RestController
@RequestMapping("/api/boms")
@Tag(name = "BOM組件管理", description = "處理物料清單(BOM)組件的相關操作")
public class BomComponentController {

    @Autowired
    private BomComponentService bomComponentService;

    /**
     * 新增一個 BOM 組件。
     * @param bomComponent 包含 BOM 組件資訊的實體。
     * @return 新增後的 BOM 組件實體。
     */
    @Operation(summary = "新增BOM組件", description = "新增一個新的物料清單組件")
    @PostMapping("/add")
    public BomComponent addBomComponent(@Parameter(description = "BOM組件物件", required = true) @RequestBody BomComponent bomComponent) {
        return bomComponentService.insertBomComponent(bomComponent);
    }

    /**
     * 根據ID獲取單一 BOM 組件。
     * @param id BOM 組件ID。
     * @return 如果找到則返回 BOM 組件，否則返回 null。
     */
    @Operation(summary = "根據ID獲取BOM組件", description = "根據BOM組件ID查詢單一組件的詳細資訊")
    @GetMapping("/{id}")
    public BomComponent getBomComponent(@Parameter(description = "BOM組件ID", required = true) @PathVariable Long id) {
        return bomComponentService.getBomComponentById(id);
    }

    /**
     * 根據父物料ID獲取其所有 BOM 組件。
     * @param materialId 父物料ID。
     * @return 該父物料的所有 BOM 組件列表。
     */
    @Operation(summary = "根據父物料ID獲取BOM組件", description = "根據父物料ID查詢其所有相關的BOM組件")
    @GetMapping("/material/{materialId}")
    public List<BomComponent> getBomComponentsByMaterial(@Parameter(description = "父物料ID", required = true) @PathVariable Long materialId) {
        return bomComponentService.getBomComponentsByParentMaterialId(materialId);
    }

    /**
     * 更新一個現有的 BOM 組件。
     * @param bomComponent 包含更新資訊的 BOM 組件實體。
     */
    @Operation(summary = "更新BOM組件", description = "更新一個現有的物料清單組件的資訊")
    @PutMapping("/update")
    public void updateBomComponent(@Parameter(description = "BOM組件物件", required = true) @RequestBody BomComponent bomComponent) {
        bomComponentService.updateBomComponent(bomComponent);
    }

    /**
     * 根據ID刪除 BOM 組件。
     * @param id BOM 組件ID。
     */
    @Operation(summary = "刪除BOM組件", description = "根據BOM組件ID刪除指定的組件")
    @DeleteMapping("/{id}")
    public void deleteBomComponent(@Parameter(description = "要刪除的BOM組件ID", required = true) @PathVariable Long id) {
        bomComponentService.deleteBomComponent(id);
    }
}