package com.project.depot.pickingorder.controller;

import com.project.depot.pickingorder.model.PickingOrder;
import com.project.depot.pickingorder.service.PickingOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/picking-orders")
@CrossOrigin(origins = { "http://localhost:5173", "http://172.22.34.82:5173" })
@Tag(name = "領料單管理", description = "處理領料單的相關操作")
public class PickingOrderController {

    @Autowired
    private PickingOrderService pickingOrderService;

    @Operation(summary = "獲取所有領料單", description = "獲取所有領料單列表，可選地根據狀態進行過濾")
    @GetMapping
    public ResponseEntity<List<PickingOrder>> getAllPickingOrders(@Parameter(description = "領料單狀態 (例如: PENDING, COMPLETED)", required = false) @RequestParam(required = false) String status) {
        List<PickingOrder> pickingOrders = pickingOrderService.getAllPickingOrders(status);
        return new ResponseEntity<>(pickingOrders, HttpStatus.OK);
    }

    @Operation(summary = "根據ID獲取領料單", description = "根據領料單ID查詢單一領料單的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<PickingOrder> getPickingOrderById(@Parameter(description = "領料單ID", required = true) @PathVariable Long id) {
        return pickingOrderService.getPickingOrderById(id)
                .map(pickingOrder -> new ResponseEntity<>(pickingOrder, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "建立領料單", description = "建立一個新的領料單")
    @PostMapping
    public ResponseEntity<PickingOrder> createPickingOrder(@Parameter(description = "領料單物件", required = true) @RequestBody PickingOrder pickingOrder) {
        PickingOrder createdPickingOrder = pickingOrderService.createPickingOrder(pickingOrder);
        return new ResponseEntity<>(createdPickingOrder, HttpStatus.CREATED);
    }

    @Operation(summary = "更新領料單", description = "根據領料單ID更新領料單資訊")
    @PutMapping("/{id}")
    public ResponseEntity<PickingOrder> updatePickingOrder(@Parameter(description = "領料單ID", required = true) @PathVariable Long id, @Parameter(description = "包含更新資料的領料單物件", required = true) @RequestBody PickingOrder pickingOrder) {
        PickingOrder updatedPickingOrder = pickingOrderService.updatePickingOrder(id, pickingOrder);
        if (updatedPickingOrder != null) {
            return new ResponseEntity<>(updatedPickingOrder, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "刪除領料單", description = "根據領料單ID刪除指定的領料單")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePickingOrder(@Parameter(description = "要刪除的領料單ID", required = true) @PathVariable Long id) {
        pickingOrderService.deletePickingOrder(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}