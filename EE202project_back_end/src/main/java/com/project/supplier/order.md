# 訂單模組變數說明 (Order Module Variables)

## PurchaseOrder (採購訂單實體)
對應資料表: `purchase_orders`

| 欄位名稱         | 類型        | 說明                                 |
| :--------------- | :---------- | :----------------------------------- |
| `orderId`        | `Integer`   | 訂單ID (主鍵)                        |
| `supplier`       | `Supplier`  | 供應商 (關聯至 `Supplier` 實體, 必填) |
| `orderDate`      | `String`    | 訂單日期                             |
| `orderStatus`    | `String`    | 訂單狀態 (例如: `PENDING`, `COMPLETED`) |
| `subTotal`       | `double`    | 訂單總金額                           |
| `itemList`       | `List<PurchaseOrderItem>` | 訂單明細列表                         |

## PurchaseOrderItem (採購訂單明細實體)
對應資料表: `purchase_order_items`

| 欄位名稱         | 類型        | 說明                                 |
| :--------------- | :---------- | :----------------------------------- |
| `orderItemId`    | `Integer`   | 訂單明細ID (主鍵)                    |
| `order`          | `PurchaseOrder` | 訂單 (關聯至 `PurchaseOrder` 實體, 必填) |
| `materialId`     | `Integer`   | 物料ID (必填)                        |
| `quantity`       | `Integer`   | 數量 (必填)                          |
| `unitPrice`      | `double`    | 單價 (必填)                          |
| `deliveryStatus` | `String`    | 交貨狀態                             |

## OrderController (訂單控制器)

### API Endpoints:

*   **GET `/api/order/list`**
    *   **說明:** 獲取所有訂單列表。
    *   **回應:** `List<PurchaseOrder>`

*   **GET `/api/order/edit/{id}`**
    *   **說明:** 根據ID獲取單一訂單。
    *   **路徑參數:**
        *   `id`: `Integer` - 訂單ID
    *   **回應:** `Map<String, Object>` (包含訂單、訂單明細、供應商、所有供應商和所有物料的資訊), `404 Not Found` (如果未找到)

*   **POST `/api/order/insert`**
    *   **說明:** 新增一個訂單。
    *   **請求體:** `OrderInsertDTO` (JSON 格式)
    *   **回應:** `ResponseEntity<String>`

*   **PUT `/api/order/update`**
    *   **說明:** 更新一個現有的訂單。
    *   **請求體:** `OrderUpdateDTO` (JSON 格式)
    *   **回應:** `ResponseEntity<String>`

*   **DELETE `/api/order/delete/{id}`**
    *   **說明:** 根據ID刪除訂單。
    *   **路徑參數:**
        *   `id`: `Integer` - 訂單ID
    *   **回應:** `ResponseEntity<String>`

*   **GET `/api/order/amount-per-month`**
    *   **說明:** 獲取每月採購總額。
    *   **回應:** `List<Map<String, Object>>`

*   **GET `/api/order/supplier-ratio`**
    *   **說明:** 獲取各供應商採購佔比。
    *   **回應:** `List<Map<String, Object>>`