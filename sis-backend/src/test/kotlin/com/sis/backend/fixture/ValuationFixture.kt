package com.sis.backend.fixture

import javax.sql.DataSource

/**
 * Dữ liệu mẫu cho test UC39 (Valuation).
 * 
 * Setup:
 * - 2 kho: 1 hoạt động, 1 không hoạt động
 * - 3 sản phẩm với cost_price khác nhau
 * - Test BigDecimal: 0.1 + 0.2 = 0.30 (không 0.30000000004)
 * - Sản phẩm có tồn = 0 không hiển thị
 * - Kho không hoạt động không tính
 */
object ValuationFixture {
    
    fun setupTestData(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                // 1. Warehouses
                stmt.execute("""
                    INSERT INTO warehouses (warehouse_id, warehouse_name, location, is_active) VALUES
                    (20, 'Kho Test Valuation', 'Hà Nội', TRUE),
                    (21, 'Kho Không Hoạt Động Valuation', 'HCM', FALSE)
                """)
                
                // 2. Categories
                stmt.execute("""
                    INSERT INTO categories (category_id, category_name, description, is_active) VALUES
                    (20, 'Danh mục Test Valuation', 'Test category', TRUE),
                    (21, 'Danh mục khác', 'Other category', TRUE)
                """)
                
                // 3. Suppliers
                stmt.execute("""
                    INSERT INTO suppliers (supplier_id, supplier_name, phone, email, is_active) VALUES
                    (20, 'Nhà cung cấp Valuation', '0902222222', 'valuation@example.com', TRUE)
                """)
                
                // 4. Products với cost_price khác nhau
                // Product 201: cost_price = 10000, stock = 100 → value = 1,000,000
                // Product 202: cost_price = 15000, stock = 50 → value = 750,000
                // Product 203: cost_price = 20000, stock = 0 → KHÔNG hiển thị
                // Product 204: Test BigDecimal 0.1 + 0.2 = 0.30
                stmt.execute("""
                    INSERT INTO products (product_id, sku, barcode, product_name, category_id, supplier_id, unit, cost_price, sale_price, min_stock, max_stock, is_active) VALUES
                    (201, 'VAL-001', '2011111111', 'Sản phẩm A', 20, 20, 'Hộp', 10000.00, 15000.00, 10, 200, TRUE),
                    (202, 'VAL-002', '2022222222', 'Sản phẩm B', 20, 20, 'Hộp', 15000.00, 20000.00, 10, 200, TRUE),
                    (203, 'VAL-003', '2033333333', 'Sản phẩm C - Hết hàng', 20, 20, 'Hộp', 20000.00, 25000.00, 10, 200, TRUE),
                    (204, 'VAL-004', '2044444444', 'Sản phẩm D - Test BigDecimal', 20, 20, 'Hộp', 0.10, 0.50, 10, 200, TRUE),
                    (205, 'VAL-005', '2055555555', 'Sản phẩm E - Danh mục khác', 21, 20, 'Hộp', 5000.00, 8000.00, 10, 200, TRUE)
                """)
                
                // 5. Inventory - kho hoạt động
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (20, 201, 'LOT-VAL-A', 100, 0),
                    (20, 202, 'LOT-VAL-B', 50, 0),
                    (20, 203, 'LOT-VAL-C', 0, 0),
                    (20, 204, 'LOT-VAL-D', 3, 0),
                    (20, 205, 'LOT-VAL-E', 200, 0)
                """)
                
                // Inventory - kho KHÔNG hoạt động (KHÔNG ĐƯỢC TÍNH)
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (21, 201, 'LOT-DISABLED-A', 1000, 0)
                """)
            }
        }
    }
    
    fun cleanupTestData(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("DELETE FROM inventory WHERE warehouse_id IN (20, 21)")
                stmt.execute("DELETE FROM products WHERE product_id BETWEEN 201 AND 205")
                stmt.execute("DELETE FROM suppliers WHERE supplier_id = 20")
                stmt.execute("DELETE FROM categories WHERE category_id IN (20, 21)")
                stmt.execute("DELETE FROM warehouses WHERE warehouse_id IN (20, 21)")
            }
        }
    }
}
