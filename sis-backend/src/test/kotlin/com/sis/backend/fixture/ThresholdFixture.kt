package com.sis.backend.fixture

import javax.sql.DataSource

/**
 * Dữ liệu mẫu cho test UC33-35 (Thresholds và Alerts).
 * 
 * Setup:
 * - 3 sản phẩm với ngưỡng khác nhau
 * - Sản phẩm 1: Tồn thấp (total=30 < min=50)
 * - Sản phẩm 2: OK (min=100 <= total=500 <= max=1000)
 * - Sản phẩm 3: Vượt mức (total=1500 > max=1000)
 * - Sản phẩm 4: max_stock=0 để test chia 0
 */
object ThresholdFixture {
    
    fun setupTestData(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                // 1. Warehouses
                stmt.execute("""
                    INSERT INTO warehouses (warehouse_id, warehouse_name, location, is_active) VALUES
                    (10, 'Kho Test Threshold', 'Hà Nội', TRUE),
                    (11, 'Kho Không Hoạt Động', 'HCM', FALSE)
                """)
                
                // 2. Categories
                stmt.execute("""
                    INSERT INTO categories (category_id, category_name, description, is_active) VALUES
                    (10, 'Thực phẩm Test', 'Test category', TRUE)
                """)
                
                // 3. Suppliers
                stmt.execute("""
                    INSERT INTO suppliers (supplier_id, supplier_name, phone, email, is_active) VALUES
                    (10, 'Nhà cung cấp Test', '0901111111', 'test@example.com', TRUE)
                """)
                
                // 4. Products với các ngưỡng khác nhau
                stmt.execute("""
                    INSERT INTO products (product_id, sku, barcode, product_name, category_id, supplier_id, unit, cost_price, sale_price, min_stock, max_stock, is_active) VALUES
                    (101, 'LOW-STOCK-01', '1111111111', 'Sản phẩm tồn thấp', 10, 10, 'Hộp', 10000, 15000, 50, 200, TRUE),
                    (102, 'OK-STOCK-02', '2222222222', 'Sản phẩm OK', 10, 10, 'Hộp', 20000, 25000, 100, 1000, TRUE),
                    (103, 'OVER-STOCK-03', '3333333333', 'Sản phẩm vượt mức', 10, 10, 'Hộp', 30000, 35000, 100, 1000, TRUE),
                    (104, 'MAX-ZERO-04', '4444444444', 'Sản phẩm max=0', 10, 10, 'Hộp', 5000, 8000, 0, 0, TRUE),
                    (105, 'NO-INV-05', '5555555555', 'Sản phẩm không có inventory', 10, 10, 'Hộp', 1000, 2000, 10, 100, TRUE),
                    (106, 'EQUAL-MIN-06', '6666666666', 'Sản phẩm bằng min', 10, 10, 'Hộp', 1000, 2000, 100, 200, TRUE),
                    (107, 'EQUAL-MAX-07', '7777777777', 'Sản phẩm bằng max', 10, 10, 'Hộp', 1000, 2000, 50, 100, TRUE),
                    (108, 'INACTIVE-08', '8888888888', 'Sản phẩm ngừng hoạt động', 10, 10, 'Hộp', 1000, 2000, 10, 100, FALSE)
                """)
                
                // 5. Inventory
                // Sản phẩm 101: LOW_STOCK (total=30 < min=50)
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 101, 'LOT-LOW', 30, 0)
                """)
                
                // Sản phẩm 102: OK (total=500, min=100, max=1000)
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 102, 'LOT-OK', 500, 50)
                """)
                
                // Sản phẩm 103: OVER_STOCK (total=1500 > max=1000)
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 103, 'LOT-OVER', 1500, 0)
                """)
                
                // Sản phẩm 104: max=0, có tồn để test chia 0
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 104, 'LOT-ZERO', 10, 0)
                """)
                
                // Sản phẩm 106: total = min_stock (không low, không over)
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 106, 'LOT-EQ-MIN', 100, 0)
                """)
                
                // Sản phẩm 107: total = max_stock (không low, không over)
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 107, 'LOT-EQ-MAX', 100, 0)
                """)
                
                // Sản phẩm 108: is_active=FALSE, có tồn nhưng không được tính
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (10, 108, 'LOT-INACTIVE', 50, 0)
                """)
                
                // Inventory ở kho không hoạt động - KHÔNG ĐƯỢC TÍNH
                stmt.execute("""
                    INSERT INTO inventory (warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity) VALUES
                    (11, 103, 'LOT-DISABLED-WH', 5000, 0)
                """)
            }
        }
    }
    
    fun cleanupTestData(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("DELETE FROM inventory WHERE warehouse_id IN (10, 11)")
                stmt.execute("DELETE FROM products WHERE product_id BETWEEN 101 AND 108")
                stmt.execute("DELETE FROM suppliers WHERE supplier_id = 10")
                stmt.execute("DELETE FROM categories WHERE category_id = 10")
                stmt.execute("DELETE FROM warehouses WHERE warehouse_id IN (10, 11)")
            }
        }
    }
}
