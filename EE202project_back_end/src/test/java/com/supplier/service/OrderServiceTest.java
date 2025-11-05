package com.supplier.service;

import com.project.supplier.dao.OrderItemRepository;
import com.project.supplier.dao.OrderRepository;
import com.project.supplier.dao.SupplierRepository;
import com.project.supplier.model.PurchaseOrder;
import com.project.supplier.model.PurchaseOrderItem;
import com.project.supplier.model.Supplier;
import com.project.supplier.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void insertOrder_shouldReturnOrderId() throws Exception {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setSupplierId(1);

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        PurchaseOrder savedOrder = new PurchaseOrder();
        savedOrder.setOrderId(1);
        when(orderRepository.save(any(PurchaseOrder.class))).thenReturn(savedOrder);

        // Act
        int orderId = orderService.insertOrder(1, "2025-07-31", "PENDING", BigDecimal.ZERO, Arrays.asList("1"), Arrays.asList(new BigDecimal("10")), Arrays.asList("100.0"));

        // Assert
        assertEquals(1, orderId);
        verify(orderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(orderItemRepository, times(1)).save(any(PurchaseOrderItem.class));
    }

    @Test
    void getOrderById_shouldReturnOrder_whenFound() throws Exception {
        // Arrange
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(1);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act
        PurchaseOrder result = orderService.getOrderById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        verify(orderRepository, times(1)).findById(1);
    }

    @Test
    void getAllOrdersWithItems_shouldReturnAllOrders() {
        // Arrange
        PurchaseOrder order1 = new PurchaseOrder();
        order1.setOrderId(1);
        PurchaseOrder order2 = new PurchaseOrder();
        order2.setOrderId(2);
        List<PurchaseOrder> orders = Arrays.asList(order1, order2);

        when(orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderId"))).thenReturn(orders);

        // Act
        List<PurchaseOrder> result = orderService.getAllOrdersWithItems();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll(Sort.by(Sort.Direction.DESC, "orderId"));
    }

    @Test
    void deleteOrder_shouldDeleteOrderAndItems() throws Exception {
        // Arrange
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(1);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1);

        // Assert
        verify(orderItemRepository, times(1)).deleteAll(any());
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void updateOrder_shouldUpdateOrderAndItems() throws Exception {
        // Arrange
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(1);

        Supplier supplier = new Supplier();
        supplier.setSupplierId(1);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        // Act
        orderService.updateOrder(1, 1, "2025-07-31", "PENDING", BigDecimal.ZERO, Arrays.asList("1"), Arrays.asList(new BigDecimal("10")), Arrays.asList("100.0"));

        // Assert
        verify(orderRepository, times(2)).save(any(PurchaseOrder.class));
        verify(orderItemRepository, times(1)).deleteAll(any());
        verify(orderItemRepository, times(1)).save(any(PurchaseOrderItem.class));
    }
}