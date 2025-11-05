# 工單模組變數說明 (Work Order Module Variables)

## WorkOrder (工單主檔實體)
對應資料表: `outbound_work_orders`

| 欄位名稱           | 類型            | 說明                                 |
| :---------------   | :-------------- | :----------------------------------- |
| `woId`             | `Integer`       | 工單ID (主鍵)                        |
| `woNumber`         | `String`        | 工單號碼 (唯一, 必填)                |
| `material`         | `Material`      | 要生產的成品/半成品 (關聯至 `Material` 實體, 必填) |
| `requiredQuantity` | `Integer`       | 需求數量 (必填)                      |
| `status`           | `String`        | 狀態 (預設 `PENDING`)                |
| `requestedBy`      | `EmployeeUser`  | 需求人員 (關聯至 `EmployeeUser` 實體)        |
| `issuedBy`         | `EmployeeUser`  | 開單人員 (關聯至 `EmployeeUser` 實體)        |
| `createdAt`        | `LocalDateTime` | 建立時間 (自動生成)                  |
| `updatedAt`        | `LocalDateTime` | 更新時間 (自動更新)                  |

## WorkOrderController (工單控制器)

### API Endpoints:

*   **GET `/api/workorder`**
    *   **說明:** 獲取所有工單列表。
    *   **回應:** `List<WorkOrder>`

*   **GET `/api/workorder/{id}`**
    *   **說明:** 根據ID獲取單一工單。
    *   **路徑參數:**
        *   `id`: `Integer` - 工單ID
    *   **回應:** `WorkOrder` (如果找到), `404 Not Found` (如果未找到)

*   **POST `/api/workorder`**
    *   **說明:** 新增一個工單。
    *   **請求體:** `WorkOrder` (JSON 格式)
    *   **回應:** `WorkOrder` (儲存後的實體), `201 Created`

*   **PUT `/api/workorder/{id}`**
    *   **說明:** 更新一個現有的工單。
    *   **路徑參數:**
        *   `id`: `Integer` - 工單ID
    *   **請求體:** `WorkOrder` (JSON 格式，包含要更新的資訊)
    *   **回應:** `WorkOrder` (更新後的實體), `404 Not Found` (如果未找到)

*   **DELETE `/api/workorder/{id}`**
    *   **說明:** 根據ID刪除工單。
    *   **路徑參數:**
        *   `id`: `Integer` - 工單ID
    *   **回應:** `204 No Content` (成功刪除), `404 Not Found` (如果未找到)

*   **POST `/api/workorder/picking`**
    *   **說明:** 處理工單領料 (出庫) 操作。
    *   **請求體:** `WorkOrderMaterial` (JSON 格式，包含領料數量和物料ID)
    *   **回應:** `WorkOrderMaterial` (處理後的實體), `200 OK` (成功), `400 Bad Request` (庫存不足或其他錯誤)

*   **GET `/api/workorder/{woId}/materials`**
    *   **說明:** 根據工單ID獲取其所有領料明細。
    *   **路徑參數:**
        *   `woId`: `Integer` - 工單ID
    *   **回應:** `List<WorkOrderMaterial>`
