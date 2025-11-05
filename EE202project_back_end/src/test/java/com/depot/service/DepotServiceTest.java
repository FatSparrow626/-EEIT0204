
package com.depot.service;

import com.project.core.dao.EmployeeUserRepository;
import com.project.depot.dao.InboundReceiptRepository;
import com.project.depot.dao.InventoryTransactionRepository;
import com.project.depot.dao.MaterialRepository;
import com.project.depot.dto.request.InboundReceiptCreateRequest;
import com.project.depot.dto.request.InboundReceiptItemCreateRequest;
import com.project.depot.model.InboundReceipt;
import com.project.depot.model.InboundReceiptItem;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.model.Material;
import com.project.depot.service.DepotServiceImpl;
import com.project.employeeuser.model.EmployeeUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List; // 引入 List
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 倉庫服務單元測試
 */
@ExtendWith(MockitoExtension.class)
public class DepotServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private InboundReceiptRepository inboundReceiptRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private EmployeeUserRepository employeeUserRepository;

    @InjectMocks
    private DepotServiceImpl depotService;

    private Material material;
    private InboundReceipt inboundReceipt;

    @BeforeEach
    void setUp() {
        // 準備測試用物料
        material = new Material();
        material.setMaterialId(1L);
        material.setMaterialName("測試物料");
        material.setStockCurrent(new BigDecimal("100.00"));

        // 準備測試用入庫單
        inboundReceipt = new InboundReceipt();
        inboundReceipt.setInboundId(1L);
        inboundReceipt.setReceiptType("PURCHASE");
        inboundReceipt.setStatus("DRAFT");

        InboundReceiptItem item = new InboundReceiptItem();
        item.setMaterial(material);
        item.setReceivedQuantity(new BigDecimal("50.00"));
        item.setInboundReceipt(inboundReceipt);

        Set<InboundReceiptItem> items = new HashSet<>();
        items.add(item);
        inboundReceipt.setItems(items);

        // Mock EmployeeUser for handledBy
        EmployeeUser testUser = new EmployeeUser();
        testUser.setUsername("testuser");
        when(employeeUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateInboundReceipt() {
        // --- Mocking --- //
        // 當儲存入庫單時，返回我們準備的入庫單，並模擬 ID 生成
        when(inboundReceiptRepository.save(any(InboundReceipt.class))).thenAnswer(invocation -> {
            InboundReceipt savedReceipt = invocation.getArgument(0);
            savedReceipt.setInboundId(1L); // Simulate ID generation for InboundReceipt
            long itemIdCounter = 1L;
            for (InboundReceiptItem item : savedReceipt.getItems()) {
                item.setInboundItemId(itemIdCounter++); // Simulate ID generation for InboundReceiptItem
            }
            return savedReceipt;
        });
        // 當根據ID尋找物料時，返回我們準備的物料
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        // --- 執行測試 --- //
        InboundReceiptCreateRequest inboundReceiptCreateRequest = new InboundReceiptCreateRequest();
        inboundReceiptCreateRequest.setInboundDate(inboundReceipt.getInboundDate());
        inboundReceiptCreateRequest.setStatus(inboundReceipt.getStatus());
        inboundReceiptCreateRequest.setHandledBy("testuser"); // Set a dummy username

        List<InboundReceiptItemCreateRequest> itemCreateRequests = new ArrayList<>();
        for (InboundReceiptItem item : inboundReceipt.getItems()) {
            InboundReceiptItemCreateRequest itemCreateRequest = new InboundReceiptItemCreateRequest();
            itemCreateRequest.setMaterialId(item.getMaterial().getMaterialId());
            itemCreateRequest.setReceivedQuantity(item.getReceivedQuantity());
            itemCreateRequests.add(itemCreateRequest);
        }
        inboundReceiptCreateRequest.setItems(itemCreateRequests);

        depotService.createInboundReceipt(inboundReceiptCreateRequest);

        // --- 驗證 --- //
        // 1. 驗證物料庫存是否正確更新 (100 + 50 = 150)
        assertEquals(new BigDecimal("150.00"), material.getStockCurrent());
        // 2. 驗證 materialRepository.save() 是否被呼叫一次
        verify(materialRepository, times(1)).save(material);
        // 3. 驗證 inventoryTransactionRepository.save() 是否被呼叫一次
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
    }
}
