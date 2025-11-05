package com.project.depot.controller;

import com.project.depot.dto.request.InboundReceiptCreateRequest;
import com.project.depot.dto.response.InboundReceiptResponse;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.dto.MaterialDto;
import com.project.depot.pickingorder.model.PickingOrder;
import com.project.depot.service.DepotService;
import com.project.depot.service.MaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 倉庫模組 API 控制器
 * 提供物料庫存、入庫單、領料單和庫存交易紀錄的 RESTful 介面。
 */
@Tag(name = "倉庫模組", description = "管理物料庫存、執行出入庫等相關操作")
@RestController
@RequestMapping("/api/depot")
@CrossOrigin(origins = {"http://localhost:5173", "http://172.22.34.82:5173"})
public class DepotController {

    private static final Logger logger = LoggerFactory.getLogger(DepotController.class);

    @Autowired
    private DepotService depotService;
    @Autowired
    private MaterialService materialService;

    /**
     * 獲取所有物料 (庫存) 列表。
     * 可選地根據物料類型進行過濾。
     * @param materialType 物料類型 (例如: PRODUCT, RAW_MATERIAL)。
     * @return 包含所有物料的列表。
     */
    @Operation(summary = "獲取所有物料列表", description = "獲取所有物料庫存列表，可選地根據物料類型進行過濾")
    @GetMapping("/materials")
    public ResponseEntity<List<MaterialDto>> getAllMaterials(@Parameter(description = "物料類型 (例如: PRODUCT, RAW_MATERIAL)", required = false) @RequestParam(required = false) String materialType) {
        logger.info("請求獲取所有物料列表，類型: {}", materialType);
        List<MaterialDto> materials;
        if (materialType != null && !materialType.isEmpty()) {
            materials = materialService.findAllMaterialsByType(materialType);
        } else {
            materials = materialService.findAllMaterials();
        }
        return ResponseEntity.ok(materials);
    }

    /**
     * 根據 ID 獲取單一物料。
     * @param id 物料ID。
     * @return 如果找到則返回物料，否則返回404。
     */
    @Operation(summary = "根據ID獲取物料", description = "根據物料ID查詢單一物料的詳細資訊")
    @GetMapping("/materials/{id}")
    public ResponseEntity<MaterialDto> getMaterialById(@Parameter(description = "物料ID", required = true) @PathVariable Long id) {
        logger.info("請求獲取物料，ID: {}", id);
        return materialService.findMaterialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增一個物料。
     * @param materialDto 包含物料資訊的DTO。
     * @return 儲存後的物料DTO和201狀態碼。
     */
    @Operation(summary = "新增物料", description = "新增一個新的物料記錄")
    @PostMapping("/materials")
    public ResponseEntity<MaterialDto> createMaterial(@Parameter(description = "物料資訊物件", required = true) @RequestBody MaterialDto materialDto) {
        logger.info("請求新增物料: {}", materialDto.getMaterialName());
        MaterialDto savedMaterial = materialService.createMaterial(materialDto);
        return new ResponseEntity<>(savedMaterial, HttpStatus.CREATED);
    }

    /**
     * 更新一個現有的物料。
     * @param id 物料ID。
     * @param materialDto 包含更新資訊的物料DTO。
     * @return 更新後的物料DTO，如果找不到則返回404。
     */
    @Operation(summary = "更新物料", description = "根據物料ID更新物料資訊")
    @PutMapping("/materials/{id}")
    public ResponseEntity<MaterialDto> updateMaterial(@Parameter(description = "物料ID", required = true) @PathVariable Long id, @Parameter(description = "包含更新資料的物料物件", required = true) @RequestBody MaterialDto materialDto) {
        logger.info("請求更新物料，ID: {}", id);
        try {
            MaterialDto updatedMaterial = materialService.updateMaterial(id, materialDto);
            logger.info("物料 {} 已更新", updatedMaterial.getMaterialName());
            return ResponseEntity.ok(updatedMaterial);
        } catch (RuntimeException e) {
            logger.warn("更新物料失敗，ID: {}. 錯誤: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根據ID刪除物料。
     * @param id 物料ID。
     * @return 204 No Content 狀態碼。
     */
    @Operation(summary = "刪除物料", description = "根據物料ID刪除指定的物料記錄")
    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> deleteMaterial(@Parameter(description = "要刪除的物料ID", required = true) @PathVariable Long id) {
        logger.info("請求刪除物料，ID: {}", id);
        try {
            materialService.deleteMaterialById(id);
            logger.info("物料 {} 已刪除", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("嘗試刪除不存在的物料，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 建立一筆入庫單。
     * @param inboundReceiptCreateRequest 入庫單建立請求DTO。
     * @return 儲存後的入庫單回應DTO。
     */
    @Operation(summary = "建立入庫單", description = "建立一筆新的物料入庫單")
    @PostMapping("/inbound-receipts")
    public ResponseEntity<InboundReceiptResponse> createInboundReceipt(@Parameter(description = "入庫單建立請求物件", required = true) @RequestBody InboundReceiptCreateRequest inboundReceiptCreateRequest) {
        logger.info("請求建立入庫單，狀態: {}", inboundReceiptCreateRequest.getStatus());
        try {
            InboundReceiptResponse createdReceipt = depotService.createInboundReceipt(inboundReceiptCreateRequest);
            return new ResponseEntity<>(createdReceipt, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("建立入庫單失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 獲取所有入庫單列表。
     * @return 包含所有入庫單的列表。
     */
    @Operation(summary = "獲取所有入庫單列表", description = "獲取所有物料入庫單的列表")
    @GetMapping("/inbound-receipts")
    public ResponseEntity<List<InboundReceiptResponse>> getAllInboundReceipts() {
        logger.info("請求獲取所有入庫單列表");
        List<InboundReceiptResponse> receipts = depotService.findAllInboundReceipts();
        return ResponseEntity.ok(receipts);
    }

    /**
     * 獲取所有領料單列表。
     * @return 包含所有領料單的列表。
     */
    @Operation(summary = "獲取所有領料單列表", description = "獲取所有物料領料單的列表")
    @GetMapping("/picking-orders")
    public ResponseEntity<List<PickingOrder>> getAllPickingOrders() {
        logger.info("請求獲取所有領料單列表");
        List<PickingOrder> pickingOrders = depotService.findAllPickingOrders();
        return ResponseEntity.ok(pickingOrders);
    }

    /**
     * 根據 ID 獲取單一入庫單。
     * @param id 入庫單ID。
     * @return 如果找到則返回入庫單，否則返回404。
     */
    @Operation(summary = "根據ID獲取入庫單", description = "根據入庫單ID查詢單一入庫單的詳細資訊")
    @GetMapping("/inbound-receipts/{id}")
    public ResponseEntity<InboundReceiptResponse> getInboundReceiptById(@Parameter(description = "入庫單ID", required = true) @PathVariable Integer id) {
        logger.info("請求獲取入庫單，ID: {}", id);
        return depotService.findInboundReceiptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 獲取所有庫存交易紀錄列表。
     * @return 包含所有庫存交易紀錄的列表。
     */
    @Operation(summary = "獲取所有庫存交易紀錄", description = "獲取所有物料庫存交易紀錄的列表")
    @GetMapping("/transactions")
    public ResponseEntity<List<InventoryTransaction>> getAllInventoryTransactions() {
        logger.info("請求獲取所有庫存交易紀錄列表");
        List<InventoryTransaction> transactions = depotService.findAllInventoryTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * 根據ID刪除入庫單。
     * @param id 入庫單ID。
     * @return 204 No Content 狀態碼。
     */
    @Operation(summary = "刪除入庫單", description = "根據入庫單ID刪除指定的入庫單記錄")
    @DeleteMapping("/inbound-receipts/{id}")
    public ResponseEntity<Void> deleteInboundReceipt(@Parameter(description = "要刪除的入庫單ID", required = true) @PathVariable Integer id) {
        logger.info("請求刪除入庫單，ID: {}", id);
        try {
            depotService.deleteInboundReceiptById(id);
            logger.info("入庫單 {} 已刪除", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("嘗試刪除不存在的入庫單，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}