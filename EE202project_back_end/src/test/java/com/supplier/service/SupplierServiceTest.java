package com.supplier.service;

import com.project.supplier.dao.SupplierRepository;
import com.project.supplier.model.Supplier;
import com.project.supplier.service.SupplierService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getActiveSuppliers_shouldReturnActiveSuppliers() {
        // Arrange
        Supplier supplier1 = new Supplier();
        supplier1.setSupplierId(1);
        supplier1.setSupplierName("Test Supplier 1");
        supplier1.setActive(true);
        Supplier supplier2 = new Supplier();
        supplier2.setSupplierId(2);
        supplier2.setSupplierName("Test Supplier 2");
        supplier2.setActive(true);
        List<Supplier> suppliers = Arrays.asList(supplier1, supplier2);

        when(supplierRepository.findByActiveTrueOrderBySupplierId()).thenReturn(suppliers);

        // Act
        List<Supplier> result = supplierService.getActiveSuppliers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Supplier 1", result.get(0).getSupplierName());
        verify(supplierRepository, times(1)).findByActiveTrueOrderBySupplierId();
    }

    @Test
    void deactivateSupplier_shouldSetSupplierInactive() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setSupplierId(1);
        supplier.setActive(true);

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        // Act
        supplierService.deactivateSupplier(1);

        // Assert
        assertFalse(supplier.isActive());
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    void insertSupplier_shouldReturnSavedSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setSupplierName("New Supplier");
        supplier.setPm("John Doe");
        supplier.setSupplierPhone("1234567890");
        supplier.setSupplierEmail("john.doe@example.com");
        supplier.setSupplierAddress("123 Main St");
        supplier.setSupplierNote("Test note");

        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // Act
        Supplier result = supplierService.insertSupplier("New Supplier", "John Doe", "1234567890", "john.doe@example.com", "123 Main St", "Test note");

        // Assert
        assertNotNull(result);
        assertEquals("New Supplier", result.getSupplierName());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_shouldReturnTrue_whenSupplierExists() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setSupplierId(1);

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        // Act
        boolean result = supplierService.updateSupplier(1, "Updated Name", "Updated PM", "0987654321", "updated.email@example.com", "456 Oak Ave", "Updated note");

        // Assert
        assertTrue(result);
        assertEquals("Updated Name", supplier.getSupplierName());
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    void getSupplierById_shouldReturnSupplier_whenFound() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setSupplierId(1);
        supplier.setSupplierName("Test Supplier");

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        // Act
        Supplier result = supplierService.getSupplierById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Test Supplier", result.getSupplierName());
        verify(supplierRepository, times(1)).findById(1);
    }
}