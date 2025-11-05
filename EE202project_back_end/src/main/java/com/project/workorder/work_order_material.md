# 工單領料明細模組變數說明 (Work Order Material Module Variables)

## WorkOrderMaterial (工單領料明細實體)
對應資料表: `outbound_work_order_materials`

| 欄位名稱           | 類型            | 說明                                 |
| :---------------   | :-------------- | :----------------------------------- |
| `woMaterialId`     | `Integer`       | 領料ID (主鍵)                        |
| `workOrder`        | `WorkOrder`     | 工單 (關聯至 `WorkOrder` 實體, 必填) |
| `material`         | `Material`      | 需要領用的原料 (關聯至 `Material` 實體, 必填) |
| `requestedQuantity`| `BigDecimal`    | 需求數量 (必填)                      |
| `issuedQuantity`   | `BigDecimal`    | 已發料數量                           |
| `status`           | `String`        | 狀態 (預設 `PENDING`)                |
