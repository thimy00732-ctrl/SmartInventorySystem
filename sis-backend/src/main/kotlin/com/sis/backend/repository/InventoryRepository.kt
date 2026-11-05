package com.sis.backend.repository

import javax.sql.DataSource

class InventoryRepository(private val dataSource: DataSource) {
    fun insertInitialStock(productId: Long, quantity: Int) {
        // Câu lệnh SQL thêm tồn kho ban đầu vào bảng inventory
        val sql = "INSERT INTO inventory (product_id, warehouse_id, stock_quantity, available_quantity) VALUES (?, 1, ?, ?)"
        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, productId)
                stmt.setInt(2, quantity)
                stmt.setInt(3, quantity)
                stmt.executeUpdate()
            }
        }
    }
}