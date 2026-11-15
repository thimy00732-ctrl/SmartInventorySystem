package com.sis.backend.repository

import com.sis.backend.dto.InventorySearchFilter
import com.sis.backend.dto.PageResult
import com.sis.backend.dto.StockStatus
import com.sis.backend.entity.Inventory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn và thao tác dữ liệu tồn kho.
 *
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class InventoryRepository(private val dataSource: DataSource) {

    /**
     * Lấy danh sách tồn kho theo điều kiện tìm kiếm, lọc và phân trang.
     *
     * @param filter Các điều kiện tìm kiếm, lọc và sắp xếp.
     * @param page Trang cần lấy, bắt đầu từ 1.
     * @param size Số items tối đa trên một trang.
     * @return Danh sách tồn kho cùng thông tin phân trang.
     */
    fun findPage(
        filter: InventorySearchFilter = InventorySearchFilter(),
        page: Int = 1,
        size: Int = 10
    ): PageResult<Inventory> {

        require(page >= 1) {
            "Số trang phải lớn hơn hoặc bằng 1."
        }

        require(size in 1..100) {
            "Kích thước trang phải nằm trong khoảng từ 1 đến 100."
        }

        require(
            filter.minQuantity == null ||
                    filter.maxQuantity == null ||
                    filter.minQuantity <= filter.maxQuantity
        ) {
            "Số lượng tối thiểu không được lớn hơn số lượng tối đa."
        }

        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()

        // Tìm kiếm theo keyword
        filter.keyword?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add(
                """
                (
                    p.sku LIKE ?
                    OR p.barcode LIKE ?
                    OR p.product_name LIKE ?
                )
                """.trimIndent()
            )

            val keyword = "%$it%"
            parameters.add(keyword)
            parameters.add(keyword)
            parameters.add(keyword)
        }

        // Lọc theo kho
        filter.warehouseId?.let {
            conditions.add("i.warehouse_id = ?")
            parameters.add(it)
        }

        // Lọc theo tên kho
        filter.warehouseName?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add("w.warehouse_name LIKE ?")
            parameters.add("%$it%")
        }

        // Lọc theo danh mục
        filter.categoryId?.let {
            conditions.add("p.category_id = ?")
            parameters.add(it)
        }

        // Lọc theo tên danh mục
        filter.categoryName?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add("c.category_name LIKE ?")
            parameters.add("%$it%")
        }

        // Lọc theo nhà cung cấp
        filter.supplierId?.let {
            conditions.add("p.supplier_id = ?")
            parameters.add(it)
        }

        // Lọc theo tên nhà cung cấp
        filter.supplierName?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add("s.supplier_name LIKE ?")
            parameters.add("%$it%")
        }

        // Lọc theo trạng thái tồn kho
        when (filter.stockStatus) {
            StockStatus.IN_STOCK -> conditions.add("i.stock_quantity > 0")
            StockStatus.OUT_OF_STOCK -> conditions.add("i.stock_quantity = 0")
            StockStatus.HAS_RESERVED -> conditions.add("i.reserved_quantity > 0")
            StockStatus.ALL -> {} // Không thêm điều kiện
        }

        // Lọc theo số lượng tồn
        filter.minQuantity?.let {
            conditions.add("i.stock_quantity >= ?")
            parameters.add(it)
        }

        filter.maxQuantity?.let {
            conditions.add("i.stock_quantity <= ?")
            parameters.add(it)
        }

        // Lọc theo ngày hết hạn
        filter.expiryDateFrom?.let {
            try {
                val date = LocalDate.parse(it)
                conditions.add("i.expiry_date >= ?")
                parameters.add(date)
            } catch (e: Exception) {
                throw IllegalArgumentException("Định dạng ngày hết hạn từ không hợp lệ (YYYY-MM-DD).")
            }
        }

        filter.expiryDateTo?.let {
            try {
                val date = LocalDate.parse(it)
                conditions.add("i.expiry_date <= ?")
                parameters.add(date)
            } catch (e: Exception) {
                throw IllegalArgumentException("Định dạng ngày hết hạn đến không hợp lệ (YYYY-MM-DD).")
            }
        }

        val whereClause = if (conditions.isEmpty()) {
            ""
        } else {
            "WHERE ${conditions.joinToString(" AND ")}"
        }

        // Whitelist cho sắp xếp
        val allowedSortColumns = mapOf(
            "stock_quantity" to "i.stock_quantity",
            "reserved_quantity" to "i.reserved_quantity",
            "available_quantity" to "i.available_quantity",
            "product_name" to "p.product_name",
            "expiry_date" to "i.expiry_date",
            "updated_at" to "i.updated_at"
        )

        val sortColumn = allowedSortColumns[filter.sortBy.lowercase()]
            ?: "i.stock_quantity"

        val sortDirection = when (filter.sortDirection.uppercase()) {
            "ASC" -> "ASC"
            else -> "DESC"
        }

        val offset = try {
            Math.multiplyExact(page.toLong() - 1L, size.toLong())
        } catch (exception: ArithmeticException) {
            throw IllegalArgumentException("Số trang vượt quá giới hạn.")
        }

        val countSql = """
            SELECT COUNT(*)
            FROM inventory i
            INNER JOIN products p ON i.product_id = p.product_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
            INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            $whereClause
        """.trimIndent()

        val selectSql = """
            SELECT
                i.inventory_id,
                i.warehouse_id,
                w.warehouse_name,
                i.product_id,
                p.product_name,
                p.sku,
                p.barcode,
                p.category_id,
                c.category_name,
                p.supplier_id,
                s.supplier_name,
                p.unit,
                i.batch_code,
                i.manufacture_date,
                i.expiry_date,
                i.stock_quantity,
                i.reserved_quantity,
                i.available_quantity,
                i.updated_at
            FROM inventory i
            INNER JOIN products p ON i.product_id = p.product_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
            INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            $whereClause
            ORDER BY $sortColumn $sortDirection, i.inventory_id DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return dataSource.connection.use { connection ->

            // Đếm tổng số bản ghi
            val totalItems = connection.prepareStatement(countSql).use { statement ->
                bindSearchParameters(statement, parameters)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.getLong(1)
                    } else {
                        0L
                    }
                }
            }

            // Lấy danh sách tồn kho của trang hiện tại
            val inventoryList = connection.prepareStatement(selectSql).use { statement ->
                bindSearchParameters(statement, parameters)

                statement.setInt(parameters.size + 1, size)
                statement.setLong(parameters.size + 2, offset)

                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapInventory(resultSet))
                        }
                    }
                }
            }

            val totalPages = if (totalItems == 0L) {
                0
            } else {
                ((totalItems - 1) / size + 1).toInt()
            }

            PageResult(
                items = inventoryList,
                page = page,
                size = size,
                totalItems = totalItems,
                totalPages = totalPages
            )
        }
    }

    /**
     * Lấy chi tiết tồn kho theo ID.
     *
     * @param inventoryId ID tồn kho.
     * @return Inventory nếu tồn tại, ngược lại null.
     */
    fun findById(inventoryId: Long): Inventory? {
        val sql = """
            SELECT
                i.inventory_id,
                i.warehouse_id,
                w.warehouse_name,
                i.product_id,
                p.product_name,
                p.sku,
                p.barcode,
                p.category_id,
                c.category_name,
                p.supplier_id,
                s.supplier_name,
                p.unit,
                i.batch_code,
                i.manufacture_date,
                i.expiry_date,
                i.stock_quantity,
                i.reserved_quantity,
                i.available_quantity,
                i.updated_at
            FROM inventory i
            INNER JOIN products p ON i.product_id = p.product_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
            INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            WHERE i.inventory_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, inventoryId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) mapInventory(resultSet) else null
                }
            }
        }
    }
    
    /**
     * Lấy danh sách tồn kho theo product ID (UC30 by-product).
     * 
     * Chỉ lấy kho đang hoạt động (is_active = TRUE).
     * Sắp xếp theo stock_quantity DESC.
     * 
     * @param productId ID sản phẩm.
     * @return Danh sách inventory của sản phẩm.
     */
    fun findByProductId(productId: Long): List<Inventory> {
        val sql = """
            SELECT
                i.inventory_id,
                i.warehouse_id,
                w.warehouse_name,
                i.product_id,
                p.product_name,
                p.sku,
                p.barcode,
                p.category_id,
                c.category_name,
                p.supplier_id,
                s.supplier_name,
                p.unit,
                i.batch_code,
                i.manufacture_date,
                i.expiry_date,
                i.stock_quantity,
                i.reserved_quantity,
                i.available_quantity,
                i.updated_at
            FROM inventory i
            INNER JOIN products p ON i.product_id = p.product_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
            INNER JOIN warehouses w ON i.warehouse_id = w.warehouse_id
            WHERE i.product_id = ?
              AND w.is_active = TRUE
            ORDER BY i.stock_quantity DESC, i.inventory_id DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, productId)

                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapInventory(resultSet))
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Cập nhật reserved_quantity (UC31) với transaction lock.
     * 
     * TRONG MỘT TRANSACTION:
     * 1. setAutoCommit(false)
     * 2. SELECT ... FOR UPDATE (khóa dòng)
     * 3. Kiểm tra 0 <= n <= stock_quantity
     * 4. UPDATE reserved_quantity = n
     * 5. Đọc lại và commit
     * 6. Rollback nếu lỗi
     * 7. Đặt lại autoCommit = true trong finally
     * 
     * @param inventoryId ID dòng tồn kho.
     * @param newReservedQuantity Số lượng giữ chỗ mới (>= 0, <= stock_quantity).
     * @return Inventory sau khi cập nhật, hoặc null nếu không tồn tại.
     * @throws IllegalArgumentException nếu vi phạm ràng buộc.
     */
    fun updateReserved(inventoryId: Long, newReservedQuantity: Int): Inventory? {
        val connection = dataSource.connection
        val originalAutoCommit = connection.autoCommit
        
        try {
            // Bắt đầu transaction
            connection.autoCommit = false
            
            // 1. Khóa dòng bằng SELECT ... FOR UPDATE
            val lockSql = """
                SELECT stock_quantity, reserved_quantity
                FROM inventory
                WHERE inventory_id = ?
                FOR UPDATE
            """.trimIndent()
            
            val (stockQuantity, currentReserved) = connection.prepareStatement(lockSql).use { stmt ->
                stmt.setLong(1, inventoryId)
                
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        Pair(rs.getInt("stock_quantity"), rs.getInt("reserved_quantity"))
                    } else {
                        // Không tồn tại → rollback và trả null
                        connection.rollback()
                        return null
                    }
                }
            }
            
            // 2. Validate: 0 <= n <= stock_quantity
            if (newReservedQuantity < 0) {
                connection.rollback()
                throw IllegalArgumentException(
                    "Số lượng giữ chỗ không được âm."
                )
            }
            
            if (newReservedQuantity > stockQuantity) {
                connection.rollback()
                throw IllegalArgumentException(
                    "Số lượng giữ chỗ ($newReservedQuantity) vượt quá tồn kho hiện tại ($stockQuantity)."
                )
            }
            
            // 3. UPDATE reserved_quantity
            // KHÔNG ghi available_quantity (cột sinh tự động)
            val updateSql = """
                UPDATE inventory
                SET reserved_quantity = ?
                WHERE inventory_id = ?
            """.trimIndent()
            
            connection.prepareStatement(updateSql).use { stmt ->
                stmt.setInt(1, newReservedQuantity)
                stmt.setLong(2, inventoryId)
                stmt.executeUpdate()
            }
            
            // 4. Commit transaction
            connection.commit()
            
            // 5. Đọc lại dòng sau khi cập nhật
            return findById(inventoryId)
            
        } catch (e: Exception) {
            // Rollback nếu có lỗi
            try {
                connection.rollback()
            } catch (rollbackEx: Exception) {
                // Log rollback error nhưng throw exception gốc
                println("Rollback error: ${rollbackEx.message}")
            }
            throw e
            
        } finally {
            // Đặt lại autoCommit về giá trị ban đầu
            try {
                connection.autoCommit = originalAutoCommit
                connection.close()
            } catch (e: Exception) {
                println("Error resetting autoCommit: ${e.message}")
            }
        }
    }

    /**
     * Thêm tồn kho ban đầu khi tạo sản phẩm mới.
     *
     * @param productId ID sản phẩm.
     * @param quantity Số lượng ban đầu.
     */
    fun insertInitialStock(productId: Long, quantity: Int) {
        val sql = """
            INSERT INTO inventory (product_id, warehouse_id, stock_quantity, available_quantity)
            VALUES (?, 1, ?, ?)
        """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, productId)
                stmt.setInt(2, quantity)
                stmt.setInt(3, quantity)
                stmt.executeUpdate()
            }
        }
    }

    /**
     * Bind parameters vào PreparedStatement theo kiểu dữ liệu.
     *
     * @param statement PreparedStatement cần bind.
     * @param parameters Danh sách parameters.
     */
    private fun bindSearchParameters(
        statement: PreparedStatement,
        parameters: List<Any>
    ) {
        parameters.forEachIndexed { index, param ->
            val position = index + 1

            when (param) {
                is String -> statement.setString(position, param)
                is Long -> statement.setLong(position, param)
                is Int -> statement.setInt(position, param)
                is Boolean -> statement.setBoolean(position, param)
                is LocalDate -> statement.setObject(position, param)
                else -> throw IllegalArgumentException(
                    "Kiểu dữ liệu không được hỗ trợ: ${param::class.simpleName}"
                )
            }
        }
    }

    /**
     * Map ResultSet thành Inventory entity.
     *
     * @param resultSet ResultSet từ query.
     * @return Inventory object.
     */
    private fun mapInventory(resultSet: ResultSet): Inventory {
        return Inventory(
            inventoryId = resultSet.getLong("inventory_id"),
            warehouseId = resultSet.getLong("warehouse_id"),
            warehouseName = resultSet.getString("warehouse_name"),
            productId = resultSet.getLong("product_id"),
            productName = resultSet.getString("product_name"),
            sku = resultSet.getString("sku"),
            barcode = resultSet.getString("barcode"),
            categoryId = resultSet.getObject("category_id") as? Long,
            categoryName = resultSet.getString("category_name"),
            supplierId = resultSet.getObject("supplier_id") as? Long,
            supplierName = resultSet.getString("supplier_name"),
            unit = resultSet.getString("unit"),
            batchCode = resultSet.getString("batch_code"),
            manufactureDate = resultSet.getDate("manufacture_date")?.toLocalDate(),
            expiryDate = resultSet.getDate("expiry_date")?.toLocalDate(),
            stockQuantity = resultSet.getInt("stock_quantity"),
            reservedQuantity = resultSet.getInt("reserved_quantity"),
            availableQuantity = resultSet.getInt("available_quantity"),
            updatedAt = resultSet.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}
