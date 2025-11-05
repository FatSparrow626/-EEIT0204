package com.workorder.service;

import com.project.core.dao.EmployeeUserRepository;
import com.project.depot.dao.InventoryTransactionRepository;
import com.project.depot.dao.MaterialRepository;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.model.Material;
import com.project.workorder.dao.WorkOrderMaterialRepository;
import com.project.workorder.dao.WorkOrderRepository;
import com.project.workorder.model.WorkOrder;
import com.project.workorder.model.WorkOrderMaterial;
import com.project.workorder.service.WorkOrderServiceImpl;
import com.project.workorder.dto.PickingRequest;
import com.project.workorder.dto.WorkOrderDto;
import com.project.workorder.dto.WorkOrderCreateRequest;
import com.project.workorder.dto.WorkOrderMaterialDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderMaterialRepository workOrderMaterialRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private EmployeeUserRepository employeeUserRepository;

    @InjectMocks
    private WorkOrderServiceImpl workOrderService; // Change to implementation class

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllWorkOrders_shouldReturnAllWorkOrders() {
        // Arrange
        WorkOrder wo1 = new WorkOrder();
        wo1.setWoId(1L);
        wo1.setWoNumber("WO001");
        wo1.setMaterial(new Material()); // Add material to avoid NullPointerException if convertToDto accesses it
        WorkOrder wo2 = new WorkOrder();
        wo2.setWoId(2L);
        wo2.setWoNumber("WO002");
        wo2.setMaterial(new Material()); // Add material
        List<WorkOrder> workOrders = Arrays.asList(wo1, wo2); // Corrected: Use List directly

        when(workOrderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(PageRequest.class))).thenReturn(new PageImpl<>(workOrders));

        // Act
        List<WorkOrderDto> result = workOrderService.findAllWorkOrders(null, null, null, PageRequest.of(0, 10))
                .getContent();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("WO001", result.get(0).getWoNumber()); // Corrected: Check the first element
        verify(workOrderRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(PageRequest.class));
    }

    @Test
    void findWorkOrderById_shouldReturnWorkOrder_whenFound() {
        // Arrange
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);
        workOrder.setWoNumber("WO001");
        workOrder.setMaterial(new Material()); // Add material

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder));

        // Act
        Optional<WorkOrderDto> result = workOrderService.findWorkOrderById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("WO001", result.get().getWoNumber());
        verify(workOrderRepository, times(1)).findById(1L);
    }

    @Test
    void findWorkOrderById_shouldReturnEmpty_whenNotFound() {
        // Arrange
        when(workOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<WorkOrderDto> result = workOrderService.findWorkOrderById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(workOrderRepository, times(1)).findById(1L);
    }

    @Test
    void saveWorkOrder_shouldReturnSavedWorkOrder() {
        // Arrange
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setWoNumber("NewWO");
        request.setMaterialId(1L); // Assuming a material ID exists
        request.setRequiredQuantity(BigDecimal.valueOf(10));
        request.setStatus("PENDING");

        Material material = new Material();
        material.setMaterialId(1L);

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);
        workOrder.setWoNumber("NewWO");
        workOrder.setMaterial(material);
        workOrder.setRequiredQuantity(BigDecimal.valueOf(10));
        workOrder.setStatus("PENDING");

        when(materialRepository.findById(request.getMaterialId())).thenReturn(Optional.of(material));
        when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(workOrder);

        // Act
        WorkOrderDto result = workOrderService.createWorkOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("NewWO", result.getWoNumber());
        verify(workOrderRepository, times(1)).save(any(WorkOrder.class));
    }

    @Test
    void deleteWorkOrderById_shouldCallDeleteMethod() {
        // Arrange
        when(workOrderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(workOrderRepository).deleteById(1L);

        // Act
        workOrderService.deleteWorkOrderById(1L);

        // Assert
        verify(workOrderRepository, times(1)).deleteById(1L);
    }

    @Test
    void processMaterialPicking_shouldDecreaseStockAndCreateTransaction() {
        // Arrange
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);

        Material material = new Material();
        material.setMaterialId(1L);
        material.setMaterialName("Test Material");
        material.setStockCurrent(BigDecimal.valueOf(100));

        PickingRequest pickingRequest = new PickingRequest();
        pickingRequest.setWoId(1L);
        pickingRequest.setMaterialId(1L);
        pickingRequest.setRequestedQuantity(BigDecimal.valueOf(10));

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class)))
                .thenReturn(new InventoryTransaction());
        when(workOrderMaterialRepository.findByWorkOrder_WoId(anyLong())).thenReturn(Arrays.asList()); // No existing
                                                                                                       // WorkOrderMaterial
        WorkOrderMaterial savedWorkOrderMaterial = new WorkOrderMaterial();
        savedWorkOrderMaterial.setWorkOrder(workOrder);
        savedWorkOrderMaterial.setMaterial(material);
        savedWorkOrderMaterial.setRequestedQuantity(pickingRequest.getRequestedQuantity());
        savedWorkOrderMaterial.setIssuedQuantity(pickingRequest.getRequestedQuantity());
        savedWorkOrderMaterial.setStatus("ISSUED");
        when(workOrderMaterialRepository.save(any(WorkOrderMaterial.class))).thenAnswer(invocation -> {
            WorkOrderMaterial savedWom = invocation.getArgument(0);
            savedWom.setWoMaterialId(2L); // Simulate ID generation
            when(workOrderMaterialRepository.findById(2L)).thenReturn(Optional.of(savedWom)); // Mock findById to return
                                                                                              // the same instance
            return savedWom;
        });
        when(workOrderMaterialRepository.findById(2L)).thenReturn(Optional.of(savedWorkOrderMaterial));

        // Act
        WorkOrderMaterialDto result = workOrderService.processMaterialPicking(pickingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(90), material.getStockCurrent());
        verify(workOrderRepository, times(1)).findById(1L);
        verify(materialRepository, times(1)).findById(1L);
        verify(materialRepository, times(1)).save(material);
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
        verify(workOrderMaterialRepository, times(1)).save(any(WorkOrderMaterial.class));
    }

    @Test
    void processMaterialPicking_shouldThrowException_whenStockInsufficient() {
        // Arrange
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);

        Material material = new Material();
        material.setMaterialId(1L);
        material.setMaterialName("Test Material");
        material.setStockCurrent(BigDecimal.valueOf(5));

        PickingRequest pickingRequest = new PickingRequest();
        pickingRequest.setWoId(1L);
        pickingRequest.setMaterialId(1L);
        pickingRequest.setRequestedQuantity(BigDecimal.valueOf(10));

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workOrderService.processMaterialPicking(pickingRequest);
        });
        assertTrue(exception.getMessage().contains("Not enough material in stock"));
        verify(workOrderRepository, times(1)).findById(1L);
        verify(materialRepository, times(1)).findById(1L);
        verify(materialRepository, never()).save(any(Material.class));
        verify(inventoryTransactionRepository, never()).save(any(InventoryTransaction.class));
        verify(workOrderMaterialRepository, never()).save(any(WorkOrderMaterial.class));
    }

    @Test
    void findAllWorkOrderMaterials_shouldReturnAllWorkOrderMaterials() {
        // Arrange
        WorkOrderMaterial wom1 = new WorkOrderMaterial();
        wom1.setWoMaterialId(1L);
        wom1.setMaterial(new Material()); // Add material
        WorkOrderMaterial wom2 = new WorkOrderMaterial();
        wom2.setWoMaterialId(2L);
        wom2.setMaterial(new Material()); // Add material
        List<WorkOrderMaterial> materials = Arrays.asList(wom1, wom2);

        when(workOrderMaterialRepository.findAll()).thenReturn(materials);

        // Act
        List<WorkOrderMaterialDto> result = workOrderService.findAllWorkOrderMaterials();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(workOrderMaterialRepository, times(1)).findAll();
    }

    @Test
    void findWorkOrderMaterialsByWorkOrderId_shouldReturnMaterialsForGivenWorkOrder() {
        // Arrange
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);

        Material material = new Material();
        material.setMaterialId(1L);

        WorkOrderMaterial wom1 = new WorkOrderMaterial();
        wom1.setWoMaterialId(1L);
        wom1.setWorkOrder(workOrder);
        wom1.setMaterial(material);

        List<WorkOrderMaterial> materials = Arrays.asList(wom1);

        when(workOrderMaterialRepository.findByWorkOrder_WoId(1L)).thenReturn(materials);

        // Act
        List<WorkOrderMaterialDto> result = workOrderService.findWorkOrderMaterialsByWorkOrderId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getWoMaterialId().longValue());
        verify(workOrderMaterialRepository, times(1)).findByWorkOrder_WoId(1L);
    }

    @Test
    void updateWorkOrderProducedQuantity_shouldUpdateProducedAndSuccessfulQuantities() {
        // Arrange
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);
        workOrder.setProducedQuantity(BigDecimal.valueOf(10));
        workOrder.setSuccessfulQuantity(BigDecimal.valueOf(10));
        workOrder.setMaterial(new Material());

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WorkOrderDto result = workOrderService.updateWorkOrderProducedQuantity(1L, BigDecimal.valueOf(5));

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal(15).compareTo(result.getProducedQuantity()));
        assertEquals(0, new BigDecimal(15).compareTo(result.getSuccessfulQuantity()));
        verify(workOrderRepository, times(1)).findById(1L);
        verify(workOrderRepository, times(1)).save(any(WorkOrder.class));
    }

    @Test
    void updateWorkOrderProducedQuantity_shouldHandleNullInitialQuantities() {
        // Arrange
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoId(1L);
        workOrder.setProducedQuantity(null);
        workOrder.setSuccessfulQuantity(null);
        workOrder.setMaterial(new Material());

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WorkOrderDto result = workOrderService.updateWorkOrderProducedQuantity(1L, BigDecimal.valueOf(5));

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal(5).compareTo(result.getProducedQuantity()));
        assertEquals(0, new BigDecimal(5).compareTo(result.getSuccessfulQuantity()));
        verify(workOrderRepository, times(1)).findById(1L);
        verify(workOrderRepository, times(1)).save(any(WorkOrder.class));
    }
}