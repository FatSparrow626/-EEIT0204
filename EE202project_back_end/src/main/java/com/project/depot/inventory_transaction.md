# 庫存交易紀錄模組變數說明 (Inventory Transaction Module Variables)

## InventoryTransaction (庫存交易紀錄實體)
對應資料表: `inventory_transactions`

| 欄位名稱           | 類型            | 說明                                 |
| :---------------   | :-------------- | :----------------------------------- |
| `transactionId`    | `Integer`       | 交易ID (主鍵)                        |
| `material`         | `Material`      | 物料 (關聯至 `Material` 實體)        |
| `transactionType`  | `String`        | 交易類型 (例如: `PURCHASE_INBOUND`, `PRODUCTION_OUTBOUND`) |
| `quantity`         | `BigDecimal`    | 異動數量 (可正可負)                  |
| `transactionDate`  | `LocalDateTime` | 交易日期 (自動生成)                  |
| `referenceTable`   | `String`        | 來源表名 (例如: `inbound_receipts`, `outbound_work_orders`) |
| `referenceId`      | `Integer`       | 來源表的主鍵 ID                      |
| `createdBy`        | `EmployeeUser`  | 建立者 (關聯至 `EmployeeUser` 實體)          |
| `notes`            | `String`        | 備註                                 |

## DepotController (倉庫控制器 - 庫存交易紀錄相關)

### API Endpoints:

*   **GET `/api/depot/transactions`**
    *   **說明:** 獲取所有庫存交易紀錄列表。
    *   **回應:** `List<InventoryTransaction>`
