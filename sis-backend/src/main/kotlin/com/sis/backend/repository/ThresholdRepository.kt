package com.sis.backend.repository

import com.sis.backend.dto.ProductThresholdResponse
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn liên quan đến ngưỡng tồn kho.
 * 
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class ThresholdRepository(
    private val dataSource: DataSource
) {
    
    /**
     * Lấy danh sách sản phẩm có thiết lập ngưỡng tồn kho (UC33).
     * 
     * CHỈ lấy sản phẩm đang hoạt động (is_active=TRUE).
     * Tính alertLevel dựa trên totalStock vs min_stock/max_stock.
     * 
     * @param warehouseId Lọc theo kho (null = tất cả kho).
     * @return Danh sách sản phẩm kèm thông tin ngưỡng.
     */
    fun findProductsWithThresholds(warehouseId: Long? = null): List<ProductThresholdResponse> {
        val sql = """
            SELECT 
                p.product_id,
                p.sku,
                p.product_name,
                p.unit,
                p.min_stock,
                p.max_stock,
                COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0) AS total_stock,
                COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.reserved_quantity 
                        ELSE 0 
                    END
                ), 0) AS total_reserved,
                COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.available_quantity 
                        ELSE 0 
                    END
                ), 0) AS total_available
            FROM products p
            LEFT JOIN inventory i ON p.product_id = i.product_id
            LEFT JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            WHERE p.is_active = TRUE
            ${if (warehouseId != null) "AND (i.warehouse_id = ? OR i.warehouse_id IS NULL)" else ""}
            GROUP BY p.product_id, p.sku, p.product_name, p.unit, p.min_stock, p.max_stock
            ORDER BY p.product_name
        """.trimIndent()
        
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                if (warehouseId != null) {
                    statement.setLong(1, warehouseId)
                }
                
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            val productId = resultSet.getLong("product_id")
                            val sku = resultSet.getString("sku")
                            val productName = resultSet.getString("product_name")
                            val unit = resultSet.getString("unit")
                            val minStock = resultSet.getInt("min_stock")
                            val maxStock = resultSet.getInt("max_stock")
                            val totalStock = resultSet.getInt("total_stock")
                            val totalReserved = resultSet.getInt("total_reserved")
                            val totalAvailable = resultSet.getInt("total_available")
                            
                            val alertLevel = calculateAlertLevel(totalStock, minStock, maxStock)
                            
                            add(
                                ProductThresholdResponse(
                                    productId = productId,
                                    sku = sku,
                                    productName = productName,
                                    unit = unit,
                                    totalStock = totalStock,
                                    totalReserved = totalReserved,
                                    totalAvailable = totalAvailable,
                                    minStock = minStock,
                                    maxStock = maxStock,
                                    alertLevel = alertLevel
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Tính mức cảnh báo dựa trên tồn kho hiện tại và ngưỡng.
     * 
     * Logic:
     * - LOW_STOCK: totalStock < minStock
     * - OVER_STOCK: totalStock > maxStock
     * - OK: trong khoảng [minStock, maxStock]
     * 
     * @param totalStock Tổng tồn hiện tại.
     * @param minStock Ngưỡng tối thiểu.
     * @param maxStock Ngưỡng tối đa.
     * @return "LOW_STOCK", "OVER_STOCK", hoặc "OK".
     */
    fun calculateAlertLevel(totalStock: Int, minStock: Int, maxStock: Int): String {
        return when {
            totalStock < minStock -> "LOW_STOCK"
            totalStock > maxStock -> "OVER_STOCK"
            else -> "OK"
        }
    }
}
