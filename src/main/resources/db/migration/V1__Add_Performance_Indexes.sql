-- Performance Optimization Indexes
-- This migration adds indexes to improve query performance for frequently accessed columns

-- Index for order queries by account_id (used in customer order queries)
CREATE INDEX IF NOT EXISTS idx_order_account_id ON "order"(account_id);

-- Index for order queries by origin_warehouse_id (used in warehouse admin queries)
CREATE INDEX IF NOT EXISTS idx_order_origin_warehouse_id ON "order"(origin_warehouse_id);

-- Index for order_item queries by order_id (used when fetching order items)
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);

-- Index for order_item queries by product_id
CREATE INDEX IF NOT EXISTS idx_order_item_product_id ON order_item(product_id);

-- Index for order_status queries by order_id and time (used for latest status queries)
CREATE INDEX IF NOT EXISTS idx_order_status_order_id_time ON order_status(order_id, time DESC);

-- Index for warehouse_product queries by product_id (used in quantity aggregation)
CREATE INDEX IF NOT EXISTS idx_warehouse_product_product_id ON warehouse_product(product_id);

-- Index for warehouse_product queries by warehouse_id
CREATE INDEX IF NOT EXISTS idx_warehouse_product_warehouse_id ON warehouse_product(warehouse_id);

-- Composite index for warehouse_product queries by both product_id and warehouse_id
CREATE INDEX IF NOT EXISTS idx_warehouse_product_product_warehouse ON warehouse_product(product_id, warehouse_id);

-- Index for warehouse_admin queries by account_id
CREATE INDEX IF NOT EXISTS idx_warehouse_admin_account_id ON warehouse_admin(account_id);

-- Index for warehouse_admin queries by warehouse_id
CREATE INDEX IF NOT EXISTS idx_warehouse_admin_warehouse_id ON warehouse_admin(warehouse_id);

-- Index for payment_proof queries by order_id
CREATE INDEX IF NOT EXISTS idx_payment_proof_order_id ON payment_proof(order_id);

-- Index for stock_ledger queries by warehouse_product_id
CREATE INDEX IF NOT EXISTS idx_stock_ledger_warehouse_product_id ON stock_ledger(warehouse_product_id);

-- Index for warehouse_ledger queries by origin_warehouse_product_id
CREATE INDEX IF NOT EXISTS idx_warehouse_ledger_origin_warehouse_product_id ON warehouse_ledger(origin_warehouse_product_id);

-- Index for warehouse_ledger queries by destination_warehouse_product_id
CREATE INDEX IF NOT EXISTS idx_warehouse_ledger_destination_warehouse_product_id ON warehouse_ledger(destination_warehouse_product_id);

-- Index for warehouse_ledger queries by status
CREATE INDEX IF NOT EXISTS idx_warehouse_ledger_status ON warehouse_ledger(status);

-- Spatial index for warehouse location queries (improves nearest warehouse lookups)
-- Note: This assumes PostGIS extension is enabled
CREATE INDEX IF NOT EXISTS idx_warehouse_location_gist ON warehouse USING GIST (location);

-- Index for product queries by category_id
CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);

-- Index for cart_item queries by account_id
CREATE INDEX IF NOT EXISTS idx_cart_item_account_id ON cart_item(account_id);

-- Index for account_permission queries by account_id
CREATE INDEX IF NOT EXISTS idx_account_permission_account_id ON account_permission(account_id);
