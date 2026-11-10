package com.sis.backend.repository

import com.sis.backend.dto.InventoryItemResponse
import com.sis.backend.entity.Inventory
import javax.sql.DataSource

class InventoryRepository(private val dataSource: DataSource) {

    fun findAll(): List<InventoryItemResponse> {
        val query = """
            SELECT 
                i.inventory_id,
                COALESCE(p.sku, CONCAT('SP', LPAD(i.product_id, 4, '0'))) AS sku,
                COALESCE(p.product_name, 'Sản phẩm mẫu') AS product_name,
                COALESCE(w.warehouse_name, 'Kho trung tâm') AS warehouse_name,
                i.stock_quantity,
                i.reserved_quantity,
                (i.stock_quantity - i.reserved_quantity) AS available_quantity
            FROM inventory i
            LEFT JOIN products p ON i.product_id = p.product_id
            LEFT JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            ORDER BY i.inventory_id ASC
        """.trimIndent()

        val result = mutableListOf<InventoryItemResponse>()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.executeQuery().use { rs ->
                    while (rs.next()) {
                        val stock = rs.getInt("stock_quantity")
                        val reserved = rs.getInt("reserved_quantity")
                        val available = rs.getInt("available_quantity")
                        val name = rs.getString("product_name") ?: "Sản phẩm"
                        val initials = name.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                            .ifBlank { "SP" }

                        val status = when {
                            available < 20 -> "Tồn thấp"
                            available < 50 -> "Cận hạn"
                            else -> "Ổn định"
                        }

                        result.add(
                            InventoryItemResponse(
                                inventoryId = rs.getInt("inventory_id"),
                                sku = rs.getString("sku") ?: "",
                                productName = name,
                                warehouseName = rs.getString("warehouse_name") ?: "Kho",
                                stockQuantity = stock,
                                reservedQuantity = reserved,
                                availableQuantity = available,
                                status = status,
                                initials = initials
                            )
                        )
                    }
                    result
                }
            }
        }
    }

    fun findById(inventoryId: Int): Inventory? {
        val query = """
            SELECT inventory_id, warehouse_id, product_id, batch_code, stock_quantity, reserved_quantity
            FROM inventory
            WHERE inventory_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setInt(1, inventoryId)
                statement.executeQuery().use { rs ->
                    if (rs.next()) {
                        Inventory(
                            inventoryId = rs.getInt("inventory_id"),
                            warehouseId = rs.getInt("warehouse_id"),
                            productId = rs.getInt("product_id"),
                            batchCode = rs.getString("batch_code") ?: "",
                            stockQuantity = rs.getInt("stock_quantity"),
                            reservedQuantity = rs.getInt("reserved_quantity")
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun updateReservedQuantity(inventoryId: Int, reservedQuantity: Int): Boolean {
        val query = """
            UPDATE inventory
            SET reserved_quantity = ?
            WHERE inventory_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setInt(1, reservedQuantity)
                statement.setInt(2, inventoryId)
                statement.executeUpdate() > 0
            }
        }
    }
}
