package com.project.workorder.service;

import com.project.workorder.dto.PickingRequest;
import com.project.workorder.dto.ProducedQuantityRequest;
import com.project.workorder.dto.ProductionOrderRequest;
import com.project.workorder.dto.WorkOrderCreateRequest;
import com.project.workorder.dto.WorkOrderDto;
import com.project.workorder.dto.WorkOrderMaterialDto;
import com.project.workorder.service.WorkOrderService;
import com.project.depot.service.MaterialService;
import com.project.machine.Bean.MachinesBean;
import com.project.bom.service.BomComponentService;
import com.project.core.dao.EmployeeUserRepository;

import com.project.depot.dao.InventoryTransactionRepository;
import com.project.depot.dao.MaterialRepository;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.model.Material;
import com.project.workorder.dao.WorkOrderMaterialRepository;
import com.project.workorder.dao.WorkOrderRepository;
import com.project.bom.model.BomComponent;
import com.project.workorder.model.WorkOrder;
import com.project.workorder.model.WorkOrderMaterial;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

/**
 * 工單服務實作類別
 * 實作工單相關的業務邏輯操作。
 */
@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMaterialRepository workOrderMaterialRepository;
    private final MaterialRepository materialRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final EmployeeUserRepository employeeUserRepository;
    private final MaterialService materialService; // 注入 MaterialService
    private final BomComponentService bomComponentService; // 注入 BomComponentService

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 建構子注入依賴。
     * 
     * @param workOrderRepository            工單資料庫操作介面
     * @param workOrderMaterialRepository    工單領料明細資料庫操作介面
     * @param materialRepository             物料資料庫操作介面
     * @param inventoryTransactionRepository 庫存交易紀錄資料庫操作介面
     * @param employeeUserRepository         員工使用者資料庫操作介面
     * @param materialService                物料服務
     * @param bomComponentService            BOM 組件服務
     */
    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository,
            WorkOrderMaterialRepository workOrderMaterialRepository,
            MaterialRepository materialRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            EmployeeUserRepository employeeUserRepository,
            MaterialService materialService,
            BomComponentService bomComponentService) { // Add BomComponentService to constructor
        this.workOrderRepository = workOrderRepository;
        this.workOrderMaterialRepository = workOrderMaterialRepository;
        this.materialRepository = materialRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.employeeUserRepository = employeeUserRepository;
        this.materialService = materialService;
        this.bomComponentService = bomComponentService;
    }

    @Override
    public Page<WorkOrderDto> findAllWorkOrders(String woNumber, String materialName, String status, Pageable pageable) {
        Specification<WorkOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (woNumber != null && !woNumber.isEmpty()) {
                predicates.add(cb.like(root.get("woNumber"), "%" + woNumber + "%"));
            }
            if (materialName != null && !materialName.isEmpty()) {
                predicates.add(cb.like(root.join("material").get("materialName"), "%" + materialName + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return workOrderRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    /**
     * 根據ID查詢單一工單。
     * 
     * @param id 工單ID
     * @return Optional<WorkOrderDto> 如果找到則返回工單，否則為空
     */
    @Override
    public Optional<WorkOrderDto> findWorkOrderById(Long id) {
        return workOrderRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 新增工單。
     * 
     * @param request 包含工單資訊的請求DTO
     * @return 儲存後的工單DTO
     */
    @Override
    @Transactional
    public WorkOrderDto createWorkOrder(WorkOrderCreateRequest request) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoNumber(request.getWoNumber());

        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Material not found with ID: " + request.getMaterialId()));
        workOrder.setMaterial(material);
        workOrder.setRequiredQuantity(request.getRequiredQuantity());
        workOrder.setProducedQuantity(BigDecimal.ZERO); // Initialize produced quantity to 0
        workOrder.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        // Assuming requestedBy and issuedBy are set elsewhere or are optional
        // For now, setting them to null or a default user if needed
        workOrder.setRequestedBy(null); // Or fetch a default user
        workOrder.setIssuedBy(null); // Or fetch a default user

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        return convertToDto(savedWorkOrder);
    }

    /**
     * 建立生產工單並扣除物料庫存。
     * 
     * @param request 生產工單請求DTO
     * @return 儲存後的工單DTO
     */
    @Override
    @Transactional
    public WorkOrderDto createProductionOrder(ProductionOrderRequest request) {
        // 1. 產生一個實體物件之後導入
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoNumber(request.getWoNumber());

        Material finishedProduct = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Finished product material not found with ID: " + request.getMaterialId()));
        workOrder.setMaterial(finishedProduct);
        workOrder.setRequiredQuantity(new BigDecimal(request.getRequiredQuantity().toString()));
        workOrder.setStatus(request.getStatus() != null ? request.getStatus() : "COMPLETED");
        // completes immediately
        workOrder.setRequestedBy(null); // Or fetch a default user
        workOrder.setIssuedBy(null); // Or fetch a default user

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);

        // 2. 減少物料的判斷與轉換型態
        for (com.project.workorder.dto.MaterialDeductionDto deduction : request.getMaterialsToDeduct()) {
            materialService.deductMaterialStock(deduction.getMaterialId(), deduction.getQuantity());

            // Record inventory transaction for raw material outbound
            InventoryTransaction transaction = new InventoryTransaction();
            Material rawMaterial = materialRepository.findById(deduction.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Raw material not found with ID: " + deduction.getMaterialId()));
            transaction.setMaterial(rawMaterial);
            // [修正] 使用資料庫允許的、代表「發料/消耗」的 物料。
            transaction.setTransactionType("PRODUCTION_OUTBOUND"); // 給logger(歷程記錄做的)
            transaction.setQuantity(deduction.getQuantity());
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setReferenceTable("work_orders");
            transaction.setReferenceId(savedWorkOrder.getWoId());
            inventoryTransactionRepository.save(transaction);
        }

        // 3. Increase finished product stock and record transaction
        finishedProduct.setStockCurrent(finishedProduct.getStockCurrent().add(request.getRequiredQuantity()));
        materialRepository.save(finishedProduct);

        InventoryTransaction finishedProductTransaction = new InventoryTransaction();
        finishedProductTransaction.setMaterial(finishedProduct);
        // [修正] 使用資料庫允許的、代表「收料/入庫」的 Enum。
        finishedProductTransaction.setTransactionType("PRODUCTION_INBOUND");
        finishedProductTransaction.setQuantity(request.getRequiredQuantity());
        finishedProductTransaction.setTransactionDate(LocalDateTime.now());
        finishedProductTransaction.setReferenceTable("work_orders");
        finishedProductTransaction.setReferenceId(savedWorkOrder.getWoId());
        inventoryTransactionRepository.save(finishedProductTransaction);

        return convertToDto(savedWorkOrder);
    }

    /**
     * 更新工單。
     * 
     * @param id      工單ID
     * @param request 包含更新資訊的請求DTO
     * @return 更新後的工單DTO
     */
    @Override
    @Transactional
    public WorkOrderDto updateWorkOrder(Long id, WorkOrderCreateRequest request) {
        WorkOrder existingWorkOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + id));

        existingWorkOrder.setWoNumber(request.getWoNumber());
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Material not found with ID: " + request.getMaterialId()));
        existingWorkOrder.setMaterial(material);
        existingWorkOrder.setRequiredQuantity(request.getRequiredQuantity());
        existingWorkOrder.setStatus(request.getStatus() != null ? request.getStatus() : existingWorkOrder.getStatus());

        WorkOrder updatedWorkOrder = workOrderRepository.save(existingWorkOrder);
        return convertToDto(updatedWorkOrder);
    }

    /**
     * 根據ID刪除工單。
     * 
     * @param id 工單ID
     */
    @Override
    @Transactional
    public void deleteWorkOrderById(Long id) {
        if (!workOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("WorkOrder not found with ID: " + id);
        }
        workOrderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public WorkOrderDto updateWorkOrderStatus(Long id, String status) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with id: " + id));

        // Set the status first
        workOrder.setStatus(status);

        // If the new status is COMPLETED, trigger inventory transactions
        if ("COMPLETED".equals(status)) {
            BigDecimal successfulQuantity = Optional.ofNullable(workOrder.getSuccessfulQuantity())
                    .orElse(BigDecimal.ZERO);

            // Proceed only if there's something produced
            if (successfulQuantity.compareTo(BigDecimal.ZERO) > 0) {

                // 1. Increase stock of the finished product
                Material finishedProduct = workOrder.getMaterial();
                BigDecimal currentStock = Optional.ofNullable(finishedProduct.getStockCurrent())
                        .orElse(BigDecimal.ZERO);
                finishedProduct.setStockCurrent(currentStock.add(successfulQuantity));
                materialRepository.save(finishedProduct);

                // 2. Log the inbound transaction for the finished product
                InventoryTransaction inboundLog = new InventoryTransaction();
                inboundLog.setMaterial(finishedProduct);
                inboundLog.setTransactionType("PRODUCTION_INBOUND");
                inboundLog.setQuantity(successfulQuantity);
                inboundLog.setTransactionDate(LocalDateTime.now());
                inboundLog.setReferenceTable("work_orders");
                inboundLog.setReferenceId(workOrder.getWoId());
                inventoryTransactionRepository.save(inboundLog);

                // 3. Get the BOM for the product to deduct raw materials
                List<BomComponent> bomComponents = bomComponentService
                        .getBomComponentsByParentMaterialId(finishedProduct.getMaterialId());

                // 4. Deduct stock for each raw material based on the BOM
                for (BomComponent component : bomComponents) {
                    Material rawMaterial = materialRepository.findById(component.getComponentMaterialId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Raw material not found with ID: " + component.getComponentMaterialId()));

                    BigDecimal totalDeduction = component.getQuantity().multiply(successfulQuantity);
                    BigDecimal currentRawStock = Optional.ofNullable(rawMaterial.getStockCurrent())
                            .orElse(BigDecimal.ZERO);

                    // Check for sufficient stock before deducting
                    if (currentRawStock.compareTo(totalDeduction) < 0) {
                        // Handle insufficient stock error - throw an exception
                        throw new IllegalStateException(
                                "Insufficient stock for raw material: " + rawMaterial.getMaterialName());
                    }

                    rawMaterial.setStockCurrent(currentRawStock.subtract(totalDeduction));
                    materialRepository.save(rawMaterial);

                    // Log the outbound transaction for the raw material
                    InventoryTransaction outboundLog = new InventoryTransaction();
                    outboundLog.setMaterial(rawMaterial);
                    outboundLog.setTransactionType("PRODUCTION_OUTBOUND");
                    outboundLog.setQuantity(totalDeduction);
                    outboundLog.setTransactionDate(LocalDateTime.now());
                    outboundLog.setReferenceTable("work_orders");
                    outboundLog.setReferenceId(workOrder.getWoId());
                    inventoryTransactionRepository.save(outboundLog);
                }
            }
        }

        WorkOrder updatedWorkOrder = workOrderRepository.save(workOrder);
        return convertToDto(updatedWorkOrder);
    }

    @Override
    @Transactional
    public WorkOrderDto updateWorkOrderProducedQuantity(Long woId, BigDecimal quantityProduced) {
        if (quantityProduced == null) {
            quantityProduced = BigDecimal.ZERO;
        }
        WorkOrder workOrder = workOrderRepository.findById(woId)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with id: " + woId));

        BigDecimal currentProduced = Optional.ofNullable(workOrder.getProducedQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal newProducedQuantity = currentProduced.add(quantityProduced);

        workOrder.setProducedQuantity(newProducedQuantity);
        workOrder.setSuccessfulQuantity(newProducedQuantity);
        WorkOrder updatedWorkOrder = workOrderRepository.save(workOrder);
        return convertToDto(updatedWorkOrder);
    }

    /**
     * 處理物料領料 (出庫) 操作。
     * 
     * @param request 領料請求DTO
     * @return 處理後的領料明細DTO
     */
    @Override
    @Transactional
    public WorkOrderMaterialDto processMaterialPicking(PickingRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWoId())
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + request.getWoId()));

        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Material not found with ID: " + request.getMaterialId()));

        // Check if enough material is available in stock
        if (material.getStockCurrent().compareTo(request.getRequestedQuantity()) < 0) {
            throw new IllegalArgumentException(
                    "Not enough material in stock for material ID: " + material.getMaterialId());
        }

        // Update material stock
        material.setStockCurrent(material.getStockCurrent().subtract(request.getRequestedQuantity()));
        materialRepository.save(material);

        // Create inventory transaction for outbound
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setMaterial(material);
        // [修正] 「領料(Picking)」是發料出庫的行為，應該使用 PRODUCTION_OUTBOUND。原先的 PRODUCTION_INBOUND
        // 是不合邏輯的。
        transaction.setTransactionType("PRODUCTION_OUTBOUND");
        transaction.setQuantity(request.getRequestedQuantity());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setReferenceId(workOrder.getWoId()); // Reference to work order
        inventoryTransactionRepository.save(transaction);

        // Update or create WorkOrderMaterial
        WorkOrderMaterial workOrderMaterial = workOrderMaterialRepository
                .findByWorkOrder_WoId(workOrder.getWoId())
                .stream()
                .filter(wm -> wm.getMaterial().getMaterialId().equals(material.getMaterialId()))
                .findFirst()
                .orElse(new WorkOrderMaterial());

        workOrderMaterial.setWorkOrder(workOrder);
        workOrderMaterial.setMaterial(material);
        workOrderMaterial.setRequestedQuantity(request.getRequestedQuantity());
        workOrderMaterial.setIssuedQuantity(
                Optional.ofNullable(workOrderMaterial.getIssuedQuantity()).orElse(BigDecimal.ZERO)
                        .add(request.getRequestedQuantity()));
        workOrderMaterial.setStatus("ISSUED"); // Or partially issued

        WorkOrderMaterial savedWorkOrderMaterial = workOrderMaterialRepository.save(workOrderMaterial);
        // Reload the entity to ensure ID is populated
        savedWorkOrderMaterial = workOrderMaterialRepository.findById(savedWorkOrderMaterial.getWoMaterialId())
                .orElseThrow(() -> new EntityNotFoundException("WorkOrderMaterial not found after save."));
        return convertToDto(savedWorkOrderMaterial);
    }

    /**
     * 根據工單ID查詢所有領料明細。
     * 
     * @param woId 工單ID
     * @return 領料明細列表
     */
    @Override
    public List<WorkOrderMaterialDto> findWorkOrderMaterialsByWorkOrderId(Long woId) {
        return workOrderMaterialRepository.findByWorkOrder_WoId(woId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 查詢所有領料明細。
     * 
     * @return 領料明細列表
     */
    @Override
    public List<WorkOrderMaterialDto> findAllWorkOrderMaterials() {
        return workOrderMaterialRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據ID查詢單一領料明細。
     * 
     * @param id 領料明細ID
     * @return Optional<WorkOrderMaterialDto> 如果找到則返回領料明細，否則為空
     */
    @Override
    public Optional<WorkOrderMaterialDto> findWorkOrderMaterialById(Long id) {
        return workOrderMaterialRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 根據ID刪除領料明細。
     * 
     * @param id 領料明細ID
     */
    @Override
    @Transactional
    public void deleteWorkOrderMaterialById(Long id) {
        if (!workOrderMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("WorkOrderMaterial not found with ID: " + id);
        }
        workOrderMaterialRepository.deleteById(id);
    }

    /**
     * 將 WorkOrder 實體轉換為 WorkOrderDto DTO。
     * 
     * @param workOrder WorkOrder 實體
     * @return WorkOrderDto DTO
     */
    private WorkOrderDto convertToDto(WorkOrder workOrder) {
        WorkOrderDto dto = new WorkOrderDto();
        dto.setWoId(workOrder.getWoId().intValue());
        dto.setWoNumber(workOrder.getWoNumber());
        if (workOrder.getMaterial() != null) {
            dto.setMaterialId(workOrder.getMaterial().getMaterialId());
            dto.setMaterialName(workOrder.getMaterial().getMaterialName());
        } else {
            dto.setMaterialId(null); // Or some default value
            dto.setMaterialName("N/A"); // Or some default value
        }
        dto.setRequiredQuantity(workOrder.getRequiredQuantity());
        // Set both quantity fields in the DTO to ensure consistency, regardless of which one the frontend uses.
        dto.setProducedQuantity(workOrder.getProducedQuantity());
        dto.setSuccessfulQuantity(workOrder.getSuccessfulQuantity());
        dto.setStatus(workOrder.getStatus());
        dto.setCreatedAt(workOrder.getCreatedAt());
        dto.setUpdatedAt(workOrder.getUpdatedAt());
        // Populate machines
        dto.setMachines(findMachinesByWorkOrderId(Long.valueOf(workOrder.getWoId())));
        return dto;
    }

    /**
     * 將 WorkOrderMaterial 實體轉換為 WorkOrderMaterialDto DTO。
     * 
     * @param workOrderMaterial WorkOrderMaterial 實體
     * @return WorkOrderMaterialDto DTO
     */
    private WorkOrderMaterialDto convertToDto(WorkOrderMaterial workOrderMaterial) {
        WorkOrderMaterialDto dto = new WorkOrderMaterialDto();
        dto.setWoMaterialId(workOrderMaterial.getWoMaterialId().intValue()); // Convert Long to Integer
        dto.setMaterialId(workOrderMaterial.getMaterial().getMaterialId()); // Assuming materialId is Long in DTO
        dto.setMaterialName(workOrderMaterial.getMaterial().getMaterialName());
        dto.setRequestedQuantity(workOrderMaterial.getRequestedQuantity());
        dto.setIssuedQuantity(workOrderMaterial.getIssuedQuantity());
        dto.setStatus(workOrderMaterial.getStatus());
        return dto;
    }

    @Override
    public void assignMachinesToWorkOrder(Long woId, List<Integer> machineIds) {
        for (Integer machineId : machineIds) {
            // Check if the association already exists to prevent duplicates
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM workorder_machines WHERE wo_id = ? AND machine_id = ?",
                    Integer.class, woId, machineId);
            if (count == 0) {
                jdbcTemplate.update("INSERT INTO workorder_machines (wo_id, machine_id) VALUES (?, ?)", woId, machineId);
            }
        }
    }

    @Override
    public List<MachinesBean> findMachinesByWorkOrderId(Long woId) {
        String sql = "SELECT m.* FROM machines m JOIN workorder_machines wm ON m.machine_id = wm.machine_id WHERE wm.wo_id = ?";
        return jdbcTemplate.query(sql, new org.springframework.jdbc.core.BeanPropertyRowMapper<>(MachinesBean.class),
                woId);
    }

    @Override
    public void removeMachineFromWorkOrder(Long woId, Long machineId) {
        int rowsAffected = jdbcTemplate.update(
                "DELETE FROM workorder_machines WHERE wo_id = ? AND machine_id = ?", woId, machineId);
        if (rowsAffected == 0) {
            throw new EntityNotFoundException("未找到對應的工單與機台關聯，工單ID: " + woId + "，機台ID: " + machineId);
        }
    }

    @Override
    public WorkOrderDto reportWorkOrder(Long id, ProducedQuantityRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reportWorkOrder'");
    }

    @Transactional
    public WorkOrderDto updateQuantities(Long woId, BigDecimal successfulQuantity, BigDecimal failedQuantity) {
        WorkOrder workOrder = workOrderRepository.findById(woId)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + woId));

        // Correctly accumulate quantities instead of overwriting them
        BigDecimal currentSuccessful = Optional.ofNullable(workOrder.getSuccessfulQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal newSuccessfulTotal = currentSuccessful.add(successfulQuantity);
        workOrder.setSuccessfulQuantity(newSuccessfulTotal);
        workOrder.setProducedQuantity(newSuccessfulTotal); // Also update the other field for consistency

        BigDecimal currentFailed = Optional.ofNullable(workOrder.getFailedQuantity()).orElse(BigDecimal.ZERO);
        workOrder.setFailedQuantity(currentFailed.add(failedQuantity));

        WorkOrder updatedWorkOrder = workOrderRepository.save(workOrder);
        return convertToDto(updatedWorkOrder);
    }

    @Override
    public BigDecimal findSuccessfulQuantityByWorkOrderId(Long woId) {
        WorkOrder workOrder = workOrderRepository.findById(woId)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + woId));
        return workOrder.getSuccessfulQuantity();
    }

    @Override
    public List<WorkOrderMaterialDto> getWorkOrderBOM(Long woId) {
        WorkOrder workOrder = workOrderRepository.findById(woId)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + woId));
        
        Material finishedProduct = workOrder.getMaterial();
        if (finishedProduct == null) {
            return List.of(); // Or throw an exception if a product is always expected
        }

        List<com.project.bom.model.BomComponent> bomComponents = bomComponentService.getBomComponentsByParentMaterialId(finishedProduct.getMaterialId());

        // Manually map BomComponent to WorkOrderMaterialDto
        return bomComponents.stream().map(component -> {
            WorkOrderMaterialDto dto = new WorkOrderMaterialDto();
            Material componentMaterial = materialRepository.findById(component.getComponentMaterialId())
                    .orElse(null);
            
            if (componentMaterial != null) {
                dto.setMaterialId(componentMaterial.getMaterialId());
                dto.setMaterialName(componentMaterial.getMaterialName());
            } else {
                dto.setMaterialId(component.getComponentMaterialId());
                dto.setMaterialName("Unknown Material");
            }
            
            dto.setIssuedQuantity(component.getQuantity()); // Using issuedQuantity to hold the BOM quantity
            dto.setRequestedQuantity(component.getQuantity()); // Also populating requestedQuantity for consistency
            dto.setStatus("BOM"); // Using status to indicate this is a BOM component, not an issued material
            return dto;
        }).collect(Collectors.toList());
    }

}