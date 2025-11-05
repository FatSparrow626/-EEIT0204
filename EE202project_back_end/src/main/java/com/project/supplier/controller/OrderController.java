package com.project.supplier.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.project.supplier.model.PurchaseOrder;
import com.project.supplier.model.PurchaseOrderItem;
import com.project.supplier.model.OrderInsertDTO;
import com.project.supplier.model.OrderUpdateDTO;
import com.project.supplier.service.MaterialService;
import com.project.supplier.service.OrderService;
import com.project.supplier.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 訂單模組 API 控制器
 * 提供訂單相關的 RESTful 介面，包含查詢、新增、更新、刪除等操作。
 */
@RestController // = @Controller + @ResponseBody 用於回傳JSON給前端
@RequestMapping("/api/order")
@Tag(name = "訂單管理", description = "處理採購訂單的相關操作")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private MaterialService materialService;

    /**
     * 顯示新增訂單的表單，提供供應商和物料列表。
     * 
     * @return 包含供應商和物料列表的 Map
     */
    @Operation(summary = "顯示新增訂單表單", description = "獲取新增訂單所需的供應商和物料列表")
    @GetMapping("/addForm")
    public Map<String, Object> showAddOrderForm() {
        Map<String, Object> data = new HashMap<>();
        data.put("suppliers", supplierService.getActiveSuppliers());
        data.put("materials", materialService.getActiveMaterials());
        return data;
    }

    /**
     * 新增訂單。
     * 
     * @param dto 包含訂單資訊的 DTO
     * @return 成功或失敗的訊息
     */
    @Operation(summary = "新增訂單", description = "新增一筆新的採購訂單")
    @PostMapping("/insert")
    public ResponseEntity<String> insertOrder(
            @Parameter(description = "訂單插入請求物件", required = true) @RequestBody OrderInsertDTO dto) {
        try {
            orderService.insertOrder(
                    dto.getSupplierId(),
                    dto.getOrderDate(),
                    dto.getOrderStatus(),
                    BigDecimal.ZERO,
                    dto.getMaterialIds(),
                    dto.getQuantities().stream().map(BigDecimal::new).collect(Collectors.toList()),
                    dto.getUnitPrices());
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error");
        }
    }

    /**
     * 顯示所有訂單列表。
     * 
     * @return 所有訂單的列表
     */
    @Operation(summary = "查詢所有訂單", description = "獲取所有採購訂單的列表，包含訂單項目")
    @GetMapping("/list")
    public List<PurchaseOrder> listOrders() {
        return orderService.getAllOrdersWithItems();
    }

    /**
     * 編輯訂單，提供訂單詳細資訊、供應商和物料列表。
     * 
     * @param id 訂單ID
     * @return 包含訂單詳細資訊的 Map
     */
    @Operation(summary = "編輯訂單", description = "根據訂單ID獲取訂單詳細資訊，並提供供應商和物料列表用於編輯")
    @GetMapping("/edit/{id}")
    public ResponseEntity<Map<String, Object>> editOrder(
            @Parameter(description = "訂單ID", required = true) @PathVariable int id) {
        try {
            PurchaseOrder order = orderService.getOrderById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("order", order);
            data.put("items", order.getItemList());
            data.put("supplier", order.getSupplier());
            data.put("suppliers", supplierService.getActiveSuppliers());
            data.put("materials", materialService.getActiveMaterials());

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 更新訂單。
     * 
     * @param dto 包含更新資訊的 DTO
     * @return 成功或失敗的訊息
     */
    @Operation(summary = "更新訂單", description = "根據訂單ID更新採購訂單的資訊")
    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(
            @Parameter(description = "訂單更新請求物件", required = true) @RequestBody OrderUpdateDTO dto) {
        try {
            orderService.updateOrder(
                    dto.getOrderId(),
                    dto.getSupplierId(),
                    dto.getOrderDate(),
                    dto.getOrderStatus(),
                    BigDecimal.ZERO,
                    dto.getMaterialIds(),
                    dto.getQuantities().stream().map(BigDecimal::new).collect(Collectors.toList()),
                    dto.getUnitPrices());
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("更新失敗");
        }
    }

    /**
     * 刪除訂單。
     * 
     * @param id 訂單ID
     * @return 成功或失敗的訊息
     */
    @Operation(summary = "刪除訂單", description = "根據訂單ID刪除指定的採購訂單")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(
            @Parameter(description = "要刪除的訂單ID", required = true) @PathVariable int id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 取得每月採購金額統計表。
     * 
     * @return 每月採購金額的列表
     */
    @Operation(summary = "獲取每月採購金額統計", description = "獲取每月採購訂單的總金額統計列表")
    @GetMapping("/amount-per-month")
    public List<Map<String, Object>> getMonthlyPurchaseTotal() {
        return orderService.getMonthlyPurchaseTotal();
    }

    /**
     * 取得各供應商採購佔比統計表。
     * 
     * @return 各供應商採購佔比的列表
     */
    @Operation(summary = "獲取各供應商採購佔比統計", description = "獲取各供應商採購金額佔總採購金額的比例統計列表")
    @GetMapping("/supplier-ratio")
    public List<Map<String, Object>> getSupplierPurchaseTotal() {
        return orderService.getSupplierPurchaseTotal();
    }

    /**
     * 匯出訂單資料為 CSV 檔案。
     * 
     * @param response HttpServletResponse 用於設定回應標頭和輸出檔案內容
     * @throws IOException 當輸出過程中發生錯誤時拋出
     */
    @GetMapping("/export/csv")
    public void exportOrdersToCsv(HttpServletResponse response) throws IOException {
        // 1) 回應標頭（檔名與編碼）
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''orders.csv");

        List<PurchaseOrder> orders = orderService.getAllOrdersWithItems();

        try (var out = response.getOutputStream();
                var osw = new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8);
                var writer = new java.io.BufferedWriter(osw)) {

            // 2) 寫入 BOM，避免 Excel 亂碼
            writer.write('\uFEFF');

            // 3) 表頭
            writer.write("訂單編號,供應商,訂單日期,狀態,小計,物料名稱,數量,單價,出貨狀態");
            writer.newLine();

            for (PurchaseOrder order : orders) {
                var supplierName = order.getSupplier() != null ? order.getSupplier().getSupplierName() : "";
                var orderId = order.getOrderId();
                var orderDate = order.getOrderDate(); // 目前是字串就直接輸出
                var status = translateStatus(order.getOrderStatus());
                var subTotal = String.format("%.2f", order.getSubTotal());

                var items = order.getItemList();
                if (items == null || items.isEmpty()) {
                    // 沒有明細也輸出一列
                    writer.write(String.join(",",
                            csv(orderId), csv(supplierName), csv(orderDate), csv(status), csv(subTotal),
                            "", "", "", "" // 明細欄位留空
                    ));
                    writer.newLine();
                    continue;
                }

                for (PurchaseOrderItem item : items) {
                    String materialName = String.valueOf(item.getMaterialName());
                    String qty = item.getQuantity() != null ? item.getQuantity().toPlainString() : "";
                    String unitPrice = String.format("%.2f", item.getUnitPrice()); // 你的欄位是 double
                    // 改：轉換成中文狀態
                    String deliveryStatus = translateStatus(item.getDeliveryStatus());

                    writer.write(String.join(",",
                            csv(orderId),
                            csv(supplierName),
                            csv(orderDate),
                            csv(status),
                            csv(subTotal),
                            csv(materialName),
                            csv(qty),
                            csv(unitPrice),
                            csv(deliveryStatus)));
                    writer.newLine();
                }
            }
            writer.flush();
        }
    }

    // 小工具：狀態碼轉中文
    private String translateStatus(String status) {
        if (status == null)
            return "";
        return switch (status) {
            case "PENDING" -> "待處理";
            case "COMPLETED" -> "已完成";
            case "PARTIALLY_RECEIVED" -> "部分到貨";
            case "CANCELLED" -> "已取消";
            default -> status; // 預設回傳原本值
        };
    }

    // 小工具：CSV 欄位跳脫（用雙引號包住並把內部 " 變成 ""）
    private static String csv(Object v) {
        if (v == null)
            return "\"\"";
        String s = String.valueOf(v);
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

}