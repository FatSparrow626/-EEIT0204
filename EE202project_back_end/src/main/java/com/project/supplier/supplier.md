# 供應商模組變數說明 (Supplier Module Variables)

## Supplier (供應商實體)
對應資料表: `core_suppliers`

| 欄位名稱         | 類型        | 說明                                 |
| :--------------- | :---------- | :----------------------------------- |
| `supplierId`     | `Integer`   | 供應商ID (主鍵)                      |
| `supplierName`   | `String`    | 供應商名稱 (必填)                    |
| `pm`             | `String`    | 聯絡人                               |
| `supplierPhone`  | `String`    | 電話                                 |
| `supplierEmail`  | `String`    | 電子郵件 (唯一)                      |
| `supplierAddress`| `String`    | 地址                                 |
| `active`         | `boolean`   | 是否啟用 (預設為 `true`)             |
| `createdAt`      | `LocalDateTime` | 建立時間 (自動生成)                  |
| `updatedAt`      | `LocalDateTime` | 更新時間 (自動更新)                  |

## SupplierController (供應商控制器)

### API Endpoints:

*   **GET `/api/supplier`**
    *   **說明:** 獲取所有供應商列表。
    *   **回應:** `List<Supplier>`

*   **GET `/api/supplier/{id}`**
    *   **說明:** 根據ID獲取單一供應商。
    *   **路徑參數:**
        *   `id`: `Integer` - 供應商ID
    *   **回應:** `Supplier` (如果找到), `404 Not Found` (如果未找到)

*   **POST `/api/supplier`**
    *   **說明:** 新增一個供應商。
    *   **請求體:** `Supplier` (JSON 格式)
    *   **回應:** `Supplier` (儲存後的實體), `201 Created`

*   **PUT `/api/supplier/{id}`**
    *   **說明:** 更新一個現有的供應商。
    *   **路徑參數:**
        *   `id`: `Integer` - 供應商ID
    *   **請求體:** `Supplier` (JSON 格式，包含要更新的資訊)
    *   **回應:** `Supplier` (更新後的實體), `404 Not Found` (如果未找到)

*   **DELETE `/api/supplier/{id}`**
    *   **說明:** 根據ID刪除供應商。
    *   **路徑參數:**
        *   `id`: `Integer` - 供應商ID
    *   **回應:** `204 No Content` (成功刪除), `404 Not Found` (如果未找到)