package com.sis.backend.repository

import com.sis.backend.dto.ValuationItemResponse
import com.sis.backend.dto.ValuationSummaryResponse
import com.sis.backend.dto.WarehouseValuationResponse
import java.math.BigDecimal
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn giá trị tồn kho (UC39).
 * 
 * CHỈ ADMIN được xem.
 * Chỉ tính kho đang hoạt động (w.is_active = TRUE).
 * Dùng BigDecimal cho tiền, scale 2, HALF_UP.
 * 
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class ValuationRepository(
    private val dataSource: DataSource
) {
    
    /**
     * Lấy tổng quan giá trị tồn kho (UC39 summary).
     * 
     * Bao gồm:
     * - totalValue: tổng giá trị (SUM(stock_quantity * cost_price))
     * - totalQuantity: tổng số lượng
     * - productCount: COUNT(DISTINCT product_id) có stock > 0
     * - warehouses: danh sách giá trị từng kho, sort DESC
     * 
     * @param warehouseId Lọc theo kho (null = tất cả kho). Chỉ dùng cho chi tiết, không dùng ở đây.
     * @param categoryId Lọc theo danh mục (null = tất cả).
     * @return Tổng quan giá trị.
     */
    fun getSummary(categoryId: Long? = null): ValuationSummaryResponse {
        // Query tổng
        val totalSql = """
            SELECT 
                COALESCE(SUM(i.stock_quantity * p.cost_price), 0) AS total_value,
                COALESCE(SUM(i.stock_quantity), 0) AS total_quantity,
                COUNT(DISTINCT CASE WHEN i.stock_quantity > 0 THEN p.product_id END) AS product_count
            FROM inventory i
            INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            INNER JOIN products p ON i.product_id = p.product_id
            WHERE w.is_active = TRUE
            ${if (categoryId != null) "AND p.category_id = ?" else ""}
        """.trimIndent()
        
        val (totalValue, totalQuantity, productCount) = dataSource.connection.use { conn ->
            conn.prepareStatement(totalSql).use { stmt ->
                if (categoryId != null) {
                    stmt.setLong(1, categoryId)
                }
                
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        Triple(
                            rs.getBigDecimal("total_value") ?: BigDecimal.ZERO,
                            rs.getInt("total_quantity"),
                            rs.getInt("product_count")
                        )
                    } else {
                        Triple(BigDecimal.ZERO, 0, 0)
                    }
                }
            }
        }
        
        // Query từng kho
        val warehouseSql = """
            SELECT 
                w.warehouse_id,
                w.warehouse_name,
                COALESCE(SUM(i.stock_quantity * p.cost_price), 0) AS total_value,
                COALESCE(SUM(i.stock_quantity), 0) AS total_quantity
            FROM warehouses w
            LEFT JOIN inventory i ON w.warehouse_id = i.warehouse_id
            LEFT JOIN products p ON i.product_id = p.product_id
            WHERE w.is_active = TRUE
            ${if (categoryId != null) "AND (p.category_id = ? OR p.category_id IS NULL)" else ""}
            GROUP BY w.warehouse_id, w.warehouse_name
            HAVING COALESCE(SUM(i.stock_quantity), 0) > 0
            ORDER BY total_value DESC
        """.trimIndent()
        
        val warehouses = dataSource.connection.use { conn ->
            conn.prepareStatement(warehouseSql).use { stmt ->
                if (categoryId != null) {
                    stmt.setLong(1, categoryId)
                }
                
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                WarehouseValuationResponse(
                                    warehouseId = rs.getLong("warehouse_id"),
                                    warehouseName = rs.getString("warehouse_name"),
                                    totalValue = rs.getBigDecimal("total_value") ?: BigDecimal.ZERO,
                                    totalQuantity = rs.getInt("total_quantity")
                                )
                            )
                        }
                    }
                }
            }
        }
        
        return ValuationSummaryResponse(
            totalValue = totalValue,
            totalQuantity = totalQuantity,
            productCount = productCount,
            warehouses = warehouses
        )
    }
    
    /**
     * Lấy chi tiết giá trị tồn kho với phân trang (UC39 detail).
     * 
     * Gom theo kho + sản phẩm.
     * Chỉ hiển thị dòng có SUM(stock_quantity) > 0.
     * Sắp xếp theo stockValue DESC.
     * 
     * @param warehouseId Lọc theo kho (null = tất cả kho).
     * @param categoryId Lọc theo danh mục (null = tất cả).
     * @param keyword Tìm kiếm theo SKU hoặc tên sản phẩm.
     * @param page Trang hiện tại (bắt đầu từ 1).
     * @param size Số dòng mỗi trang.
     * @return Danh sách chi tiết và thông tin phân trang.
     */
    fun getDetail(
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): Pair<List<ValuationItemResponse>, Long> {
        
        require(page >= 1) { "Số trang phải >= 1" }
        require(size in 1..100) { "Kích thước trang phải từ 1-100" }
        
        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()
        
        conditions.add("w.is_active = TRUE")
        
        warehouseId?.let {
            conditions.add("w.warehouse_id = ?")
            parameters.add(it)
        }
        
        categoryId?.let {
            conditions.add("p.category_id = ?")
            parameters.add(it)
        }
        
        keyword?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add("(p.sku LIKE ? OR p.product_name LIKE ?)")
            val searchValue = "%$it%"
            parameters.add(searchValue)
            parameters.add(searchValue)
        }
        
        val whereClause = "WHERE ${conditions.joinToString(" AND ")}"
        
        // Count total
        val countSql = """
            SELECT COUNT(*) FROM (
                SELECT 1
                FROM inventory i
                INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
                INNER JOIN products p ON i.product_id = p.product_id
                $whereClause
                GROUP BY w.warehouse_id, w.warehouse_name, p.product_id, p.sku, p.product_name, p.cost_price
                HAVING SUM(i.stock_quantity) > 0
            ) AS subquery
        """.trimIndent()
        
        val totalItems = dataSource.connection.use { conn ->
            conn.prepareStatement(countSql).use { stmt ->
                parameters.forEachIndexed { index, value ->
                    when (value) {
                        is Long -> stmt.setLong(index + 1, value)
                        is String -> stmt.setString(index + 1, value)
                    }
                }
                
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }
        }
        
        // Get items
        val offset = (page - 1) * size
        
        val selectSql = """
            SELECT 
                w.warehouse_id,
                w.warehouse_name,
                p.product_id,
                p.sku,
                p.product_name,
                p.cost_price,
                SUM(i.stock_quantity) AS total_quantity,
                SUM(i.stock_quantity * p.cost_price) AS stock_value
            FROM inventory i
            INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            INNER JOIN products p ON i.product_id = p.product_id
            $whereClause
            GROUP BY w.warehouse_id, w.warehouse_name, p.product_id, p.sku, p.product_name, p.cost_price
            HAVING SUM(i.stock_quantity) > 0
            ORDER BY stock_value DESC, p.product_name
            LIMIT ? OFFSET ?
        """.trimIndent()
        
        val items = dataSource.connection.use { conn ->
            conn.prepareStatement(selectSql).use { stmt ->
                parameters.forEachIndexed { index, value ->
                    when (value) {
                        is Long -> stmt.setLong(index + 1, value)
                        is String -> stmt.setString(index + 1, value)
                    }
                }
                
                stmt.setInt(parameters.size + 1, size)
                stmt.setInt(parameters.size + 2, offset)
                
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                ValuationItemResponse(
                                    warehouseId = rs.getLong("warehouse_id"),
                                    warehouseName = rs.getString("warehouse_name"),
                                    productId = rs.getLong("product_id"),
                                    sku = rs.getString("sku"),
                                    productName = rs.getString("product_name"),
                                    totalQuantity = rs.getInt("total_quantity"),
                                    currentCostPrice = rs.getBigDecimal("cost_price") ?: BigDecimal.ZERO,
                                    stockValue = rs.getBigDecimal("stock_value") ?: BigDecimal.ZERO
                                )
                            )
                        }
                    }
                }
            }
        }
        
        return Pair(items, totalItems)
    }
}
