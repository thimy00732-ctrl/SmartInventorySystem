package com.sis.backend.fixture

import javax.sql.DataSource

/**
 * Dữ liệu mẫu cho test UC29-32.
 */
object InventoryFixture {
    
    fun setupTestData(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                // 1. Warehouses
                stmt.execute("""
                    INSERT INTO warehouses (warehouse_id, warehouse_name, location, is_active) VALUES
                    (1, 'Kho Trung Tâm', 'Hà Nội', TRUE),
                    (2, 'Kho Chi Nhánh HCM', 'TP.HCM', TRUE),
                    (3, 'Kho Không Hoạt Động', 'Đà Nẵng', FALSE)
                """)
                
                // 2. Categories
                stmt.execute("""
                    INSERT INTO categories (category_id, category_name, description, is_active) VALUES
                    (1, 'Thực phẩm', 'Sản phẩm thực phẩm', TRUE),
                    (2, 'Đồ uống', 'Nước giải khát', TRUE)
                """)
                
                // 3. Suppliers
                stmt.execute("""
                    INSERT INTO suppliers (supplier_id, supplier_name, phone, email, is_active) VALUES
                    (1, 'Vinamilk', '0901234567', 'vinamilk@example.com', TRUE),
                    (2, 'Coca Cola', '0909876543', 'coca@example.com', TRUE)
                """)
                
                // 4. Products
                stmt.execute("""
                    INSERT INTO products (product_id, sku, barcode, product_name, category_id, supplier_id, unit, cost_price, sale_price, min_stock, max_stock, is_active) VALUES
                    (1, 'MILK-001', '1234567890', 'Sữa tươi Vinamilk', 1, 1, 'Hộp', 15000, 18000, 50, 500, TRUE),
                    (2, 'COLA-001', '0987654321', 'Coca Cola 330ml', 2, 2, 'Lon', 8000, 12000, 100, 1000, TRUE),
                    (3, 'TEST-999', NULL, 'Sản phẩm hết hàng', 1, 1, 'Cái', 10000, 15000, 10, 100, TRUE)
                """)
                
                // 5. Inventory
                stmt.execute("""
                    INSERT INTO inventory (inventory_id, warehouse_id, product_id, batch_code, manufacture_date, expiry_date, stock_quantity, reserved_quantity) VALUES
                    (1, 1, 1, 'LOT2024-01', '2024-01-01', '2024-12-31', 200, 10),
                    (2, 2, 1, 'LOT2024-02', '2024-02-01', '2025-01-31', 150, 0),
                    (3, 1, 2, '', NULL, NULL, 500, 50),
                    (4, 1, 3, '', NULL, NULL, 0, 0)
                """)
            }
        }
    }
}
