package com.project.workorder.controller;

import com.project.machine.Bean.MachinesBean;
import com.project.workorder.dto.PickingRequest;
import com.project.workorder.dto.ProductionOrderRequest;
import com.project.workorder.dto.ProducedQuantityRequest;
import com.project.workorder.dto.StatusUpdateRequest;
import com.project.workorder.dto.WorkOrderCreateRequest;
import com.project.workorder.dto.WorkOrderDto;
import com.project.workorder.dto.WorkOrderMaterialDto;
import com.project.workorder.service.WorkOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 工單模組 API 控制器
 * 提供工單和領料的 RESTful 介面，包含查詢、新增、更新、刪除等操作。
 */
@Tag(name = "工單模組", description = "管理生產工單、領料、BOM 等相關操作")
@RestController
@RequestMapping("/api/workorder")
@CrossOrigin(origins = { "http://localhost:5173", "http://172.22.34.82:5173" }) // 允許來自前端的跨域請求
public class WorkOrderController {

    private static final Logger logger = LoggerFactory.getLogger(WorkOrderController.class);

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 獲取所有工單列表。
     * 
     * @return ResponseEntity<List<WorkOrderDto>> 包含所有工單的列表
     */
    @Operation(summary = "獲取所有工單", description = "獲取所有生產工單的列表")
    @GetMapping
    public ResponseEntity<Page<WorkOrderDto>> getAllWorkOrders(
            @RequestParam(required = false) String woNumber,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        logger.info("請求獲取所有工單列表");
        Page<WorkOrderDto> workOrders = workOrderService.findAllWorkOrders(woNumber, materialName, status, pageable);
        return ResponseEntity.ok(workOrders);
    }

    /**
     * 根據ID獲取單一工單。
     * 
     * @param id 工單ID
     * @return ResponseEntity<WorkOrderDto> 如果找到則返回工單，否則返回404
     */
    @Operation(summary = "根據ID獲取工單", description = "根據工單ID查詢單一工單的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderDto> getWorkOrderById(
            @Parameter(description = "工單ID", required = true) @PathVariable Long id) {
        logger.info("請求獲取工單，ID: {}", id);
        return workOrderService.findWorkOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增一個工單。
     * 
     * @param request 工單建立請求DTO
     * @return ResponseEntity<WorkOrderDto> 儲存後的工單DTO和201狀態碼
     */
    @Operation(summary = "新增工單", description = "新增一個新的生產工單")
    @PostMapping
    public ResponseEntity<WorkOrderDto> createWorkOrder(
            @Parameter(description = "工單建立請求物件", required = true) @RequestBody WorkOrderCreateRequest request) {
        logger.info("請求新增工單: {}", request.getWoNumber());
        WorkOrderDto savedWorkOrder = workOrderService.createWorkOrder(request);
        return new ResponseEntity<>(savedWorkOrder, HttpStatus.CREATED);
    }

    /**
     * 建立生產工單並扣除物料庫存。
     * 
     * @param request 生產工單請求DTO
     * @return ResponseEntity<WorkOrderDto> 儲存後的工單DTO和201狀態碼
     */
    @Operation(summary = "建立生產工單並扣除物料庫存", description = "建立生產工單，並自動扣除所需物料的庫存")
    @PostMapping("/produce")
    public ResponseEntity<WorkOrderDto> createProductionOrder(
            @Parameter(description = "生產工單請求物件", required = true) @RequestBody ProductionOrderRequest request) {
        logger.info("請求建立生產工單: {}", request.getWoNumber());
        try {
            WorkOrderDto savedWorkOrder = workOrderService.createProductionOrder(request);
            return new ResponseEntity<>(savedWorkOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("建立生產工單失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) {
            logger.error("建立生產工單失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 更新一個現有的工單。
     * 
     * @param id      工單ID
     * @param request 包含更新資訊的工單建立請求DTO
     * @return ResponseEntity<WorkOrderDto> 更新後的工單DTO，如果找不到則返回404
     */
    @Operation(summary = "更新工單", description = "根據工單ID更新工單資訊")
    @PutMapping("/{id}")
    public ResponseEntity<WorkOrderDto> updateWorkOrder(
            @Parameter(description = "工單ID", required = true) @PathVariable Long id,
            @Parameter(description = "包含更新資料的工單建立請求物件", required = true) @RequestBody WorkOrderCreateRequest request) {
        logger.info("請求更新工單，ID: {}", id);
        try {
            WorkOrderDto updatedWorkOrder = workOrderService.updateWorkOrder(id, request);
            logger.info("工單 {} 已更新", updatedWorkOrder.getWoNumber());
            return ResponseEntity.ok(updatedWorkOrder);
        } catch (RuntimeException e) {
            logger.warn("更新工單失敗，ID: {}. 錯誤: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根據ID刪除工單。
     * 
     * @param id 工單ID
     * @return ResponseEntity<Void> 204 No Content 狀態碼
     */
    @Operation(summary = "刪除工單", description = "根據工單ID刪除指定的生產工單")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrder(
            @Parameter(description = "要刪除的工單ID", required = true) @PathVariable Long id) {
        logger.info("請求刪除工單，ID: {}", id);
        try {
            workOrderService.deleteWorkOrderById(id);
            logger.info("工單 {} 已刪除", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("嘗試刪除不存在的工單，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新工單狀態。
     * 
     * @param id     工單ID
     * @param status 新的工單狀態
     * @return ResponseEntity<WorkOrderDto> 更新後的工單DTO，如果找不到則返回404
     */
    @Operation(summary = "更新工單狀態", description = "根據工單ID更新工單的狀態")
    @PutMapping("/{id}/status")
    public ResponseEntity<WorkOrderDto> updateWorkOrderStatus(
            @Parameter(description = "工單ID", required = true) @PathVariable Long id,
            @Parameter(description = "新的工單狀態請求物件", required = true) @RequestBody StatusUpdateRequest request) {
        logger.info("請求更新工單狀態，ID: {}，新狀態: {}", id, request.getStatus());
        try {
            WorkOrderDto updatedWorkOrder = workOrderService.updateWorkOrderStatus(id, request.getStatus());
            logger.info("工單 {} 狀態已更新為 {}", id, request.getStatus());
            return ResponseEntity.ok(updatedWorkOrder);
        } catch (RuntimeException e) {
            logger.warn("更新工單狀態失敗，ID: {}. 錯誤: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "更新工單已生產數量", description = "根據工單ID增加工單的已生產數量")
    @PutMapping("/{id}/produce-quantity")
    public ResponseEntity<WorkOrderDto> updateWorkOrderProducedQuantity(
            @Parameter(description = "工單ID", required = true) @PathVariable Long id,
            @Parameter(description = "本次生產的數量", required = true) @RequestBody ProducedQuantityRequest request) {
        logger.info("請求更新工單已生產數量，ID: {}，本次生產數量: {}", id, request.getSuccessfulQuantity());
        try {
            WorkOrderDto updatedWorkOrder = workOrderService.updateWorkOrderProducedQuantity(id,
                    request.getSuccessfulQuantity());
            logger.info("工單 {} 已生產數量更新成功，目前已生產: {}", id, updatedWorkOrder.getProducedQuantity());
            return ResponseEntity.ok(updatedWorkOrder);
        } catch (RuntimeException e) {
            logger.warn("更新工單已生產數量失敗，ID: {}. 錯誤: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * 處理工單領料 (出庫) 操作。
     * 
     * @param request 領料請求DTO
     * @return ResponseEntity<WorkOrderMaterialDto> 處理後的領料明細DTO和200狀態碼
     */
    @Operation(summary = "處理工單領料", description = "處理生產工單的物料領料(出庫)操作")
    @PostMapping("/picking")
    public ResponseEntity<WorkOrderMaterialDto> processPicking(
            @Parameter(description = "領料請求物件", required = true) @RequestBody PickingRequest request) {
        logger.info("請求處理領料，工單ID: {}，物料ID: {}，數量: {}", request.getWoId(), request.getMaterialId(),
                request.getRequestedQuantity());
        try {
            WorkOrderMaterialDto processedWoMaterial = workOrderService.processMaterialPicking(request);
            return ResponseEntity.ok(processedWoMaterial);
        } catch (IllegalArgumentException e) {
            logger.error("領料失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return 400 with error message
        } catch (RuntimeException e) {
            logger.error("領料失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Generic error for other
                                                                                       // runtime exceptions
        }
    }

    /**
     * 根據工單ID獲取其所有領料明細。
     * 
     * @param woId 工單ID
     * @return ResponseEntity<List<WorkOrderMaterialDto>> 該工單的領料明細列表
     */
    @Operation(summary = "根據工單ID獲取領料明細", description = "根據工單ID查詢其所有相關的領料明細")
    @GetMapping("/{woId}/materials") // Changed to Long
    public ResponseEntity<List<WorkOrderMaterialDto>> getWorkOrderMaterialsByWorkOrderId(
            @Parameter(description = "工單ID", required = true) @PathVariable Long woId) {
        logger.info("請求獲取工單 {} 的領料明細", woId);
        List<WorkOrderMaterialDto> materials = workOrderService.findWorkOrderMaterialsByWorkOrderId(woId);
        return ResponseEntity.ok(materials);
    }

    /**
     * 獲取所有領料明細列表。
     * 
     * @return ResponseEntity<List<WorkOrderMaterialDto>> 所有領料明細的列表
     */
    @Operation(summary = "獲取所有領料明細", description = "獲取所有工單領料明細的列表")
    @GetMapping("/materials")
    public ResponseEntity<List<WorkOrderMaterialDto>> getAllWorkOrderMaterials() {
        logger.info("請求獲取所有領料明細列表");
        List<WorkOrderMaterialDto> materials = workOrderService.findAllWorkOrderMaterials();
        return ResponseEntity.ok(materials);
    }

    /**
     * 根據ID獲取單一領料明細。
     * 
     * @param id 領料明細ID
     * @return ResponseEntity<WorkOrderMaterialDto> 如果找到則返回領料明細，否則返回404
     */
    @Operation(summary = "根據ID獲取領料明細", description = "根據領料明細ID查詢單一領料明細的詳細資訊")
    @GetMapping("/materials/{id}") // Changed to Long
    public ResponseEntity<WorkOrderMaterialDto> getWorkOrderMaterialById(
            @Parameter(description = "領料明細ID", required = true) @PathVariable Long id) {
        logger.info("請求獲取領料明細，ID: {}", id);
        return workOrderService.findWorkOrderMaterialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根據ID刪除領料明細。
     * 
     * @param id 領料明細ID
     * @return ResponseEntity<Void> 204 No Content 狀態碼
     */
    @Operation(summary = "刪除領料明細", description = "根據領料明細ID刪除指定的領料明細記錄")
    @DeleteMapping("/materials/{id}") // Changed to Long
    public ResponseEntity<Void> deleteWorkOrderMaterial(
            @Parameter(description = "要刪除的領料明細ID", required = true) @PathVariable Long id) {
        logger.info("請求刪除領料明細，ID: {}", id);
        try {
            workOrderService.deleteWorkOrderMaterialById(id);
            logger.info("領料明細 {} 已刪除", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("嘗試刪除不存在的領料明細，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 指派機台給工單。
     * 
     * @param woId       工單ID
     * @param machineIds 要指派的機台ID列表
     * @return ResponseEntity<Void> 200 OK 狀態碼
     */
    @Operation(summary = "指派機台給工單", description = "將指定的機台指派給某個工單")
    @PostMapping("/{woId}/machines")
    public ResponseEntity<Void> assignMachinesToWorkOrder(@PathVariable Long woId,
            @RequestBody List<Integer> machineIds) {
        workOrderService.assignMachinesToWorkOrder(woId, machineIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{woId}/machines")
    public ResponseEntity<List<MachinesBean>> getMachinesByWorkOrderId(@PathVariable Long woId) {
        List<MachinesBean> machines = workOrderService.findMachinesByWorkOrderId(woId);
        return ResponseEntity.ok(machines);
    }

    @Operation(summary = "移除機台與工單的關聯", description = "根據工單ID和機台ID移除兩者的關聯")
    @DeleteMapping("/{woId}/machine/{machineId}")
    public ResponseEntity<Void> removeMachineFromWorkOrder(
            @Parameter(description = "工單ID", required = true) @PathVariable Long woId,
            @Parameter(description = "機台ID", required = true) @PathVariable Long machineId) {
        try {
            workOrderService.removeMachineFromWorkOrder(woId, machineId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("移除機台與工單關聯失敗，工單ID: {}, 機台ID: {}. 錯誤: {}", woId, machineId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 更新工單的成功數量和失敗數量。
     * 
     * @param woId    工單ID
     * @param request 包含成功數量和失敗數量的請求物件
     * @return 更新後的工單DTO
     */
    @PutMapping("/{woId}/quantities")
    public ResponseEntity<WorkOrderDto> updateQuantities(
            @PathVariable Long woId,
            @RequestBody ProducedQuantityRequest request) {
        logger.info("接收到的數據: 工單ID={}, 成功數量={}, 失敗數量={}",
                woId, request.getSuccessfulQuantity(), request.getFailedQuantity());
        WorkOrderDto updatedWorkOrder = workOrderService.updateQuantities(
                woId,
                request.getSuccessfulQuantity(),
                request.getFailedQuantity());
        return ResponseEntity.ok(updatedWorkOrder);
    }

    /**
     * 查詢工單的成功數量。
     * 
     * @param woId 工單ID
     * @return 成功數量
     */
    @GetMapping("/{woId}/successful-quantity")
    public ResponseEntity<BigDecimal> getSuccessfulQuantity(@PathVariable Long woId) {
        BigDecimal successfulQuantity = workOrderService.findSuccessfulQuantityByWorkOrderId(woId);
        return ResponseEntity.ok(successfulQuantity);
    }

    /**
     * 根據工單ID獲取其產品的物料清單 (BOM)。
     * 
     * @param id 工單ID
     * @return 該工單產品的BOM列表
     */
    @Operation(summary = "獲取工單的BOM", description = "根據工單ID查詢其對應產品的物料清單(BOM)")
    @GetMapping("/{id}/bom")
    public ResponseEntity<List<com.project.workorder.dto.WorkOrderMaterialDto>> getWorkOrderBOM(
            @Parameter(description = "工單ID", required = true) @PathVariable Long id) {
        logger.info("請求獲取工單 {} 的BOM", id);
        List<com.project.workorder.dto.WorkOrderMaterialDto> bom = workOrderService.getWorkOrderBOM(id);
        return ResponseEntity.ok(bom);
    }
}