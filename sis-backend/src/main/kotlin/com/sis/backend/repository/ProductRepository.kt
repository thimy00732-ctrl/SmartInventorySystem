package com.sis.backend.repository

import com.sis.backend.entity.Product
import com.sis.backend.exception.DuplicateBarcodeException
import com.sis.backend.exception.DuplicateSkuException
import com.sis.backend.mapper.mapProduct
import com.sis.backend.dto.ProductRequest
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Thực hiện các thao tác truy xuất dữ liệu sản phẩm bằng JDBC.
 *
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class ProductRepository(
    private val dataSource: DataSource
) {

    private companion object {
        const val SELECT_COLUMNS = """
            product_id, sku, barcode, product_name, description,
            unit, min_stock, category_id, status, created_by,
            created_at, updated_at
        """
    }

    /**
     * Lấy toàn bộ sản phẩm, sắp xếp theo ID giảm dần.
     *
     * @return Danh sách sản phẩm.
     */
    fun findAll(): List<Product> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "SELECT $SELECT_COLUMNS FROM products ORDER BY product_id DESC"
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapProduct(resultSet))
                        }
                    }
                }
            }
        }

    /**
     * Tìm sản phẩm theo ID.
     *
     * @param id ID sản phẩm cần tìm.
     * @return Sản phẩm nếu tồn tại, ngược lại trả về null.
     */
    fun findById(id: Long): Product? =
        dataSource.connection.use { connection ->
            findById(connection, id)
        }

    /**
     * Kiểm tra SKU đã được sử dụng hay chưa.
     *
     * @param sku SKU cần kiểm tra.
     * @param excludeId ID sản phẩm cần loại trừ khi cập nhật.
     * @return true nếu SKU đã được sử dụng.
     */
    fun existsBySku(sku: String, excludeId: Long? = null): Boolean =
        dataSource.connection.use { connection ->
            val sql = if (excludeId == null) {
                "SELECT 1 FROM products WHERE sku = ? LIMIT 1"
            } else {
                "SELECT 1 FROM products WHERE sku = ? AND product_id <> ? LIMIT 1"
            }

            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, sku)
                if (excludeId != null) {
                    statement.setLong(2, excludeId)
                }

                statement.executeQuery().use { it.next() }
            }
        }

    /**
     * Kiểm tra Barcode đã được sử dụng hay chưa.
     *
     * @param barcode Barcode cần kiểm tra; null được xem là không có Barcode.
     * @param excludeId ID sản phẩm cần loại trừ khi cập nhật.
     * @return true nếu Barcode đã được sử dụng.
     */
    fun existsByBarcode(
        barcode: String?,
        excludeId: Long? = null
    ): Boolean {
        if (barcode == null) return false

        return dataSource.connection.use { connection ->
            val sql = if (excludeId == null) {
                "SELECT 1 FROM products WHERE barcode = ? LIMIT 1"
            } else {
                "SELECT 1 FROM products WHERE barcode = ? AND product_id <> ? LIMIT 1"
            }

            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, barcode)
                if (excludeId != null) {
                    statement.setLong(2, excludeId)
                }

                statement.executeQuery().use { it.next() }
            }
        }
    }

    /**
     * Kiểm tra danh mục có tồn tại và đang hoạt động hay không.
     *
     * @param categoryId ID danh mục cần kiểm tra.
     * @return true nếu danh mục tồn tại và có trạng thái ACTIVE.
     */
    fun isActiveCategory(categoryId: Long): Boolean =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT 1
                FROM categories
                WHERE category_id = ? AND status = 'ACTIVE'
                LIMIT 1
                """.trimIndent()
            ).use { statement ->
                statement.setLong(1, categoryId)
                statement.executeQuery().use { it.next() }
            }
        }

    /**
     * Tạo sản phẩm mới.
     *
     * @param request Dữ liệu sản phẩm đã được kiểm tra.
     * @param createdBy ID người tạo, nếu có.
     * @return Sản phẩm vừa được tạo.
     * @throws DuplicateSkuException nếu SKU bị trùng tại thời điểm ghi.
     * @throws DuplicateBarcodeException nếu Barcode bị trùng tại thời điểm ghi.
     */
    fun create(request: ProductRequest, createdBy: Long? = null): Product =
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(
                    """
                    INSERT INTO products (
                        sku, barcode, product_name, description, unit,
                        min_stock, category_id, status, created_by
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    PreparedStatement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    bindProduct(statement, request)
                    if (createdBy == null) {
                        statement.setNull(9, java.sql.Types.BIGINT)
                    } else {
                        statement.setLong(9, createdBy)
                    }
                    statement.executeUpdate()

                    statement.generatedKeys.use { keys ->
                        if (!keys.next()) {
                            error("Không lấy được ID sản phẩm vừa tạo")
                        }

                        findById(connection, keys.getLong(1))
                            ?: error("Không tìm thấy sản phẩm vừa tạo")
                    }
                }
            } catch (exception: SQLException) {
                throw translateDuplicateException(exception)
            }
        }

    /**
     * Cập nhật thông tin sản phẩm.
     *
     * @param id ID sản phẩm cần cập nhật.
     * @param request Dữ liệu mới.
     * @return Sản phẩm sau khi cập nhật, hoặc null nếu không tồn tại.
     */
    fun update(id: Long, request: ProductRequest): Product? =
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(
                    """
                    UPDATE products
                    SET sku = ?, barcode = ?, product_name = ?, description = ?,
                        unit = ?, min_stock = ?, category_id = ?, status = ?
                    WHERE product_id = ?
                    """.trimIndent()
                ).use { statement ->
                    bindProduct(statement, request)
                    statement.setLong(9, id)
                    statement.executeUpdate()
                }

                findById(connection, id)
            } catch (exception: SQLException) {
                throw translateDuplicateException(exception)
            }
        }

    /**
     * Xóa sản phẩm theo ID.
     *
     * @param id ID sản phẩm cần xóa.
     * @return true nếu một bản ghi đã được xóa.
     */
    fun delete(id: Long): Boolean =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "DELETE FROM products WHERE product_id = ?"
            ).use { statement ->
                statement.setLong(1, id)
                statement.executeUpdate() > 0
            }
        }

    /**
     * Tìm sản phẩm bằng một kết nối JDBC đã mở.
     *
     * @param connection Kết nối đang được sử dụng.
     * @param id ID sản phẩm.
     * @return Sản phẩm hoặc null.
     */
    private fun findById(connection: Connection, id: Long): Product? =
        connection.prepareStatement(
            "SELECT $SELECT_COLUMNS FROM products WHERE product_id = ?"
        ).use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) mapProduct(resultSet) else null
            }
        }

    /**
     * Gán dữ liệu request vào các tham số SQL của sản phẩm.
     *
     * @param statement PreparedStatement cần gán dữ liệu.
     * @param request Dữ liệu sản phẩm.
     */
    private fun bindProduct(
        statement: PreparedStatement,
        request: ProductRequest
    ) {
        statement.setString(1, request.sku)
        if (request.barcode == null) {
            statement.setNull(2, java.sql.Types.VARCHAR)
        } else {
            statement.setString(2, request.barcode)
        }
        statement.setString(3, request.productName)
        if (request.description == null) {
            statement.setNull(4, java.sql.Types.LONGVARCHAR)
        } else {
            statement.setString(4, request.description)
        }
        statement.setString(5, request.unit)
        statement.setBigDecimal(
            6,
            java.math.BigDecimal.valueOf(request.minStock)
        )
        if (request.categoryId == null) {
            statement.setNull(7, java.sql.Types.BIGINT)
        } else {
            statement.setLong(7, request.categoryId)
        }
        statement.setString(8, request.status)
    }

    /**
     * Chuyển lỗi ràng buộc UNIQUE thành ngoại lệ nghiệp vụ nếu xác định được cột.
     *
     * @param exception Ngoại lệ SQL gốc.
     * @return Ngoại lệ nghiệp vụ tương ứng hoặc ngoại lệ SQL gốc.
     */
    private fun translateDuplicateException(
        exception: SQLException
    ): Exception {
        if (exception.sqlState == "23000" || exception.errorCode == 1062) {
            val message = exception.message.orEmpty().lowercase()

            return when {
                "sku" in message -> DuplicateSkuException()
                "barcode" in message -> DuplicateBarcodeException()
                else -> exception
            }
        }

        return exception
    }

    /**
     * Tìm kiếm sản phẩm theo tên hoặc SKU.
     *
     * @param keyword Từ khóa tìm kiếm; chuỗi rỗng trả về toàn bộ sản phẩm.
     * @return Danh sách sản phẩm phù hợp.
     */
    fun search(keyword: String): List<Product> {
        val normalizedKeyword = keyword.trim()

        if (normalizedKeyword.isEmpty()) {
            return findAll()
        }

        val escapedKeyword = normalizedKeyword
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")

        val sql = """
        SELECT $SELECT_COLUMNS
        FROM products
        WHERE product_name LIKE ? ESCAPE '!'
           OR sku LIKE ? ESCAPE '!'
        ORDER BY product_id DESC
    """.trimIndent()

        val searchPattern = "%$escapedKeyword%"

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, searchPattern)
                statement.setString(2, searchPattern)

                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapProduct(resultSet))
                        }
                    }
                }
            }
        }
    }

    /**
     * Tìm sản phẩm theo Barcode chính xác.
     *
     * @param barcode Barcode cần tra cứu.
     * @return Sản phẩm nếu tìm thấy; ngược lại trả về null.
     */
    fun findByBarcode(barcode: String): Product? {
        val sql = """
        SELECT $SELECT_COLUMNS
        FROM products
        WHERE barcode = ?
        LIMIT 1
    """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, barcode)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        mapProduct(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }
}