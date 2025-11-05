# 專案修改日誌 (Changelog)

本文檔記錄了為了解決「完成工單」功能所進行的一系列前端與後端程式碼修改。

## 核心問題

最初，系統存在三個主要問題：
1.  在「完成工單」後，「已生產數量」沒有正確累加。
2.  「材料明細」列表始終為空。
3.  完成工單後，系統沒有自動更新成品和原料的庫存。

## 後端修改 (`EE202project_back_end`)

### 1. `WorkOrderServiceImpl.java`

這是本次修改的核心，主要有三個關鍵改動：

#### 1.1. 修復「生產數量」被覆蓋的錯誤

- **問題**: `updateQuantities` 方法的邏輯是「設定」新數量，而非「累加」，導致歷史生產數量遺失。
- **解決方案**: 將邏輯改為先讀取現有數量，再進行累加。

**修改前:**
```java
workOrder.setSuccessfulQuantity(successfulQuantity);
workOrder.setFailedQuantity(failedQuantity);
```

**修改後:**
```java
BigDecimal currentSuccessful = Optional.ofNullable(workOrder.getSuccessfulQuantity()).orElse(BigDecimal.ZERO);
workOrder.setSuccessfulQuantity(currentSuccessful.add(successfulQuantity));

BigDecimal currentFailed = Optional.ofNullable(workOrder.getFailedQuantity()).orElse(BigDecimal.ZERO);
workOrder.setFailedQuantity(currentFailed.add(failedQuantity));
```

#### 1.2. 新增「完成工單後更新庫存」的業務邏輯

- **問題**: `updateWorkOrderStatus` 方法只更新了工單狀態，沒有觸發任何庫存交易。
- **解決方案**: 重寫此方法，在檢測到狀態變為 `COMPLETED` 時，增加完整的庫存操作邏輯。

**修改後新增的核心邏輯:**
```java
if ("COMPLETED".equals(status)) {
    // 1. 增加成品庫存
    Material finishedProduct = workOrder.getMaterial();
    finishedProduct.setStockCurrent(...);
    materialRepository.save(finishedProduct);

    // 2. 記錄成品入庫日誌
    InventoryTransaction inboundLog = new InventoryTransaction();
    // ...
    inventoryTransactionRepository.save(inboundLog);

    // 3. 根據BOM表，扣除原料庫存
    List<BomComponent> bomComponents = bomComponentService.getBomComponentsByParentMaterialId(...);
    for (BomComponent component : bomComponents) {
        // ... 查詢原料並計算消耗量
        rawMaterial.setStockCurrent(...);
        materialRepository.save(rawMaterial);

        // ... 記錄原料出庫日誌
        inventoryTransactionRepository.save(outboundLog);
    }
}
```

#### 1.3. 新增「獲取BOM」的方法並轉換為DTO

- **問題**: 前端無法正確獲取產品的BOM物料清單，因為後端缺少對應的API，且回傳的資料結構不對。
- **解決方案**: 新增 `getWorkOrderBOM` 方法，並將查詢到的 `BomComponent` 物件，轉換為前端表格看得懂的 `WorkOrderMaterialDto` 格式。

**新增的方法 `getWorkOrderBOM`:**
```java
public List<WorkOrderMaterialDto> getWorkOrderBOM(Long woId) {
    // ... 根據 woId 找到產品
    // ... 呼叫 bomComponentService 找到 BOM 列表
    // ... 將 List<BomComponent> 轉換為 List<WorkOrderMaterialDto>
    return bomComponentDtos;
}
```

### 2. `WorkOrderService.java` (介面)

- **改動**: 新增了 `getWorkOrderBOM` 的介面定義，並將其回傳型別定義為 `List<WorkOrderMaterialDto>`。

### 3. `WorkOrderController.java`

- **改動**: 新增了一個 API 端點 `GET /api/workorder/{id}/bom`，將 `WorkOrderService` 中的 `getWorkOrderBOM` 方法暴露給前端呼叫。

### 4. 依賴注入

- **改動**: 為了讓 `WorkOrderServiceImpl` 能夠查詢BOM，將 `BomComponentService` 注入到了其建構子中。

---

## 前端修改 (`EE202project_front_end`)

### 1. `WorkOrderDetailModalFinish.vue`

- **問題**: 「材料明細」表格為空，因為呼叫了錯誤的後端API。
- **解決方案**: 修改了 `fetchWorkOrderMaterials` 方法，將其 API 請求從 `.../materials` 改為指向新建立的 `.../bom` 端點。

**修改前:**
```javascript
const res = await api.get(`/api/workorder/${localWorkOrder.value.woId}/materials`);
```

**修改後:**
```javascript
const res = await api.get(`/api/workorder/${localWorkOrder.value.woId}/bom`);
```

### 2. `ProductsView.vue`

- **問題**: 「生產」按鈕的功能與文字描述不符，且其API (`/workorder/produce`) 會錯誤地直接更新庫存。
- **解決方案**:
    1.  將按鈕文字從「生產」改為「建立工單」，使其意義更清晰。
    2.  將其API呼叫從 `/workorder/produce` 改為 `/workorder`，確保建立工單時不會觸發任何庫存操作。

---

## 最終修正 (Final Fix)

在完成以上修改後，前端的「已生產數量」顯示仍然有誤。經查，原因是後端資料庫模型 (`WorkOrder.java`) 與傳輸物件 (`WorkOrderDto.java`) 之間，存在 `producedQuantity` 和 `successfulQuantity` 兩個欄位的混用與不同步。

為徹底解決此問題，進行了以下「雙保險」修正：

### `WorkOrderServiceImpl.java`

1.  **同步更新資料庫**：在 `updateQuantities` 方法中，同時更新 `producedQuantity` 和 `successfulQuantity` 兩個欄位，確保它們在資料庫中永遠同步。

    ```java
    // ...
    workOrder.setSuccessfulQuantity(newSuccessfulTotal);
    workOrder.setProducedQuantity(newSuccessfulTotal); // 同步更新另一個欄位
    // ...
    ```

2.  **同步回傳給前端**：在 `convertToDto` 方法中，用 `successfulQuantity` 的值，同時設定DTO中的 `producedQuantity` 和 `successfulQuantity` 兩個欄位，確保不論前端使用哪個欄位，收到的都是正確的值。

    ```java
    // ...
    // 用 successfulQuantity 同時設定兩個欄位
    dto.setProducedQuantity(workOrder.getSuccessfulQuantity()); 
    dto.setSuccessfulQuantity(workOrder.getSuccessfulQuantity());
    // ...
    ```

---

## 405 Method Not Allowed 錯誤修復

- **問題**: 在 `WorkOrderView.vue` 中，其子元件 `ProductionManagementView.vue` 會發送 `GET /api/workorderfinish` 請求，但後端沒有對應的處理方法，導致 405 錯誤。
- **根本原因**: 後端 `WorkOrderFinishController.java` 雖然處理 `/api/workorderfinish` 路徑，但缺少一個無參數的 `@GetMapping` 來回應「獲取全部」的請求。
- **解決方案**: 在後端新增了獲取所有生產回報的功能。
    1.  **`WorkOrderFinishService.java`**: 新增 `getAllReports()` 方法，其內部呼叫 `finishRepository.findAll()`。
    2.  **`WorkOrderFinishController.java`**: 新增一個 `getAllReports()` 方法，並為其加上 `@GetMapping` 註解，將前一步的服務暴露為 API。

**新增的 Controller 方法:**
```java
@GetMapping
public ResponseEntity<List<WorkOderFinishBean>> getAllReports() {
    List<WorkOderFinishBean> reports = service.getAllReports();
    return ResponseEntity.ok(reports);
}
```

---

## Gemini CLI Agent 修改 (2025-08-26)

本次修改由 Gemini CLI Agent 執行，主要解決了工單列表顯示、分頁、過濾等問題，並新增了按狀態搜尋的功能。

### 核心問題

1.  **前端頁面解析錯誤**: `OutboundView.vue` 檔案中存在不可見的非標準空格字元，導致 `vite-plugin-vue-inspector` 拋出 `Duplicate` 錯誤。
2.  **工單列表無法顯示**:
    -   前端 API 請求路徑錯誤 ( `/api/api/...` )。
    -   後端 API 未支援分頁和關鍵字搜尋，導致前端無法正確解析回傳的資料。
3.  **缺少狀態搜尋功能**: 使用者無法按工單狀態 (如「待處理」、「進行中」) 篩選列表。

---

### 前端修改 (`EE202project_front_end`)

#### `views/OutboundView.vue`

##### 1. 修復頁面解析錯誤與 API 路徑

-   **問題**: 檔案中包含大量 `&nbsp;` 空格，導致編譯時出現非預期錯誤。同時，API 路徑與 `http-common.ts` 中的 `baseURL` 拼接後產生了重複的 `/api`。
-   **解決方案**:
    1.  將所有非標準空格替換為標準空格。
    2.  將 `API_BASE_URL` 從 `/api/workorder` 改回 `/workorder`，以配合 `axios` 的 `baseURL` 設定。
    3.  在 `searchFilters` 中新增 `status` 欄位，並在 `fetchWorkOrders` 和 `handleReset` 方法中加入對應的邏輯。
    4.  在工具列新增 `el-select` 下拉選單，用於選擇狀態進行篩選。

**修改後 (script setup):**
```typescript
// 搜尋過濾器
const searchFilters = reactive({
  woNumber: '',
  materialName: '',
  status: '', // 新增狀態過濾
})

// API 請求參數
const params = {
  // ...
  status: searchFilters.status || null, // 新增狀態參數
}

// 重置搜尋
const handleReset = () => {
  // ...
  searchFilters.status = ''; // 重置狀態
  // ...
}
```

**修改後 (template):**
```html
<div class="search-area">
  <!-- ... -->
  <el-select v-model="searchFilters.status" placeholder="請選擇狀態" clearable @change="handleSearch">
    <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value"></el-option>
  </el-select>
  <!-- ... -->
</div>
```

---

### 後端修改 (`EE202project_back_end`)

#### 1. `WorkOrderRepository.java`

-   **問題**: Repository 未支援 JPA Specification，無法進行動態條件查詢。
-   **解決方案**: 繼承 `JpaSpecificationExecutor<WorkOrder>` 介面。

**修改後:**
```java
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
}
```

#### 2. `WorkOrderService.java` (介面)

-   **問題**: `findAllWorkOrders` 方法不支援分頁和過濾。
-   **解決方案**: 修改方法簽名，加入過濾參數 (`woNumber`, `materialName`, `status`) 和 `Pageable` 物件，並將回傳型別改為 `Page<WorkOrderDto>`。

**修改後:**
```java
Page<WorkOrderDto> findAllWorkOrders(String woNumber, String materialName, String status, Pageable pageable);
```

#### 3. `WorkOrderServiceImpl.java`

-   **問題**: `findAllWorkOrders` 的實作只會回傳所有資料，沒有分頁和過濾邏輯。
-   **解決方案**: 使用 `JpaSpecificationExecutor` 提供的動態查詢功能，根據傳入的參數 (`woNumber`, `materialName`, `status`) 動態建立查詢條件。

**修改後:**
```java
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
```

#### 4. `WorkOrderController.java`

-   **問題**: `getAllWorkOrders` 端點無法接收分頁和過濾參數。
-   **解決方案**: 在方法簽名中加入 `@RequestParam` 和 `Pageable` 參數，以接收前端傳來的所有篩選條件。

**修改後:**
```java
@GetMapping
public ResponseEntity<Page<WorkOrderDto>> getAllWorkOrders(
        @RequestParam(required = false) String woNumber,
        @RequestParam(required = false) String materialName,
        @RequestParam(required = false) String status,
        Pageable pageable) {
    Page<WorkOrderDto> workOrders = workOrderService.findAllWorkOrders(woNumber, materialName, status, pageable);
    return ResponseEntity.ok(workOrders);
}
```