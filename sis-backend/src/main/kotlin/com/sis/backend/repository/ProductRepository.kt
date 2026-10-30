package com.sis.backend.repository

import com.sis.backend.dto.product.ProductInventoryResponse
import com.sis.backend.entity.Product
import com.sis.backend.mapper.ProductInventoryMapper
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

/**
 * Điều kiện tìm kiếm và lọc sản phẩm.
 *
 * @property keyword Từ khóa tìm kiếm theo SKU, barcode hoặc tên sản phẩm.
 * @property categoryId Mã danh mục cần lọc.
 * @property supplierId Mã nhà cung cấp cần lọc.
 * @property isActive Trạng thái hoạt động; null là lấy cả hai trạng thái.
 * @property minPrice Giá bán thấp nhất.
 * @property maxPrice Giá bán cao nhất.
 * @property sortBy Cột dùng để sắp xếp.
 * @property sortDirection Chiều sắp xếp ASC hoặc DESC.
 */
data class ProductSearchFilter(
    val keyword: String? = null,
    val categoryId: Long? = null,
    val supplierId: Long? = null,
    val isActive: Boolean? = true,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val sortBy: String = "created_at",
    val sortDirection: String = "DESC"
)

/**
 * Kết quả danh sách sản phẩm có phân trang.
 *
 * @property items Danh sách sản phẩm của trang hiện tại.
 * @property page Trang hiện tại, bắt đầu từ 1.
 * @property size Số sản phẩm tối đa trên một trang.
 * @property totalItems Tổng số sản phẩm thỏa mãn điều kiện.
 * @property totalPages Tổng số trang.
 */
data class ProductPageResult(
    val items: List<Product>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)

/**
 * Repository xử lý truy vấn và thao tác dữ liệu sản phẩm.
 *
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class ProductRepository(
    private val dataSource: DataSource
) {

    /**
     * Lấy danh sách sản phẩm theo từ khóa tìm kiếm.
     *
     * @param keyword Từ khóa tìm kiếm, có thể để trống.
     * @param includeInactive Có lấy cả sản phẩm đã ngừng hoạt động hay không.
     * @return Danh sách sản phẩm.
     */
    fun findAll(
        keyword: String? = null,
        includeInactive: Boolean = false
    ): List<Product> {
        val sql = buildString {
            append(
                """
                SELECT product_id, sku, barcode, product_name, image_url,
                       category_id, supplier_id, unit, cost_price, sale_price,
                       min_stock, max_stock, is_active, created_at, updated_at
                FROM products
                WHERE 1 = 1
                """.trimIndent()
            )

            if (!includeInactive) {
                append(" AND is_active = TRUE")
            }

            if (!keyword.isNullOrBlank()) {
                append(
                    """
                    AND (
                        sku LIKE ?
                        OR barcode LIKE ?
                        OR product_name LIKE ?
                    )
                    """.trimIndent()
                )
            }

            append(" ORDER BY product_id DESC")
        }

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->

                if (!keyword.isNullOrBlank()) {
                    val searchValue = "%${keyword.trim()}%"
                    statement.setString(1, searchValue)
                    statement.setString(2, searchValue)
                    statement.setString(3, searchValue)
                }

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
     * Lấy danh sách sản phẩm theo điều kiện tìm kiếm, lọc và phân trang.
     *
     * @param filter Các điều kiện tìm kiếm, lọc và sắp xếp.
     * @param page Trang cần lấy, bắt đầu từ 1.
     * @param size Số sản phẩm tối đa trên một trang.
     * @return Danh sách sản phẩm cùng thông tin phân trang.
     */
    fun findPage(
        filter: ProductSearchFilter = ProductSearchFilter(),
        page: Int = 1,
        size: Int = 10
    ): ProductPageResult {

        require(page >= 1) {
            "Số trang phải lớn hơn hoặc bằng 1."
        }

        require(size in 1..100) {
            "Kích thước trang phải nằm trong khoảng từ 1 đến 100."
        }

        require(
            filter.minPrice == null ||
                    filter.maxPrice == null ||
                    filter.minPrice <= filter.maxPrice
        ) {
            "Giá bán thấp nhất không được lớn hơn giá bán cao nhất."
        }

        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()

        filter.keyword?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add(
                """
                (
                    sku LIKE ?
                    OR barcode LIKE ?
                    OR product_name LIKE ?
                )
                """.trimIndent()
            )

            val keyword = "%$it%"
            parameters.add(keyword)
            parameters.add(keyword)
            parameters.add(keyword)
        }

        filter.categoryId?.let {
            conditions.add("category_id = ?")
            parameters.add(it)
        }

        filter.supplierId?.let {
            conditions.add("supplier_id = ?")
            parameters.add(it)
        }

        filter.isActive?.let {
            conditions.add("is_active = ?")
            parameters.add(it)
        }

        filter.minPrice?.let {
            conditions.add("sale_price >= ?")
            parameters.add(it)
        }

        filter.maxPrice?.let {
            conditions.add("sale_price <= ?")
            parameters.add(it)
        }

        val whereClause = if (conditions.isEmpty()) {
            ""
        } else {
            "WHERE ${conditions.joinToString(" AND ")}"
        }

        // Chỉ cho phép sắp xếp theo các cột được định nghĩa.
        val allowedSortColumns = mapOf(
            "product_id" to "product_id",
            "sku" to "sku",
            "barcode" to "barcode",
            "product_name" to "product_name",
            "cost_price" to "cost_price",
            "sale_price" to "sale_price",
            "created_at" to "created_at",
            "updated_at" to "updated_at"
        )

        val sortColumn = allowedSortColumns[filter.sortBy.lowercase()]
            ?: "created_at"

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
            FROM products
            $whereClause
        """.trimIndent()

        val selectSql = """
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
            $whereClause
            ORDER BY $sortColumn $sortDirection, product_id DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return dataSource.connection.use { connection ->

            // Đếm tổng số sản phẩm thỏa mãn điều kiện lọc.
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

            // Lấy danh sách sản phẩm của trang hiện tại.
            val products = connection.prepareStatement(selectSql).use { statement ->

                bindSearchParameters(statement, parameters)

                statement.setInt(parameters.size + 1, size)
                statement.setLong(parameters.size + 2, offset)

                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapProduct(resultSet))
                        }
                    }
                }
            }

            val totalPages = if (totalItems == 0L) {
                0
            } else {
                ((totalItems - 1) / size + 1).toInt()
            }

            ProductPageResult(
                items = products,
                page = page,
                size = size,
                totalItems = totalItems,
                totalPages = totalPages
            )
        }
    }

    /**
     * Tìm sản phẩm theo mã định danh.
     *
     * @param productId Mã sản phẩm.
     * @return Sản phẩm nếu tồn tại, ngược lại trả về null.
     */
    fun findById(productId: Long): Product? {
        val sql = """
            SELECT product_id, sku, barcode, product_name, image_url,
                   category_id, supplier_id, unit, cost_price, sale_price,
                   min_stock, max_stock, is_active, created_at, updated_at
            FROM products
            WHERE product_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, productId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) mapProduct(resultSet) else null
                }
            }
        }
    }

    /**
     * Tìm sản phẩm theo SKU.
     *
     * @param sku Mã SKU cần tìm.
     * @return Sản phẩm nếu tồn tại.
     */
    fun findBySku(sku: String): Product? {
        return findOne(
            """
            SELECT product_id, sku, barcode, product_name, image_url,
                   category_id, supplier_id, unit, cost_price, sale_price,
                   min_stock, max_stock, is_active, created_at, updated_at
            FROM products
            WHERE sku = ?
            """.trimIndent(),
            sku
        )
    }

    /**
     * Tìm sản phẩm theo barcode.
     *
     * @param barcode Mã barcode cần tìm.
     * @return Sản phẩm nếu tồn tại.
     */
    fun findByBarcode(barcode: String): Product? {
        return findOne(
            """
            SELECT product_id, sku, barcode, product_name, image_url,
                   category_id, supplier_id, unit, cost_price, sale_price,
                   min_stock, max_stock, is_active, created_at, updated_at
            FROM products
            WHERE barcode = ?
            """.trimIndent(),
            barcode
        )
    }

    /**
     * Kiểm tra SKU đã được sử dụng hay chưa.
     *
     * @param sku SKU cần kiểm tra.
     * @param excludeProductId Mã sản phẩm không tính khi cập nhật.
     * @return true nếu SKU đã tồn tại.
     */
    fun existsSku(
        sku: String,
        excludeProductId: Long? = null
    ): Boolean {
        return exists(
            """
            SELECT 1 FROM products
            WHERE sku = ?
            AND (? IS NULL OR product_id <> ?)
            LIMIT 1
            """.trimIndent(),
            sku,
            excludeProductId
        )
    }

    /**
     * Kiểm tra barcode đã được sử dụng hay chưa.
     *
     * @param barcode Barcode cần kiểm tra.
     * @param excludeProductId Mã sản phẩm không tính khi cập nhật.
     * @return true nếu barcode đã tồn tại.
     */
    fun existsBarcode(
        barcode: String,
        excludeProductId: Long? = null
    ): Boolean {
        return exists(
            """
            SELECT 1 FROM products
            WHERE barcode = ?
            AND (? IS NULL OR product_id <> ?)
            LIMIT 1
            """.trimIndent(),
            barcode,
            excludeProductId
        )
    }

    /**
     * Thêm sản phẩm mới vào cơ sở dữ liệu.
     *
     * @param product Thông tin sản phẩm cần thêm.
     * @return Mã sản phẩm vừa được tạo.
     */
    fun create(product: Product): Long {
        val sql = """
            INSERT INTO products (
                sku, barcode, product_name, image_url,
                category_id, supplier_id, unit,
                cost_price, sale_price, min_stock, max_stock,
                is_active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            ).use { statement ->

                bindProduct(statement, product)
                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    if (keys.next()) {
                        keys.getLong(1)
                    } else {
                        throw IllegalStateException(
                            "Không thể lấy mã sản phẩm vừa tạo."
                        )
                    }
                }
            }
        }
    }

    /**
     * Cập nhật thông tin sản phẩm.
     *
     * @param product Sản phẩm chứa thông tin cần cập nhật.
     * @return true nếu cập nhật thành công.
     */
    fun update(product: Product): Boolean {
        val sql = """
    UPDATE products
    SET sku = ?,
        barcode = ?,
        product_name = ?,
        image_url = ?,
        category_id = ?,
        supplier_id = ?,
        unit = ?,
        cost_price = ?,
        sale_price = ?,
        min_stock = ?,
        max_stock = ?,
        updated_at = CURRENT_TIMESTAMP
    WHERE product_id = ?
""".trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bindProduct(statement, product)
                statement.setLong(12, product.productId)

                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * Ngừng kinh doanh sản phẩm bằng cách chuyển trạng thái hoạt động.
     *
     * @param productId Mã sản phẩm.
     * @return true nếu cập nhật thành công.
     */
    fun deactivate(productId: Long): Boolean {
        val sql = """
            UPDATE products
            SET is_active = FALSE
            WHERE product_id = ?
              AND is_active = TRUE
        """.trimIndent()

        return executeUpdate(sql, productId)
    }

    /**
     * Kích hoạt lại sản phẩm đã ngừng kinh doanh.
     *
     * @param productId Mã sản phẩm.
     * @return true nếu cập nhật thành công.
     */
    fun reactivate(productId: Long): Boolean {
        val sql = """
            UPDATE products
            SET is_active = TRUE
            WHERE product_id = ?
              AND is_active = FALSE
        """.trimIndent()

        return executeUpdate(sql, productId)
    }

    /**
     * Lấy danh sách tồn kho của một sản phẩm tại các kho.
     *
     * @param productId Mã sản phẩm.
     * @return Danh sách thông tin tồn kho.
     */
    fun findInventories(
        productId: Long
    ): List<ProductInventoryResponse> {
        val sql = """
            SELECT
                i.inventory_id,
                i.warehouse_id,
                w.warehouse_name,
                i.batch_code,
                i.manufacture_date,
                i.expiry_date,
                i.stock_quantity,
                i.reserved_quantity,
                i.available_quantity,
                i.updated_at
            FROM inventory i
            INNER JOIN warehouses w
                ON i.warehouse_id = w.warehouse_id
            WHERE i.product_id = ?
            ORDER BY w.warehouse_name, i.expiry_date
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, productId)

                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(ProductInventoryMapper.toResponse(resultSet))
                        }
                    }
                }
            }
        }
    }

    /**
     * Tìm một sản phẩm bằng câu truy vấn và một tham số chuỗi.
     *
     * @param sql Câu truy vấn SQL.
     * @param value Giá trị điều kiện.
     * @return Sản phẩm nếu tìm thấy.
     */
    private fun findOne(
        sql: String,
        value: String
    ): Product? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, value)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) mapProduct(resultSet) else null
                }
            }
        }
    }

    /**
     * Kiểm tra dữ liệu có tồn tại theo câu truy vấn hay không.
     *
     * @param sql Câu truy vấn kiểm tra.
     * @param value Giá trị cần kiểm tra.
     * @param excludeProductId Mã sản phẩm cần loại trừ.
     * @return true nếu tìm thấy dữ liệu.
     */
    private fun exists(
        sql: String,
        value: String,
        excludeProductId: Long?
    ): Boolean {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, value)

                if (excludeProductId == null) {
                    statement.setNull(2, java.sql.Types.BIGINT)
                    statement.setNull(3, java.sql.Types.BIGINT)
                } else {
                    statement.setLong(2, excludeProductId)
                    statement.setLong(3, excludeProductId)
                }

                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                }
            }
        }
    }

    /**
     * Gán các tham số tìm kiếm và lọc vào PreparedStatement.
     *
     * @param statement Câu lệnh SQL đã chuẩn bị.
     * @param parameters Danh sách giá trị điều kiện.
     */
    private fun bindSearchParameters(
        statement: PreparedStatement,
        parameters: List<Any>
    ) {
        parameters.forEachIndexed { index, value ->
            val parameterIndex = index + 1

            when (value) {
                is String -> statement.setString(parameterIndex, value)
                is Long -> statement.setLong(parameterIndex, value)
                is Int -> statement.setInt(parameterIndex, value)
                is Boolean -> statement.setBoolean(parameterIndex, value)
                is BigDecimal -> statement.setBigDecimal(parameterIndex, value)
                else -> throw IllegalArgumentException(
                    "Kiểu dữ liệu tham số tìm kiếm không được hỗ trợ."
                )
            }
        }
    }

    /**
     * Thực hiện câu lệnh cập nhật với một mã sản phẩm.
     *
     * @param sql Câu lệnh SQL.
     * @param productId Mã sản phẩm.
     * @return true nếu có bản ghi được cập nhật.
     */
    private fun executeUpdate(
        sql: String,
        productId: Long
    ): Boolean {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, productId)
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * Gán dữ liệu sản phẩm vào PreparedStatement.
     *
     * @param statement Câu lệnh SQL đã chuẩn bị.
     * @param product Thông tin sản phẩm.
     */
    private fun bindProduct(
        statement: PreparedStatement,
        product: Product
    ) {
        statement.setString(1, product.sku)

        if (product.barcode == null) {
            statement.setNull(2, java.sql.Types.VARCHAR)
        } else {
            statement.setString(2, product.barcode)
        }

        statement.setString(3, product.productName)

        if (product.imageUrl == null) {
            statement.setNull(4, java.sql.Types.VARCHAR)
        } else {
            statement.setString(4, product.imageUrl)
        }

        if (product.categoryId == null) {
            statement.setNull(5, java.sql.Types.BIGINT)
        } else {
            statement.setLong(5, product.categoryId)
        }

        if (product.supplierId == null) {
            statement.setNull(6, java.sql.Types.BIGINT)
        } else {
            statement.setLong(6, product.supplierId)
        }

        statement.setString(7, product.unit)
        statement.setBigDecimal(8, product.costPrice)
        statement.setBigDecimal(9, product.salePrice)
        statement.setInt(10, product.minStock)
        statement.setInt(11, product.maxStock)
    }

    /**
     * Chuyển một dòng dữ liệu SQL thành Product.
     *
     * @param resultSet Dòng dữ liệu truy vấn.
     * @return Đối tượng Product.
     */
    private fun mapProduct(resultSet: ResultSet): Product {
        return Product(
            productId = resultSet.getLong("product_id"),
            sku = resultSet.getString("sku"),
            barcode = resultSet.getString("barcode"),
            productName = resultSet.getString("product_name"),
            imageUrl = resultSet.getString("image_url"),
            categoryId = resultSet.getLong("category_id")
                .let { if (resultSet.wasNull()) null else it },
            supplierId = resultSet.getLong("supplier_id")
                .let { if (resultSet.wasNull()) null else it },
            unit = resultSet.getString("unit"),
            costPrice = resultSet.getBigDecimal("cost_price")
                ?: BigDecimal.ZERO,
            salePrice = resultSet.getBigDecimal("sale_price")
                ?: BigDecimal.ZERO,
            minStock = resultSet.getInt("min_stock"),
            maxStock = resultSet.getInt("max_stock"),
            isActive = resultSet.getBoolean("is_active"),
            createdAt = resultSet.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = resultSet.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}
