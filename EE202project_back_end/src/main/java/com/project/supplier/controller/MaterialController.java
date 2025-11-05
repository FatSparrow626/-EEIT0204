package com.project.supplier.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.depot.model.Material;
import com.project.supplier.service.MaterialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 物料模組 API 控制器
 * 提供物料相關的 RESTful 介面，包含查詢操作。
 */
// @CrossOrigin(origins = "http://localhost:5173")
@RestController //= @Controller + @ResponseBody  用於回傳JSON給前端
@RequestMapping("/api/material")
@Tag(name = "物料管理", description = "處理物料資訊的相關操作")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * 取得所有啟用狀態的物料列表。
     * @return 啟用物料的列表
     */
    @Operation(summary = "獲取所有啟用物料", description = "獲取所有啟用狀態的物料列表")
    @GetMapping("/list")
    public List<Material> listMaterials() {
        // 取得所有尚未下架的物料
        return materialService.getActiveMaterials(); // 返回物料清單頁面的JSP
    }

    /**
     * 取得指定父物料的 BOM 中未使用的啟用狀態物料列表。
     * @param parentMaterialId 父物料的ID
     * @return 未使用的物料列表
     */
    @Operation(summary = "獲取未使用的BOM物料", description = "獲取指定父物料的BOM中未使用的啟用狀態物料列表")
    @GetMapping("/unused/{parentMaterialId}")
    public List<Material> listUnusedMaterials(@Parameter(description = "父物料的ID", required = true) @PathVariable Long parentMaterialId) {
        return materialService.getUnusedMaterials(parentMaterialId);
    }
}