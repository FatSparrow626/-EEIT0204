package com.project.depot.service;

import com.project.depot.dao.InboundReceiptRepository;
import com.project.depot.dao.InventoryTransactionRepository;
import com.project.depot.dao.MaterialRepository;
import com.project.core.dao.EmployeeUserRepository; // 引入 EmployeeUserRepository
import com.project.employeeuser.model.EmployeeUser; // 引入 EmployeeUser 實體
import com.project.depot.dto.request.InboundReceiptCreateRequest; // 引入新的請求 DTO
import com.project.depot.dto.request.InboundReceiptItemCreateRequest; // 引入新的請求 DTO
import com.project.depot.dto.response.InboundReceiptResponse; // 引入新的回應 DTO
import com.project.depot.dto.response.InboundReceiptItemResponse; // 引入新的回應 DTO
import com.project.depot.model.InboundReceiptStatus;
import com.project.depot.model.InboundReceiptStatus;
import com.project.depot.model.InboundReceipt;
import com.project.depot.model.InboundReceiptItem;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.model.Material;
import com.project.depot.pickingorder.model.PickingOrder;
import com.project.depot.pickingorder.service.PickingOrderService;
import com.project.supplier.dao.OrderRepository;
import com.project.supplier.model.PurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // 引入 Collectors

import jakarta.persistence.EntityNotFoundException; // 引入 EntityNotFoundException
import java.util.Set; // 引入 Set

/**
 * 倉庫模組服務實作
 */
@Service
public class DepotServiceImpl implements DepotService {

    private static final Logger logger = LoggerFactory.getLogger(DepotServiceImpl.class);

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private InboundReceiptRepository inboundReceiptRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.project.supplier.dao.OrderItemRepository orderItemRepository;

    private final EmployeeUserRepository employeeUserRepository; // 注入 EmployeeUserRepository
    private final PickingOrderService pickingOrderService; // 注入 PickingOrderService

    public DepotServiceImpl(MaterialRepository materialRepository, InboundReceiptRepository inboundReceiptRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            EmployeeUserRepository employeeUserRepository, PickingOrderService pickingOrderService,
            OrderRepository orderRepository, com.project.supplier.dao.OrderItemRepository orderItemRepository) {
        this.materialRepository = materialRepository;
        this.inboundReceiptRepository = inboundReceiptRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.employeeUserRepository = employeeUserRepository;
        this.pickingOrderService = pickingOrderService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 查詢所有物料庫存。
     * 
     * @return 物料列表
     */
    @Override
    public List<Material> findAllMaterials() {
        logger.info("查詢所有物料庫存");
        return materialRepository.findAll();
    }

    /**
     * 根據ID查詢物料。
     * 
     * @param id 物料ID
     * @return Optional<Material>
     */
    @Override
    public Optional<Material> findMaterialById(Integer id) {
        logger.info("根據ID查詢物料: {}", id);
        return materialRepository.findById(id.longValue());
    }

    /**
     * 新增物料。
     * 
     * @param material 物料實體
     * @return 儲存後的物料實體
     */
    @Override
    @Transactional
    public Material createMaterial(Material material) {
        logger.info("儲存物料: {}", material.getMaterialName());
        // 新增物料時，如果 stockCurrent 為 null，則設定為 0
        if (material.getStockCurrent() == null) {
            material.setStockCurrent(BigDecimal.ZERO);
        }
        return materialRepository.save(material);
    }

    /**
     * 更新物料。
     * 
     * @param id       物料ID
     * @param material 包含更新資訊的物料實體
     * @return 更新後的物料實體
     */
    @Override
    @Transactional
    public Material updateMaterial(Integer id, Material material) {
        logger.info("更新物料，ID: {}", id);
        return materialRepository.findById(id.longValue())
                .map(existingMaterial -> {
                    existingMaterial.setMaterialName(material.getMaterialName());
                    existingMaterial.setUnit(material.getUnit());
                    existingMaterial.setMaterialDescription(material.getMaterialDescription());
                    existingMaterial.setLocation(material.getLocation());
                    // 庫存數量不直接從更新介面修改，而是透過入庫/出庫交易
                    // existingMaterial.setStockCurrent(material.getStockCurrent());
                    existingMaterial.setSafetyStock(material.getSafetyStock());
                    existingMaterial.setReorderLevel(material.getReorderLevel());
                    existingMaterial.setActive(material.isActive());
                    Material updatedMaterial = materialRepository.save(existingMaterial);
                    logger.info("物料 {} 已更新", updatedMaterial.getMaterialName());
                    return updatedMaterial;
                })
                .orElseThrow(() -> new RuntimeException("找不到物料ID: " + id));
    }

    /**
     * 根據ID刪除物料。
     * 
     * @param id 物料ID
     */
    @Override
    @Transactional
    public void deleteMaterialById(Integer id) {
        logger.info("根據ID刪除物料: {}", id);
        materialRepository.deleteById(id.longValue());
    }

    /**
     * 建立一筆入庫單。
     * 
     * @param inboundReceiptCreateRequest 入庫單建立請求 DTO
     * @return 儲存後的入庫單回應 DTO
     */
    @Override
    @Transactional
    public InboundReceiptResponse createInboundReceipt(InboundReceiptCreateRequest inboundReceiptCreateRequest) {
        logger.info("開始建立入庫單，狀態: {}", inboundReceiptCreateRequest.getStatus());
        logger.info("接收到的經手人: {}", inboundReceiptCreateRequest.getHandledBy());
        logger.info("接收到的備註: {}", inboundReceiptCreateRequest.getNote());

        InboundReceipt inboundReceipt = new InboundReceipt();
        inboundReceipt.setInboundDate(inboundReceiptCreateRequest.getInboundDate());
        try {
            inboundReceipt.setStatus(
                    InboundReceiptStatus.valueOf(inboundReceiptCreateRequest.getStatus().toUpperCase()).name());
        } catch (IllegalArgumentException e) {
            logger.warn("無效的入庫單狀態: {}，將設定為 DRAFT", inboundReceiptCreateRequest.getStatus());
            inboundReceipt.setStatus(InboundReceiptStatus.DRAFT.name());
        }
        inboundReceipt.setNote(inboundReceiptCreateRequest.getNote()); // 設定備註

        if (inboundReceiptCreateRequest.getPurchaseOrderId() != null) {
            PurchaseOrder purchaseOrder = orderRepository
                    .findById(inboundReceiptCreateRequest.getPurchaseOrderId().intValue())
                    .orElseThrow(() -> new RuntimeException(
                            "找不到採購訂單ID: " + inboundReceiptCreateRequest.getPurchaseOrderId()));
            inboundReceipt.setPurchaseOrder(purchaseOrder);
        }

        // 處理領料單關聯
        if (inboundReceiptCreateRequest.getPickingOrderId() != null) {
            pickingOrderService.getPickingOrderById(inboundReceiptCreateRequest.getPickingOrderId())
                    .ifPresent(inboundReceipt::setPickingOrder);
        }

        // 處理經手人
        String handledByUsername = inboundReceiptCreateRequest.getHandledBy();
        logger.info("嘗試尋找經手人，使用者名稱: {}", handledByUsername);
        if (handledByUsername != null && !handledByUsername.isEmpty()) {
            Optional<EmployeeUser> handlerUserOptional = employeeUserRepository.findByUsername(handledByUsername);
            if (handlerUserOptional.isPresent()) {
                EmployeeUser handlerUser = handlerUserOptional.get();
                inboundReceipt.setHandledBy(handlerUser);
                logger.info("找到經手人: {} (ID: {})", handlerUser.getUsername(), handlerUser.getEmployeeUserId());
            } else {
                inboundReceipt.setHandledBy(null); // 如果找不到使用者，則設為 null
                logger.warn("找不到用戶名為 {} 的經手人，將經手人設為 null", handledByUsername);
            }
        } else {
            inboundReceipt.setHandledBy(null); // 如果沒有提供經手人，則設為 null
            logger.info("未提供經手人");
        }

        Set<InboundReceiptItem> items = new HashSet<>();
        for (InboundReceiptItemCreateRequest itemDTO : inboundReceiptCreateRequest.getItems()) {
            Material material = materialRepository.findById(itemDTO.getMaterialId().longValue())
                    .orElseThrow(() -> new RuntimeException("找不到物料ID: " + itemDTO.getMaterialId()));

            InboundReceiptItem item = new InboundReceiptItem();
            item.setMaterial(material);
            if (itemDTO.getReceivedQuantity() == null) {
                throw new IllegalArgumentException("InboundReceiptItem receivedQuantity cannot be null.");
            }
            item.setReceivedQuantity(itemDTO.getReceivedQuantity());
            item.setInboundReceipt(inboundReceipt);
            items.add(item);
        }
        inboundReceipt.setItems(items);

        InboundReceipt savedReceipt = inboundReceiptRepository.save(inboundReceipt);
        logger.info("入庫單主檔已儲存，ID: {}", savedReceipt.getInboundId());

        for (InboundReceiptItem item : savedReceipt.getItems()) {
            Material material = item.getMaterial();
            BigDecimal currentStock = material.getStockCurrent();
            BigDecimal receivedQuantity = item.getReceivedQuantity();
            material.setStockCurrent(currentStock.add(receivedQuantity));
            materialRepository.save(material);
            logger.info("物料 {} 庫存已更新，增加: {}，現有: {}", material.getMaterialName(), receivedQuantity,
                    material.getStockCurrent());

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setMaterial(material);
            transaction.setTransactionType("PURCHASE_INBOUND");
            transaction.setQuantity(receivedQuantity);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setReferenceTable("inbound_receipts");
            transaction.setReferenceId(savedReceipt.getInboundId());
            inventoryTransactionRepository.save(transaction);
            logger.info("已為物料 {} 建立庫存交易紀錄", material.getMaterialName());

            // 更新領料單項目的已收貨數量
            if (savedReceipt.getPickingOrder() != null) {
                savedReceipt.getPickingOrder().getItems().stream()
                        .filter(pickingOrderItem -> pickingOrderItem.getMaterial().getMaterialId()
                                .equals(material.getMaterialId()))
                        .findFirst()
                        .ifPresent(pickingOrderItem -> {
                            pickingOrderItem
                                    .setReceivedQuantity(pickingOrderItem.getReceivedQuantity().add(receivedQuantity));
                            pickingOrderService.createPickingOrder(savedReceipt.getPickingOrder()); // 保存更新後的領料單
                        });
            }

            // 更新採購訂單項目的已收貨數量並保存
            if (savedReceipt.getPurchaseOrder() != null) {
                savedReceipt.getPurchaseOrder().getItemList().stream()
                        .filter(orderItem -> orderItem.getMaterial().getMaterialId().equals(material.getMaterialId()))
                        .findFirst()
                        .ifPresent(orderItem -> {
                            orderItem.setReceivedQuantity(orderItem.getReceivedQuantity().add(receivedQuantity));

                            // NEW LOGIC: Update deliveryStatus for PurchaseOrderItem
                            if (orderItem.getReceivedQuantity().compareTo(orderItem.getQuantity()) >= 0) {
                                orderItem.setDeliveryStatus("已到貨"); // Fully received
                            } else if (orderItem.getReceivedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                                orderItem.setDeliveryStatus("部分到貨"); // Partially received
                            }

                            // 關鍵：保存更新後的 PurchaseOrderItem
                            orderItemRepository.save(orderItem);
                        });
            }
        }

        // 在所有入庫明細處理完畢後，更新採購訂單狀態
        if (savedReceipt.getPurchaseOrder() != null) {
            PurchaseOrder purchaseOrder = orderRepository.findById(savedReceipt.getPurchaseOrder().getOrderId())
                    .orElseThrow(
                            () -> new RuntimeException("找不到採購訂單ID: " + savedReceipt.getPurchaseOrder().getOrderId()));

            BigDecimal totalOrderedQuantity = BigDecimal.ZERO;
            BigDecimal totalReceivedQuantity = BigDecimal.ZERO;

            for (com.project.supplier.model.PurchaseOrderItem orderItem : purchaseOrder.getItemList()) {
                totalOrderedQuantity = totalOrderedQuantity.add(orderItem.getQuantity());
                totalReceivedQuantity = totalReceivedQuantity.add(orderItem.getReceivedQuantity());
            }

            if (totalReceivedQuantity.compareTo(totalOrderedQuantity) >= 0) {
                purchaseOrder.setOrderStatus("COMPLETED");
            } else if (totalReceivedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                purchaseOrder.setOrderStatus("PARTIALLY_RECEIVED");
            }
            orderRepository.save(purchaseOrder);
        }

        return convertToInboundReceiptResponse(savedReceipt);
    }

    /**
     * 根據ID查詢入庫單。
     * 
     * @param id 入庫單ID
     * @return Optional<InboundReceiptResponse>
     */
    @Override
    public Optional<InboundReceiptResponse> findInboundReceiptById(Integer id) {
        logger.info("根據ID查詢入庫單: {}", id);
        return inboundReceiptRepository.findById(id)
                .map(this::convertToInboundReceiptResponse);
    }

    /**
     * 查詢所有入庫單。
     * 
     * @return 入庫單回應列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<InboundReceiptResponse> findAllInboundReceipts() {
        logger.info("查詢所有入庫單");
        return inboundReceiptRepository.findAllWithDetails().stream()
                .map(this::convertToInboundReceiptResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查詢所有庫存交易紀錄。
     * 
     * @return 庫存交易紀錄列表
     */
    @Override
    public List<InventoryTransaction> findAllInventoryTransactions() {
        logger.info("查詢所有庫存交易紀錄");
        return inventoryTransactionRepository.findAllWithMaterial();
    }

    /**
     * 根據ID刪除入庫單。
     * 
     * @param id 入庫單ID
     */
    @Override
    @Transactional
    public void deleteInboundReceiptById(Integer id) {
        logger.info("根據ID刪除入庫單: {}", id);
        inboundReceiptRepository.deleteById(id);
    }

    /**
     * 查詢所有領料單。
     * 
     * @return 領料單列表
     */
    @Override
    public List<PickingOrder> findAllPickingOrders() {
        logger.info("查詢所有領料單");
        return pickingOrderService.getAllPickingOrders(null);
    }

    /**
     * 將 InboundReceipt 實體轉換為 InboundReceiptResponse DTO。
     * 
     * @param inboundReceipt InboundReceipt 實體
     * @return InboundReceiptResponse DTO
     */
    private InboundReceiptResponse convertToInboundReceiptResponse(InboundReceipt inboundReceipt) {
        if (inboundReceipt == null) {
            return null;
        }
        InboundReceiptResponse response = new InboundReceiptResponse();

        // 基本屬性
        response.setInboundId(inboundReceipt.getInboundId() != null ? inboundReceipt.getInboundId().intValue() : null);
        response.setInboundDate(inboundReceipt.getInboundDate());
        response.setStatus(inboundReceipt.getStatus());
        response.setNote(inboundReceipt.getNote());

        // 關聯屬性 (PurchaseOrder)
        if (inboundReceipt.getPurchaseOrder() != null) {
            response.setPurchaseOrderId((long) inboundReceipt.getPurchaseOrder().getOrderId());
        }

        // 關聯屬性 (HandledBy - EmployeeUser)
        if (inboundReceipt.getHandledBy() != null) {
            response.setHandledBy(new com.project.depot.dto.response.UserDto(
                    inboundReceipt.getHandledBy().getEmployeeUserId(),
                    inboundReceipt.getHandledBy().getUsername()));
        }

        // 關聯屬性 (Items)
        if (inboundReceipt.getItems() != null) {
            response.setItems(inboundReceipt.getItems().stream()
                    .map(this::convertToInboundReceiptItemResponse)
                    .filter(java.util.Objects::nonNull) // 過濾掉轉換後為 null 的項目
                    .collect(Collectors.toList()));
        } else {
            response.setItems(new java.util.ArrayList<>()); // 如果 items 為 null，則設置為空列表
        }

        return response;
    }

    /**
     * 將 InboundReceiptItem 實體轉換為 InboundReceiptItemResponse DTO。
     * 
     * @param item InboundReceiptItem 實體
     * @return InboundReceiptItemResponse DTO
     */
    private InboundReceiptItemResponse convertToInboundReceiptItemResponse(InboundReceiptItem item) {
        InboundReceiptItemResponse itemResponse = new InboundReceiptItemResponse();
        itemResponse.setInboundItemId(item.getInboundItemId().intValue());
        itemResponse.setReceivedQuantity(item.getReceivedQuantity());
        if (item.getMaterial() != null) {
            itemResponse.setMaterialId(item.getMaterial().getMaterialId().intValue());
            itemResponse.setMaterialName(item.getMaterial().getMaterialName());
        } else {
            itemResponse.setMaterialId(null);
            itemResponse.setMaterialName("未知物料");
        }
        return itemResponse;
    }

    /**
     * 調整物料庫存數量並記錄交易。
     *
     * @param materialId      物料ID
     * @param quantity        調整數量 (正數為增加，負數為減少)
     * @param transactionType 交易類型 (例如: PRODUCTION_CONSUMPTION, PRODUCTION_OUTPUT)
     * @param referenceTable  參考表格名稱 (例如: work_orders)
     * @param referenceId     參考ID (例如: 工單ID)
     * @return 更新後的物料實體
     */
    @Override
    @Transactional
    public Material adjustMaterialStock(Long materialId, BigDecimal quantity, String transactionType,
            String referenceTable, Long referenceId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with ID: " + materialId));

        material.setStockCurrent(material.getStockCurrent().add(quantity));
        Material updatedMaterial = materialRepository.save(material);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setMaterial(updatedMaterial);
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(quantity);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setReferenceTable(referenceTable);
        transaction.setReferenceId(referenceId);
        inventoryTransactionRepository.save(transaction);

        return updatedMaterial;
    }
}