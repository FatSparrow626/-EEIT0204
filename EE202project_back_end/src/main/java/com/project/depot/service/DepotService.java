package com.project.depot.service;

import com.project.depot.dto.request.InboundReceiptCreateRequest;
import com.project.depot.dto.response.InboundReceiptResponse;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.model.Material;
import com.project.depot.pickingorder.model.PickingOrder;
import java.util.List;
import java.util.Optional;

/**
 * 倉庫模組服務介面
 */
public interface DepotService {

    /**
     * 查詢所有物料 (庫存)
     * 
     * @return 物料列表
     */
    List<Material> findAllMaterials();

    /**
     * 根據ID查詢物料
     * 
     * @param id 物料ID
     * @return Optional<Material>
     */
    Optional<Material> findMaterialById(Integer id);

    /**
     * 新增物料
     * 
     * @param material 物料實體
     * @return 儲存後的物料實體
     */
    Material createMaterial(Material material);

    /**
     * 更新物料
     * 
     * @param id 物料ID
     * @param material 包含更新資訊的物料實體
     * @return 更新後的物料實體
     */
    Material updateMaterial(Integer id, Material material);

    /**
     * 根據ID刪除物料
     * 
     * @param id 物料ID
     */
    void deleteMaterialById(Integer id);

    /**
     * 建立一筆入庫單
     *
     * @param inboundReceiptCreateRequest 入庫單建立請求 DTO
     * @return 儲存後的入庫單回應 DTO
     */
    InboundReceiptResponse createInboundReceipt(InboundReceiptCreateRequest inboundReceiptCreateRequest);

    /**
     * 根據ID查詢入庫單
     *
     * @param id 入庫單ID
     * @return Optional<InboundReceiptResponse>
     */
    Optional<InboundReceiptResponse> findInboundReceiptById(Integer id);

    /**
     * 查詢所有入庫單
     *
     * @return 入庫單回應列表
     */
    List<InboundReceiptResponse> findAllInboundReceipts();

    /**
     * 查詢所有庫存交易紀錄
     * 
     * @return 庫存交易紀錄列表
     */
    List<InventoryTransaction> findAllInventoryTransactions();

    /**
     * 根據ID刪除入庫單
     * 
     * @param id 入庫單ID
     */
    void deleteInboundReceiptById(Integer id);

    /**
     * 查詢所有領料單
     *
     * @return 領料單列表
     */
    List<PickingOrder> findAllPickingOrders();

    /**
     * 調整物料庫存數量並記錄交易。
     *
     * @param materialId 物料ID
     * @param quantity 調整數量 (正數為增加，負數為減少)
     * @param transactionType 交易類型 (例如: PRODUCTION_CONSUMPTION, PRODUCTION_OUTPUT)
     * @param referenceTable 參考表格名稱 (例如: work_orders)
     * @param referenceId 參考ID (例如: 工單ID)
     * @return 更新後的物料實體
     */
    Material adjustMaterialStock(Long materialId, java.math.BigDecimal quantity, String transactionType, String referenceTable, Long referenceId);
}