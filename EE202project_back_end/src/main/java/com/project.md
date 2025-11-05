-- =================================================================
-- 說明：此為根據您的最終需求整合後的完整倉儲管理系統資料庫結構
-- 版本：Final-snake_case-v2 (已修正 FK 錯誤)
-- 核心改動：
-- 1. 全面採納 INT IDENTITY 作為主鍵。
-- 2. 統一資料表命名為 snake_case (例如：purchase_orders)。
-- 3. 整合所有功能模組 (採購、入庫、工單、BOM、交易紀錄)。
-- 4. 修正 outbound_work_orders 中的多重串聯路徑 FK 錯誤。
-- =================================================================

-- =================================================================
-- 核心主檔 (Core Master Data)
-- =================================================================

--- 供應商表
CREATE TABLE core_suppliers (
supplier_id INT PRIMARY KEY IDENTITY(1,1), -- 自動遞增主鍵
supplier_name NVARCHAR(100) NOT NULL, -- 供應商名稱
pm NVARCHAR(50), -- 聯絡人
supplier_phone NVARCHAR(20), -- 電話
supplier_email NVARCHAR(100) UNIQUE, -- 電子郵件（唯一）
supplier_address NVARCHAR(200), -- 地址
active BIT DEFAULT 1, -- 是否啟用（1=啟用，0=下架）
created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(), -- 建立時間
updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME() -- 更新時間
);
GO

--- 物料表 (整合產品與庫存資訊)
CREATE TABLE core_materials (
material_id INT PRIMARY KEY IDENTITY(1,1),
material_name NVARCHAR(100) NOT NULL,
unit NVARCHAR(20) NOT NULL,
material_description NVARCHAR(200),
location NVARCHAR(50), -- 儲位
stock_current NUMERIC(18,4) DEFAULT 0, -- 現有庫存
stock_reserved NUMERIC(18,4) DEFAULT 0, -- 已預留庫存
stock_in_shipping NUMERIC(18,4) DEFAULT 0, -- 在途庫存
safety_stock INT, -- 安全庫存
reorder_level INT, -- 再訂購點
active BIT DEFAULT 1
);
GO

--- 使用者表
CREATE TABLE core_users (
user_id INT PRIMARY KEY IDENTITY(1,1),
username NVARCHAR(100) UNIQUE NOT NULL,
password_hash NVARCHAR(255) NOT NULL,
role NVARCHAR(50) NOT NULL,
full_name NVARCHAR(255) NOT NULL,
is_active BIT NOT NULL DEFAULT 1,
last_login_at DATETIME2,
created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

--- 供應商與物料對應表
CREATE TABLE core_material_suppliers (
id INT PRIMARY KEY IDENTITY(1,1),
supplier_id INT NOT NULL,
material_id INT NOT NULL,
offer_price DECIMAL(10,2) CHECK (offer_price >= 0), -- 供應商報價
active BIT DEFAULT 1,
CONSTRAINT fk_material_suppliers_supplier FOREIGN KEY (supplier_id) REFERENCES core_suppliers(supplier_id) ON DELETE CASCADE,
CONSTRAINT fk_material_suppliers_material FOREIGN KEY (material_id) REFERENCES core_materials(material_id) ON DELETE CASCADE,
CONSTRAINT uq_material_supplier UNIQUE (supplier_id, material_id)
);
GO

-- =================================================================
-- 採購與入庫模組 (Purchase & Inbound)
-- =================================================================

--- 採購訂單主表
CREATE TABLE purchase_orders (
order_id INT PRIMARY KEY IDENTITY(1000,1),
supplier_id INT NOT NULL,
order_date DATE DEFAULT GETDATE(),
order_status NVARCHAR(20) DEFAULT 'PENDING',
sub_total DECIMAL(12,2),
CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES core_suppliers(supplier_id)
);
GO

--- 採購訂單明細表
CREATE TABLE purchase_order_items (
order_item_id INT PRIMARY KEY IDENTITY(1,1),
order_id INT NOT NULL,
material_id INT NOT NULL,
quantity INT CHECK (quantity > 0),
unit_price DECIMAL(10,2) CHECK (unit_price >= 0),
delivery_status NVARCHAR(20) DEFAULT '未到貨',
CONSTRAINT fk_purchase_order_items_order FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id) ON DELETE CASCADE,
CONSTRAINT fk_purchase_order_items_material FOREIGN KEY (material_id) REFERENCES core_materials(material_id)
);
GO

--- 入庫主表
CREATE TABLE inbound_receipts (
inbound_id INT PRIMARY KEY IDENTITY(1,1),
order_id INT, -- 可為空，對應非採購入庫
inbound_date DATE NOT NULL DEFAULT GETDATE(),
receipt_type VARCHAR(50) NOT NULL DEFAULT 'PURCHASE',
status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
handled_by_user_id INT, -- 關聯至 core_users 表
note NVARCHAR(200),
created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
CONSTRAINT fk_inbound_receipts_order FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id),
CONSTRAINT fk_inbound_receipts_user FOREIGN KEY (handled_by_user_id) REFERENCES core_users(user_id) ON DELETE SET NULL,
CONSTRAINT chk_inbound_receipt_type CHECK (receipt_type IN ('PURCHASE', 'RETURN', 'ADJUSTMENT_IN', 'PRODUCTION_RECEIPT')),
CONSTRAINT chk_inbound_status CHECK (status IN ('DRAFT', 'COMPLETED', 'CANCELLED'))
);
GO

--- 入庫明細表
CREATE TABLE inbound_receipt_items (
inbound_item_id INT PRIMARY KEY IDENTITY(1,1),
inbound_id INT NOT NULL,
material_id INT NOT NULL,
received_quantity NUMERIC(15, 4) NOT NULL CHECK (received_quantity >= 0),
purchase_order_item_id INT NULL,
CONSTRAINT fk_inbound_items_inbound FOREIGN KEY (inbound_id) REFERENCES inbound_receipts(inbound_id) ON DELETE CASCADE,
CONSTRAINT fk_inbound_items_material FOREIGN KEY (material_id) REFERENCES core_materials(material_id),
CONSTRAINT fk_inbound_items_po_item FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items(order_item_id),
CONSTRAINT uq_inbound_item UNIQUE (inbound_id, material_id)
);
GO

-- =================================================================
-- 生產與出庫模組 (Production & Outbound)
-- =================================================================

--- 工單主檔
CREATE TABLE outbound_work_orders (
wo_id INT PRIMARY KEY IDENTITY(1,1),
wo_number NVARCHAR(100) UNIQUE NOT NULL,
material_id INT NOT NULL, -- 要生產的成品/半成品
required_quantity INT NOT NULL CHECK (required_quantity > 0),
status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
requested_by_user_id INT,
issued_by_user_id INT,
created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
CONSTRAINT fk_work_orders_material FOREIGN KEY (material_id) REFERENCES core_materials(material_id),
-- [修正] 將 ON DELETE SET NULL 改為 ON DELETE NO ACTION 來解決循環路徑問題
CONSTRAINT fk_work_orders_requested_by FOREIGN KEY (requested_by_user_id) REFERENCES core_users(user_id) ON DELETE NO ACTION,
CONSTRAINT fk_work_orders_issued_by FOREIGN KEY (issued_by_user_id) REFERENCES core_users(user_id) ON DELETE NO ACTION
);
GO

--- 工單領料明細
CREATE TABLE outbound_work_order_materials (
wo_material_id INT PRIMARY KEY IDENTITY(1,1),
wo_id INT NOT NULL,
material_id INT NOT NULL, -- 需要領用的原料
requested_quantity DECIMAL(18, 4) NOT NULL CHECK (requested_quantity > 0),
issued_quantity DECIMAL(18, 4) CHECK (issued_quantity >= 0),
status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
CONSTRAINT fk_wom_wo FOREIGN KEY (wo_id) REFERENCES outbound_work_orders(wo_id) ON DELETE CASCADE,
CONSTRAINT fk_wom_material FOREIGN KEY (material_id) REFERENCES core_materials(material_id),
CONSTRAINT uq_wo_material UNIQUE (wo_id, material_id)
);
GO

-- =================================================================
-- BOM 與稽核模組 (BOM & Auditing)
-- =================================================================

--- 物料清單 (BOM)
CREATE TABLE bom_components (
bom_component_id INT PRIMARY KEY IDENTITY(1,1),
parent_material_id INT NOT NULL, -- 被組裝的產品
component_material_id INT NOT NULL, -- 組成父產品的零件
quantity DECIMAL(18, 4) NOT NULL CHECK (quantity > 0),
notes NVARCHAR(MAX),
created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
CONSTRAINT fk_bom_parent FOREIGN KEY (parent_material_id) REFERENCES core_materials(material_id) ON DELETE CASCADE,
CONSTRAINT fk_bom_component FOREIGN KEY (component_material_id) REFERENCES core_materials(material_id),
CONSTRAINT uq_bom_parent_component UNIQUE (parent_material_id, component_material_id)
);
GO

--- 庫存交易紀錄
CREATE TABLE inventory_transactions (
transaction_id INT PRIMARY KEY IDENTITY(1,1),
material_id INT NOT NULL,
transaction_type NVARCHAR(50) NOT NULL,
quantity DECIMAL(18, 4) NOT NULL, -- 異動數量，可正可負
transaction_date DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
reference_table NVARCHAR(100), -- 來源表名，例如 'inbound_receipts'
reference_id INT, -- 來源表的主鍵 ID
created_by_user_id INT,
notes NVARCHAR(MAX),
CONSTRAINT fk_transactions_material FOREIGN KEY (material_id) REFERENCES core_materials(material_id),
CONSTRAINT fk_transactions_user FOREIGN KEY (created_by_user_id) REFERENCES core_users(user_id) ON DELETE SET NULL,
CONSTRAINT chk_inv_transaction_type CHECK (transaction_type IN ('PURCHASE_INBOUND', 'PRODUCTION_INBOUND', 'RETURN_INBOUND', 'ADJUSTMENT_INBOUND', 'PRODUCTION_OUTBOUND', 'SALES_OUTBOUND', 'ADJUSTMENT_OUTBOUND', 'TRANSFER'))
);
GO

要求 幫我建構 spring boot mvc 架構 用個這些表幫我分成 suppler(供應商) depot(倉庫) workorder(工單) 等三大模組 其中資料夾定義是在 com 底下 範例(com.suppler) (com.depot) (com.workorder) 然後架構幫我分成 dao model service controller 每個都幫我寫介面+實作
然後每個程式碼都要加繁體中文註解 然後幫我建構單元測試 然後幫我在檔案底下 寫一個.md 內容是這整個傳送的值的變數 , 然後幫我用 logger 去看反應

我要做的出入庫的 crud 還有能查看倉庫與進行倉庫管理的功能
