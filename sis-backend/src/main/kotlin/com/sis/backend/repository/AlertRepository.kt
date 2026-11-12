package com.sis.backend.repository

import com.sis.backend.dto.AlertSummaryResponse
import com.sis.backend.dto.LowStockAlertResponse
import com.sis.backend.dto.OverStockAlertResponse
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn cảnh báo tồn kho.
 * 
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class AlertRepository(
    private val dataSource: DataSource
) {
    
    /**
     * Lấy danh sách sản phẩm có tồn kho thấp (UC34).
     * 
     * Điều kiện:
     * - is_active = TRUE
     * - Kho đang hoạt động (w.is_active = TRUE)
     * - totalStock < min_stock
     * 
     * Sắp xếp theo missingQuantity giảm dần (thiếu nhiều nhất lên đầu).
     * 
     * @return Danh sách sản phẩm tồn thấp.
     */
    fun findLowStockAlerts(): List<LowStockAlertResponse> {
        val sql = """
            SELECT 
                p.product_id,
                p.sku,
                p.product_name,
                p.unit,
                p.supplier_id,
                s.supplier_name,
                p.min_stock,
                COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0) AS total_stock,
                CASE 
                    WHEN COALESCE(SUM(
                        CASE 
                            WHEN w.is_active = TRUE THEN i.stock_quantity 
                            ELSE 0 
                        END
                    ), 0) = 0 THEN 'OUT_OF_STOCK'
                    ELSE 'LOW'
                END AS level
            FROM products p
            LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
            LEFT JOIN inventory i ON p.product_id = i.product_id
            LEFT JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            WHERE p.is_active = TRUE
            GROUP BY p.product_id, p.sku, p.product_name, p.unit, p.supplier_id, s.supplier_name, p.min_stock
            HAVING COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0) < p.min_stock
            ORDER BY (p.min_stock - COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0)) DESC, p.product_name
        """.trimIndent()
        
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            val productId = resultSet.getLong("product_id")
                            val sku = resultSet.getString("sku")
                            val productName = resultSet.getString("product_name")
                            val unit = resultSet.getString("unit")
                            val supplierId = resultSet.getLong("supplier_id")
                            val supplierIdValue = if (resultSet.wasNull()) null else supplierId
                            val supplierName = resultSet.getString("supplier_name")
                            val minStock = resultSet.getInt("min_stock")
                            val totalStock = resultSet.getInt("total_stock")
                            val level = resultSet.getString("level")
                            val missingQuantity = minStock - totalStock
                            
                            add(
                                LowStockAlertResponse(
                                    productId = productId,
                                    sku = sku,
                                    productName = productName,
                                    unit = unit,
                                    supplierId = supplierIdValue,
                                    supplierName = supplierName,
                                    totalStock = totalStock,
                                    minStock = minStock,
                                    missingQuantity = missingQuantity,
                                    level = level
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách sản phẩm có tồn kho vượt mức (UC35).
     * 
     * Điều kiện:
     * - is_active = TRUE
     * - Kho đang hoạt động (w.is_active = TRUE)
     * - totalStock > max_stock
     * 
     * excessPercent = (totalStock / maxStock - 1) * 100, null nếu maxStock = 0.
     * Sắp xếp theo excessPercent giảm dần, sau đó excessQuantity.
     * 
     * @return Danh sách sản phẩm tồn vượt mức.
     */
    fun findOverStockAlerts(): List<OverStockAlertResponse> {
        val sql = """
            SELECT 
                p.product_id,
                p.sku,
                p.product_name,
                p.unit,
                p.max_stock,
                COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0) AS total_stock
            FROM products p
            LEFT JOIN inventory i ON p.product_id = i.product_id
            LEFT JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            WHERE p.is_active = TRUE
            GROUP BY p.product_id, p.sku, p.product_name, p.unit, p.max_stock
            HAVING COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0) > p.max_stock
            ORDER BY 
                CASE 
                    WHEN p.max_stock > 0 THEN (COALESCE(SUM(
                        CASE 
                            WHEN w.is_active = TRUE THEN i.stock_quantity 
                            ELSE 0 
                        END
                    ), 0) * 1.0 / p.max_stock - 1) * 100
                    ELSE 0
                END DESC,
                (COALESCE(SUM(
                    CASE 
                        WHEN w.is_active = TRUE THEN i.stock_quantity 
                        ELSE 0 
                    END
                ), 0) - p.max_stock) DESC,
                p.product_name
        """.trimIndent()
        
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            val productId = resultSet.getLong("product_id")
                            val sku = resultSet.getString("sku")
                            val productName = resultSet.getString("product_name")
                            val unit = resultSet.getString("unit")
                            val maxStock = resultSet.getInt("max_stock")
                            val totalStock = resultSet.getInt("total_stock")
                            val excessQuantity = totalStock - maxStock
                            
                            val excessPercent = if (maxStock > 0) {
                                "%.2f".format((totalStock.toDouble() / maxStock - 1) * 100)
                            } else {
                                null
                            }
                            
                            add(
                                OverStockAlertResponse(
                                    productId = productId,
                                    sku = sku,
                                    productName = productName,
                                    unit = unit,
                                    totalStock = totalStock,
                                    maxStock = maxStock,
                                    excessQuantity = excessQuantity,
                                    excessPercent = excessPercent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Lấy tổng quan cảnh báo (UC34, UC35).
     * 
     * Trả về:
     * - lowStockCount: số sản phẩm totalStock < min_stock
     * - outOfStockCount: số sản phẩm totalStock = 0 (subset của lowStockCount)
     * - overStockCount: số sản phẩm totalStock > max_stock
     * 
     * Chỉ tính kho đang hoạt động (w.is_active = TRUE).
     * 
     * @return Thống kê tổng quan.
     */
    fun getAlertSummary(): AlertSummaryResponse {
        val sql = """
            WITH product_stock AS (
                SELECT 
                    p.product_id,
                    p.min_stock,
                    p.max_stock,
                    COALESCE(SUM(
                        CASE 
                            WHEN w.is_active = TRUE THEN i.stock_quantity 
                            ELSE 0 
                        END
                    ), 0) AS total_stock
                FROM products p
                LEFT JOIN inventory i ON p.product_id = i.product_id
                LEFT JOIN warehouses w ON i.warehouse_id = w.warehouse_id
                WHERE p.is_active = TRUE
                GROUP BY p.product_id, p.min_stock, p.max_stock
            )
            SELECT 
                COALESCE(SUM(CASE WHEN total_stock < min_stock THEN 1 ELSE 0 END), 0) AS low_stock_count,
                COALESCE(SUM(CASE WHEN total_stock < min_stock AND total_stock = 0 THEN 1 ELSE 0 END), 0) AS out_of_stock_count,
                COALESCE(SUM(CASE WHEN total_stock > max_stock THEN 1 ELSE 0 END), 0) AS over_stock_count
            FROM product_stock
        """.trimIndent()
        
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        AlertSummaryResponse(
                            lowStockCount = resultSet.getInt("low_stock_count"),
                            outOfStockCount = resultSet.getInt("out_of_stock_count"),
                            overStockCount = resultSet.getInt("over_stock_count")
                        )
                    } else {
                        AlertSummaryResponse(
                            lowStockCount = 0,
                            outOfStockCount = 0,
                            overStockCount = 0
                        )
                    }
                }
            }
        }
    }
}
