package com.project.supplier.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.supplier.model.Supplier;
import com.project.supplier.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 供應商模組 API 控制器
 * 提供供應商相關的 RESTful 介面，包含查詢、新增、更新、刪除等操作。
 */
//允許來自 http://localhost:5173 這個網址的前端網頁發送 CORS
@CrossOrigin(origins = {"http://localhost:5173", "http://172.22.34.82:5173"})
@RestController //= @Controller + @ResponseBody  用於回傳JSON給前端
@RequestMapping("/api/supplier") // 前端 axios 請求對 /supplier 開頭
@Tag(name = "供應商管理", description = "處理供應商資訊的相關操作")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 取得所有啟用供應商列表。
     * @return 啟用供應商的列表
     */
    @Operation(summary = "獲取所有啟用供應商", description = "獲取所有啟用狀態的供應商列表")
    @GetMapping("/list")
    public List<Supplier> listSuppliers() {
        return supplierService.getActiveSuppliers();
    }

    /**
     * 根據 ID 查詢單筆供應商。
     * @param id 供應商ID
     * @return 供應商實體，如果找不到則返回 null
     */
    @Operation(summary = "根據ID查詢供應商", description = "根據供應商ID查詢單一供應商的詳細資訊")
    @GetMapping("/{id}")
    public Supplier getSupplier(@Parameter(description = "供應商ID", required = true) @PathVariable int id) {
        return supplierService.getSupplierById(id);
    }

    /**
     * 新增供應商。
     * @param supplier 包含供應商資訊的實體
     * @return 新增後的供應商實體
     */
    @Operation(summary = "新增供應商", description = "新增一個新的供應商記錄")
    @PostMapping("/add")
    public Supplier addSupplier(@Parameter(description = "供應商物件", required = true) @RequestBody Supplier supplier) {
        return supplierService.insertSupplier(
            supplier.getSupplierName(),
            supplier.getPm(),
            supplier.getSupplierPhone(),
            supplier.getSupplierEmail(),
            supplier.getSupplierAddress(),
            supplier.getSupplierNote()
        );
    }

    /**
     * 更新供應商資訊。
     * @param supplier 包含更新資訊的供應商實體
     */
    @Operation(summary = "更新供應商", description = "根據供應商ID更新供應商資訊")
    @PutMapping("/update")
    public void updateSupplier(@Parameter(description = "供應商物件", required = true) @RequestBody Supplier supplier) {
        supplierService.updateSupplier(
            supplier.getSupplierId(),
            supplier.getSupplierName(),
            supplier.getPm(),
            supplier.getSupplierPhone(),
            supplier.getSupplierEmail(),
            supplier.getSupplierAddress(),
            supplier.getSupplierNote()
        );
    }

    /**
     * 下架供應商（將 active 設為 0）。
     * @param id 供應商ID
     */
    @Operation(summary = "刪除供應商", description = "根據供應商ID刪除指定的供應商記錄 (邏輯刪除，將active設為0)")
    @DeleteMapping("/{id}")
    public void deleteSupplier(@Parameter(description = "要刪除的供應商ID", required = true) @PathVariable int id) {
        supplierService.deactivateSupplier(id);
    }
}