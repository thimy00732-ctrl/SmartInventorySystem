package com.sis.backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

/**
 * Cấu hình database cho Repository Integration Tests.
 * 
 * Kết nối đến MariaDB 10.4.32 trên XAMPP (localhost:3306).
 * Database test: warehouse_test (đã có sẵn 11 bảng, chưa có dữ liệu).
 * 
 * KHÔNG sử dụng Docker/Testcontainers/H2.
 * KHÔNG tác động đến database chính warehouse_db.
 */
object TestDatabaseConfig {

    /**
     * Tạo DataSource cho test, kết nối warehouse_test.
     * 
     * Mỗi test tự quản lý dữ liệu mẫu và dọn dẹp sau khi chạy.
     */
    fun createTestDataSource(): DataSource {
        val config = HikariConfig().apply {
            // MariaDB XAMPP
            jdbcUrl = "jdbc:mariadb://127.0.0.1:3306/warehouse_test?useUnicode=true&characterEncoding=utf8mb4"
            
            username = "root"
            password = "220305"
            
            // Dùng MariaDB driver chính thức cho test
            driverClassName = "org.mariadb.jdbc.Driver"
            
            // Pool nhỏ cho test
            maximumPoolSize = 5
            minimumIdle = 1
            connectionTimeout = 5_000
            poolName = "TestInventoryPool"
            
            // Auto-commit true cho test đơn giản
            // Test cần transaction sẽ tự setAutoCommit(false)
            isAutoCommit = true
        }

        return HikariDataSource(config)
    }

    /**
     * Dọn dẹp dữ liệu test trong các bảng liên quan đến UC29-32.
     * 
     * Thứ tự xóa tuân thủ foreign key constraints:
     * 1. transaction_details (FK đến transactions, products)
     * 2. transactions (FK đến warehouses, products, users, suppliers)
     * 3. inventory (FK đến warehouses, products)
     * 4. products (FK đến categories, suppliers)
     * 5. warehouses, categories, suppliers, users
     * 
     * KHÔNG xóa cấu trúc bảng, chỉ xóa dữ liệu (DELETE, không DROP).
     */
    fun cleanupTestData(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                // Tắt foreign key checks tạm thời
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0")
                
                // Xóa dữ liệu test (giữ nguyên cấu trúc bảng)
                stmt.execute("DELETE FROM transaction_details")
                stmt.execute("DELETE FROM transactions")
                stmt.execute("DELETE FROM inventory")
                stmt.execute("DELETE FROM products")
                stmt.execute("DELETE FROM warehouses")
                stmt.execute("DELETE FROM categories")
                stmt.execute("DELETE FROM suppliers")
                stmt.execute("DELETE FROM users")
                stmt.execute("DELETE FROM refresh_tokens")
                
                // Reset AUTO_INCREMENT về 1
                stmt.execute("ALTER TABLE transaction_details AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE transactions AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE inventory AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE products AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE warehouses AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE categories AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE suppliers AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE users AUTO_INCREMENT = 1")
                stmt.execute("ALTER TABLE refresh_tokens AUTO_INCREMENT = 1")
                
                // Bật lại foreign key checks
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1")
            }
        }
    }

    /**
     * Kiểm tra kết nối đến warehouse_test có hoạt động không.
     * 
     * @return true nếu kết nối thành công, false nếu thất bại.
     */
    fun testConnection(): Boolean {
        return try {
            createTestDataSource().connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT 1").use { rs ->
                        rs.next() && rs.getInt(1) == 1
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ TestDatabaseConfig: Không kết nối được warehouse_test")
            println("   Lỗi: ${e.message}")
            false
        }
    }
}
