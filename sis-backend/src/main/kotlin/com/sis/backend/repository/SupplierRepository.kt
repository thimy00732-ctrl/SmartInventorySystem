package com.sis.backend.repository

import com.sis.backend.entity.Supplier
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn dữ liệu nhà cung cấp.
 *
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class SupplierRepository(
    private val dataSource: DataSource
) {


    /**
     * Lấy danh sách nhà cung cấp.
     *
     * Nếu keyword được cung cấp, tìm kiếm theo tên, số điện thoại
     * hoặc email. Mặc định chỉ lấy nhà cung cấp đang hoạt động.
     *
     * @param keyword Từ khóa tìm kiếm.
     * @param activeOnly Chỉ lấy nhà cung cấp đang hoạt động.
     * @return Danh sách nhà cung cấp phù hợp.
     */
    fun findAll(
        keyword: String? = null,
        activeOnly: Boolean = true
    ): List<Supplier> {
        val sql = StringBuilder(
            """
        SELECT supplier_id, supplier_name, phone, email,
               address, is_active, created_at, updated_at
        FROM suppliers
        WHERE 1 = 1
        """.trimIndent()
        )

        val searchKeyword = keyword?.trim()?.takeIf { it.isNotEmpty() }

        if (activeOnly) {
            sql.append(" AND is_active = 1 ")
        }

        if (searchKeyword != null) {
            sql.append(
                """

    AND (
        supplier_name LIKE ?
        OR phone LIKE ?
        OR email LIKE ?
    )
    """.trimIndent()
            )
        }

        sql.append(" ORDER BY supplier_id DESC")

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql.toString()).use { statement ->

                if (searchKeyword != null) {
                    val value = "%$searchKeyword%"

                    statement.setString(1, value)
                    statement.setString(2, value)
                    statement.setString(3, value)
                }

                statement.executeQuery().use { resultSet ->
                    return resultSet.toSupplierList()
                }
            }
        }
    }


    /**
     * Tìm nhà cung cấp theo mã.
     */
    fun findById(supplierId: Long): Supplier? {
        val sql = """
            SELECT supplier_id, supplier_name, phone, email,
                   address, is_active, created_at, updated_at
            FROM suppliers
            WHERE supplier_id = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, supplierId)

                statement.executeQuery().use { resultSet ->
                    return if (resultSet.next()) {
                        resultSet.toSupplier()
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra tên nhà cung cấp đã tồn tại hay chưa.
     *
     * excludeId được sử dụng khi cập nhật để bỏ qua chính nhà cung cấp đó.
     */
    fun existsByName(
        supplierName: String,
        excludeId: Long? = null
    ): Boolean {
        val sql = if (excludeId == null) {
            """
            SELECT COUNT(*)
            FROM suppliers
            WHERE LOWER(TRIM(supplier_name)) = LOWER(TRIM(?))
            """.trimIndent()
        } else {
            """
            SELECT COUNT(*)
            FROM suppliers
            WHERE LOWER(TRIM(supplier_name)) = LOWER(TRIM(?))
              AND supplier_id <> ?
            """.trimIndent()
        }

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, supplierName)

                if (excludeId != null) {
                    statement.setLong(2, excludeId)
                }

                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    return resultSet.getInt(1) > 0
                }
            }
        }
    }

    /**
     * Thêm nhà cung cấp mới.
     *
     * is_active, created_at và updated_at sử dụng giá trị mặc định
     * được khai báo trong database.
     */
    fun create(
        supplierName: String,
        phone: String?,
        email: String?,
        address: String?
    ): Long {
        val sql = """
            INSERT INTO suppliers (
                supplier_name,
                phone,
                email,
                address
            )
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(
                sql,
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { statement ->

                statement.setString(1, supplierName)
                statement.setString(2, phone)
                statement.setString(3, email)
                statement.setString(4, address)

                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    if (keys.next()) {
                        return keys.getLong(1)
                    }
                }
            }
        }

        throw IllegalStateException("Không thể lấy mã nhà cung cấp vừa tạo.")
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     *
     * updated_at được database tự động cập nhật.
     */
    fun update(
        supplierId: Long,
        supplierName: String,
        phone: String?,
        email: String?,
        address: String?
    ): Boolean {
        val sql = """
            UPDATE suppliers
            SET supplier_name = ?,
                phone = ?,
                email = ?,
                address = ?
            WHERE supplier_id = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, supplierName)
                statement.setString(2, phone)
                statement.setString(3, email)
                statement.setString(4, address)
                statement.setLong(5, supplierId)

                return statement.executeUpdate() > 0
            }
        }
    }

    /**
     * Ngừng hoạt động nhà cung cấp.
     *
     * Không xóa dữ liệu nhà cung cấp và không làm mất lịch sử
     * sản phẩm hoặc giao dịch đã phát sinh.
     */
    fun deactivate(supplierId: Long): Boolean {
        val sql = """
            UPDATE suppliers
            SET is_active = 0
            WHERE supplier_id = ?
              AND is_active = 1
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, supplierId)
                return statement.executeUpdate() > 0
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm thuộc nhà cung cấp.
     *
     * Trả về toàn bộ các trường của bảng products.
     */
    fun findProductsBySupplierId(
        supplierId: Long
    ): List<Map<String, Any?>> {
        val sql = """
            SELECT
                product_id,
                sku,
                barcode,
                product_name,
                image_url,
                category_id,
                supplier_id,
                unit,
                cost_price,
                sale_price,
                min_stock,
                max_stock,
                is_active,
                created_at,
                updated_at
            FROM products
            WHERE supplier_id = ?
            ORDER BY product_id DESC
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, supplierId)

                statement.executeQuery().use { resultSet ->
                    val products = mutableListOf<Map<String, Any?>>()

                    while (resultSet.next()) {
                        products.add(
                            mapOf(
                                "productId" to resultSet.getLong("product_id"),
                                "sku" to resultSet.getString("sku"),
                                "barcode" to resultSet.getString("barcode"),
                                "productName" to resultSet.getString("product_name"),
                                "imageUrl" to resultSet.getString("image_url"),
                                "categoryId" to resultSet.getNullableLong("category_id"),
                                "supplierId" to resultSet.getNullableLong("supplier_id"),
                                "unit" to resultSet.getString("unit"),
                                "costPrice" to resultSet.getBigDecimal("cost_price").toDouble(),
                                "salePrice" to resultSet.getBigDecimal("sale_price").toDouble(),
                                "minStock" to resultSet.getInt("min_stock"),
                                "maxStock" to resultSet.getInt("max_stock"),
                                "isActive" to (resultSet.getInt("is_active") == 1),
                                "createdAt" to resultSet.getTimestamp("created_at").toLocalDateTime().toString(),
                                "updatedAt" to resultSet.getTimestamp("updated_at").toLocalDateTime().toString()
                            )
                        )
                    }

                    return products
                }
            }
        }
    }

    /**
     * Lấy danh sách giao dịch nhập hàng của nhà cung cấp.
     *
     * Chỉ lấy transaction_type = IMPORT.
     */
    fun findImportTransactionsBySupplierId(
        supplierId: Long
    ): List<Map<String, Any?>> {
        val sql = """
            SELECT
                transaction_id,
                warehouse_id,
                target_warehouse_id,
                supplier_id,
                user_id,
                approved_by,
                transaction_type,
                transaction_subtype,
                status,
                note,
                rejection_reason,
                created_at,
                submitted_at,
                approved_at,
                completed_at,
                cancelled_at
            FROM transactions
            WHERE supplier_id = ?
              AND transaction_type = 'IMPORT'
            ORDER BY created_at DESC
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, supplierId)

                statement.executeQuery().use { resultSet ->
                    val transactions = mutableListOf<Map<String, Any?>>()

                    while (resultSet.next()) {
                        transactions.add(
                            mapOf(
                                "transactionId" to resultSet.getLong("transaction_id"),
                                "warehouseId" to resultSet.getLong("warehouse_id"),
                                "targetWarehouseId" to resultSet.getNullableLong("target_warehouse_id"),
                                "supplierId" to resultSet.getNullableLong("supplier_id"),
                                "userId" to resultSet.getLong("user_id"),
                                "approvedBy" to resultSet.getNullableLong("approved_by"),
                                "transactionType" to resultSet.getString("transaction_type"),
                                "transactionSubtype" to resultSet.getString("transaction_subtype"),
                                "status" to resultSet.getString("status"),
                                "note" to resultSet.getString("note"),
                                "rejectionReason" to resultSet.getString("rejection_reason"),
                                "createdAt" to resultSet.getTimestamp("created_at").toLocalDateTime().toString(),
                                "submittedAt" to resultSet.getNullableDateTime("submitted_at"),
                                "approvedAt" to resultSet.getNullableDateTime("approved_at"),
                                "completedAt" to resultSet.getNullableDateTime("completed_at"),
                                "cancelledAt" to resultSet.getNullableDateTime("cancelled_at")
                            )
                        )
                    }

                    return transactions
                }
            }
        }
    }

    /**
     * Lấy danh sách chi tiết sản phẩm của một giao dịch nhập hàng.
     *
     * Kết hợp transaction_details và products để lấy SKU,
     * tên sản phẩm cùng các trường chi tiết lô hàng.
     */
    fun findTransactionDetails(
        transactionId: Long
    ): List<Map<String, Any?>> {
        val sql = """
            SELECT
                td.detail_id,
                td.transaction_id,
                td.product_id,
                p.sku,
                p.product_name,
                td.batch_code,
                td.manufacture_date,
                td.expiry_date,
                td.quantity,
                td.unit_price,
                td.system_quantity,
                td.actual_quantity,
                td.difference_quantity,
                td.note
            FROM transaction_details td
            INNER JOIN products p
                ON p.product_id = td.product_id
            WHERE td.transaction_id = ?
            ORDER BY td.detail_id ASC
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, transactionId)

                statement.executeQuery().use { resultSet ->
                    val details = mutableListOf<Map<String, Any?>>()

                    while (resultSet.next()) {
                        details.add(
                            mapOf(
                                "detailId" to resultSet.getLong("detail_id"),
                                "transactionId" to resultSet.getLong("transaction_id"),
                                "productId" to resultSet.getLong("product_id"),
                                "sku" to resultSet.getString("sku"),
                                "productName" to resultSet.getString("product_name"),
                                "batchCode" to resultSet.getString("batch_code"),
                                "manufactureDate" to resultSet.getDate("manufacture_date")?.toString(),
                                "expiryDate" to resultSet.getDate("expiry_date")?.toString(),
                                "quantity" to resultSet.getInt("quantity"),
                                "unitPrice" to resultSet.getBigDecimal("unit_price").toDouble(),
                                "systemQuantity" to resultSet.getNullableInt("system_quantity"),
                                "actualQuantity" to resultSet.getNullableInt("actual_quantity"),
                                "differenceQuantity" to resultSet.getNullableInt("difference_quantity"),
                                "note" to resultSet.getString("note")
                            )
                        )
                    }

                    return details
                }
            }
        }
    }

    /**
     * Chuyển ResultSet hiện tại thành Supplier Entity.
     */
    private fun ResultSet.toSupplier(): Supplier {
        return Supplier(
            supplierId = getLong("supplier_id"),
            supplierName = getString("supplier_name"),
            phone = getString("phone"),
            email = getString("email"),
            address = getString("address"),
            isActive = getInt("is_active") == 1,
            createdAt = getTimestamp("created_at").toLocalDateTime(),
            updatedAt = getTimestamp("updated_at").toLocalDateTime()
        )
    }

    /**
     * Chuyển toàn bộ ResultSet thành danh sách Supplier.
     */
    private fun ResultSet.toSupplierList(): List<Supplier> {
        val suppliers = mutableListOf<Supplier>()

        while (next()) {
            suppliers.add(toSupplier())
        }

        return suppliers
    }

    /**
     * Lấy Long nullable từ ResultSet.
     */
    private fun ResultSet.getNullableLong(column: String): Long? {
        val value = getLong(column)
        return if (wasNull()) null else value
    }

    /**
     * Lấy Int nullable từ ResultSet.
     */
    private fun ResultSet.getNullableInt(column: String): Int? {
        val value = getInt(column)
        return if (wasNull()) null else value
    }

    /**
     * Lấy ngày giờ nullable từ ResultSet.
     */
    private fun ResultSet.getNullableDateTime(column: String): String? {
        return getTimestamp(column)?.toLocalDateTime()?.toString()
    }

    /**
     * Kích hoạt lại nhà cung cấp đã ngừng hoạt động.
     *
     * Cập nhật is_active thành TRUE.
     *
     * @param supplierId Mã nhà cung cấp cần kích hoạt.
     * @return true nếu cập nhật thành công.
     */
    fun reactivate(supplierId: Long): Boolean {
        val sql = """
        UPDATE suppliers
        SET is_active = TRUE
        WHERE supplier_id = ?
          AND is_active = FALSE
    """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, supplierId)
                return statement.executeUpdate() > 0
            }
        }
    }
}