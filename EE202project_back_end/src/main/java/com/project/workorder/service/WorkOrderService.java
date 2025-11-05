package com.project.workorder.service;

import com.project.machine.Bean.MachinesBean;
import com.project.workorder.dto.PickingRequest;
import com.project.workorder.dto.ProducedQuantityRequest;
import com.project.workorder.dto.ProductionOrderRequest;
import com.project.workorder.dto.WorkOrderCreateRequest;
import com.project.workorder.dto.WorkOrderDto;
import com.project.workorder.dto.WorkOrderMaterialDto;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 工單服務介面
 * 定義工單相關的業務邏輯操作。
 */
public interface WorkOrderService {

    /**
     * 查詢所有工單。
     * 
     * @return 工單列表
     */
    Page<WorkOrderDto> findAllWorkOrders(String woNumber, String materialName, String status, Pageable pageable);

    /**
     * 根據ID查詢單一工單。
     * 
     * @param id 工單ID
     * @return Optional<WorkOrderDto> 如果找到則返回工單，否則為空
     */
    Optional<WorkOrderDto> findWorkOrderById(Long id);

    /**
     * 新增工單。
     * 
     * @param request 包含工單資訊的請求DTO
     * @return 儲存後的工單DTO
     */
    WorkOrderDto createWorkOrder(WorkOrderCreateRequest request);

    /**
     * 更新工單。
     * 
     * @param id      工單ID
     * @param request 包含更新資訊的請求DTO
     * @return 更新後的工單DTO
     */
    WorkOrderDto updateWorkOrder(Long id, WorkOrderCreateRequest request);

    /**
     * 根據ID刪除工單。
     * 
     * @param id 工單ID
     */
    void deleteWorkOrderById(Long id);

    /**
     * 更新工單狀態。
     * 
     * @param id     工單ID
     * @param status 新的工單狀態
     * @return 更新後的工單DTO
     */
    WorkOrderDto updateWorkOrderStatus(Long id, String status);

    WorkOrderDto updateWorkOrderProducedQuantity(Long woId, BigDecimal quantityProduced);

    /**
     * 建立生產工單並扣除物料庫存。
     * 
     * @param request 生產工單請求DTO
     * @return 儲存後的工單DTO
     */
    WorkOrderDto createProductionOrder(ProductionOrderRequest request);

    /**
     * 處理物料領料 (出庫) 操作。
     * 
     * @param request 領料請求DTO
     * @return 處理後的領料明細DTO
     */
    WorkOrderMaterialDto processMaterialPicking(PickingRequest request);

    /**
     * 根據工單ID查詢所有領料明細。
     * 
     * @param woId 工單ID
     * @return 領料明細列表
     */
    List<WorkOrderMaterialDto> findWorkOrderMaterialsByWorkOrderId(Long woId);

    /**
     * 查詢所有領料明細。
     * 
     * @return 領料明細列表
     */
    List<WorkOrderMaterialDto> findAllWorkOrderMaterials();

    /**
     * 根據ID查詢單一領料明細。
     * 
     * @param id 領料明細ID
     * @return Optional<WorkOrderMaterialDto> 如果找到則返回領料明細，否則為空
     */
    Optional<WorkOrderMaterialDto> findWorkOrderMaterialById(Long id);

    /**
     * 根據ID刪除領料明細。
     * 
     * @param id 領料明細ID
     */
    void deleteWorkOrderMaterialById(Long id);

    /**
     * 指派機台給工單。
     * 
     * @param woId       工單ID
     * @param machineIds 機台ID列表
     */
    void assignMachinesToWorkOrder(Long woId, List<Integer> machineIds);

    void removeMachineFromWorkOrder(Long woId, Long machineId);

    List<MachinesBean> findMachinesByWorkOrderId(Long woId);

    WorkOrderDto reportWorkOrder(Long id, ProducedQuantityRequest request);

    /**
     * 更新工單的成功數量和失敗數量。
     * 
     * @param woId               工單ID
     * @param successfulQuantity 成功數量
     * @param failedQuantity     失敗數量
     * @return 更新後的工單DTO
     */
    WorkOrderDto updateQuantities(Long woId, BigDecimal successfulQuantity, BigDecimal failedQuantity);

    /**
     * 查詢工單的成功數量。
     * 
     * @param woId 工單ID
     * @return 成功數量
     */
    BigDecimal findSuccessfulQuantityByWorkOrderId(Long woId);

    /**
     * 根據工單ID獲取其產品的物料清單 (BOM)。
     * 
     * @param woId 工單ID
     * @return 物料清單組件列表
     */
    List<WorkOrderMaterialDto> getWorkOrderBOM(Long woId);

}