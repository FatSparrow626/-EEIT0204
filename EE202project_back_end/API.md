
# 倉儲管理系統 API 文件

此文件說明了倉儲管理後端的核心 API。

## 基礎路徑

所有 API 的基礎路徑為 `/api`。

---

## 倉庫模組 (`/depot`)

### 1. 獲取所有物料 (庫存) 列表

- **功能**: 查詢系統中所有物料的詳細資訊及其目前庫存。
- **URL**: `/api/depot/materials`
- **方法**: `GET`
- **成功回應 (200 OK)**:
  ```json
  [
    {
      "materialId": 1,
      "materialName": "螺絲 A 型",
      "unit": "個",
      "materialDescription": "標準 M3 螺絲",
      "location": "A01-01",
      "stockCurrent": 1500.00,
      "stockReserved": 200.00,
      "stockInShipping": 300.00,
      "safetyStock": 500,
      "reorderLevel": 600,
      "active": true
    },
    {
      "materialId": 2,
      "materialName": "主機板 B 型",
      "unit": "片",
      "materialDescription": "高效能主機板",
      "location": "B02-03",
      "stockCurrent": 80.00,
      "stockReserved": 10.00,
      "stockInShipping": 20.00,
      "safetyStock": 30,
      "reorderLevel": 40,
      "active": true
    }
  ]
  ```

### 2. 新增物料

- **功能**: 在系統中建立一個新的物料項目。
- **URL**: `/api/depot/materials`
- **方法**: `POST`
- **請求內文 (Request Body)**:
  ```json
  {
    "materialName": "新物料 C",
    "unit": "個",
    "location": "C01-01",
    "safetyStock": 100,
    "reorderLevel": 120
  }
  ```
- **成功回應 (201 Created)**:
  ```json
  {
    "materialId": 3,
    "materialName": "新物料 C",
    "unit": "個",
    "materialDescription": null,
    "location": "C01-01",
    "stockCurrent": 0.00,
    "stockReserved": 0.00,
    "stockInShipping": 0.00,
    "safetyStock": 100,
    "reorderLevel": 120,
    "active": true
  }
  ```

### 3. 建立入庫單 (執行入庫)

- **功能**: 建立一張入庫單，並根據單據內容增加對應物料的庫存。
- **URL**: `/api/depot/inbound-receipts`
- **方法**: `POST`
- **請求內文 (Request Body)**:
  ```json
  {
    "receiptType": "PURCHASE",
    "note": "來自供應商 A 的採購入庫",
    "items": [
      {
        "material": { "materialId": 1 },
        "receivedQuantity": 50.50
      },
      {
        "material": { "materialId": 2 },
        "receivedQuantity": 10.00
      }
    ]
  }
  ```
- **成功回應 (201 Created)**:
  ```json
  {
    "inboundId": 1,
    "purchaseOrder": null,
    "inboundDate": "2025-07-24",
    "receiptType": "PURCHASE",
    "status": "COMPLETED",
    "handledBy": null,
    "note": "來自供應商 A 的採購入庫",
    "createdAt": "2025-07-24T10:30:00Z",
    "updatedAt": "2025-07-24T10:30:00Z",
    "items": [
      {
        "inboundItemId": 1,
        "receivedQuantity": 50.50
      },
      {
        "inboundItemId": 2,
        "receivedQuantity": 10.00
      }
    ]
  }
  ```

### 4. 獲取所有入庫單列表

- **功能**: 查詢系統中所有的入庫單歷史紀錄。
- **URL**: `/api/depot/inbound-receipts`
- **方法**: `GET`
- **成功回應 (200 OK)**:
  ```json
  [
    {
      "inboundId": 1,
      "inboundDate": "2025-07-24",
      "receiptType": "PURCHASE",
      "status": "COMPLETED",
      "note": "來自供應商 A 的採購入庫"
    },
    {
      "inboundId": 2,
      "inboundDate": "2025-07-23",
      "receiptType": "RETURN",
      "status": "COMPLETED",
      "note": "客戶退貨入庫"
    }
  ]
  ```

---
