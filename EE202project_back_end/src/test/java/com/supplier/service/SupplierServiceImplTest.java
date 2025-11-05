// package com.supplier.service;

// import com.project.supplier.dao.SupplierRepository;
// import com.project.supplier.model.Supplier;
// import com.project.supplier.service.SupplierServiceImpl;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// class SupplierServiceImplTest {

//     @Mock
//     private SupplierRepository supplierRepository;

//     @InjectMocks
//     private SupplierServiceImpl supplierService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void findAllSuppliers_shouldReturnAllSuppliers() {
//         // Arrange
//         Supplier supplier1 = new Supplier();
//         supplier1.setSupplierId(1);
//         supplier1.setSupplierName("Test Supplier 1");
//         Supplier supplier2 = new Supplier();
//         supplier2.setSupplierId(2);
//         supplier2.setSupplierName("Test Supplier 2");
//         List<Supplier> suppliers = Arrays.asList(supplier1, supplier2);

//         when(supplierRepository.findAll()).thenReturn(suppliers);

//         // Act
//         List<Supplier> result = supplierService.findAllSuppliers();

//         // Assert
//         assertNotNull(result);
//         assertEquals(2, result.size());
//         assertEquals("Test Supplier 1", result.get(0).getSupplierName());
//         verify(supplierRepository, times(1)).findAll();
//     }

//     @Test
//     void findSupplierById_shouldReturnSupplier_whenFound() {
//         // Arrange
//         Supplier supplier = new Supplier();
//         supplier.setSupplierId(1);
//         supplier.setSupplierName("Test Supplier");

//         when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

//         // Act
//         Optional<Supplier> result = supplierService.findSupplierById(1);

//         // Assert
//         assertTrue(result.isPresent());
//         assertEquals("Test Supplier", result.get().getSupplierName());
//         verify(supplierRepository, times(1)).findById(1);
//     }

//     @Test
//     void findSupplierById_shouldReturnEmpty_whenNotFound() {
//         // Arrange
//         when(supplierRepository.findById(1)).thenReturn(Optional.empty());

//         // Act
//         Optional<Supplier> result = supplierService.findSupplierById(1);

//         // Assert
//         assertFalse(result.isPresent());
//         verify(supplierRepository, times(1)).findById(1);
//     }

//     @Test
//     void saveSupplier_shouldReturnSavedSupplier() {
//         // Arrange
//         Supplier supplier = new Supplier();
//         supplier.setSupplierName("New Supplier");

//         when(supplierRepository.save(supplier)).thenReturn(supplier);

//         // Act
//         Supplier result = supplierService.saveSupplier(supplier);

//         // Assert
//         assertNotNull(result);
//         assertEquals("New Supplier", result.getSupplierName());
//         verify(supplierRepository, times(1)).save(supplier);
//     }

//     @Test
//     void deleteSupplierById_shouldCallDeleteMethod() {
//         // Arrange
//         doNothing().when(supplierRepository).deleteById(1);

//         // Act
//         supplierService.deleteSupplierById(1);

//         // Assert
//         verify(supplierRepository, times(1)).deleteById(1);
//     }
// }
