package com.sis.backend.repository

import com.sis.backend.entity.Category
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import javax.sql.DataSource

/**
 * Repository thực hiện các thao tác truy vấn dữ liệu danh mục trong MySQL.
 *
 * Sử dụng JDBC để truy cập database thông qua DataSource được inject.
 * Danh mục được ngừng hoạt động thay vì xóa vật lý nhằm bảo toàn dữ liệu liên quan.
 *
 * @property dataSource DataSource dùng cho các truy vấn của repository.
 */
class CategoryRepository(
    private val dataSource: DataSource
) {

    /**
     * Chuyển một dòng dữ liệu từ ResultSet thành đối tượng Category.
     *
     * @param result ResultSet chứa dữ liệu danh mục.
     * @return Đối tượng Category tương ứng.
     */
    private fun mapRow(result: ResultSet): Category {
        return Category(
            categoryId = result.getLong("category_id"),
            categoryName = result.getString("category_name"),
            description = result.getString("description"),
            isActive = result.getBoolean("is_active"),
            createdAt = result.getTimestamp("created_at")
                ?.toLocalDateTime()
                ?.toString(),
            updatedAt = result.getTimestamp("updated_at")
                ?.toLocalDateTime()
                ?.toString()
        )
    }

    /**
     * Lấy danh sách danh mục, hỗ trợ tìm kiếm theo tên.
     *
     * @param keyword Từ khóa tìm kiếm; null hoặc rỗng để lấy tất cả.
     * @param activeOnly Chỉ lấy danh mục đang hoạt động nếu là true.
     * @return Danh sách danh mục phù hợp.
     */
    fun findAll(
        keyword: String? = null,
        activeOnly: Boolean = false
    ): List<Category> {
        val sql = """
            SELECT category_id, category_name, description,
                   is_active, created_at, updated_at
            FROM categories
            WHERE (? IS NULL OR ? = '' OR category_name LIKE ?)
              AND (? = FALSE OR is_active = TRUE)
            ORDER BY category_id DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                val searchKeyword = keyword?.trim()

                statement.setString(1, searchKeyword)
                statement.setString(2, searchKeyword)
                statement.setString(
                    3,
                    searchKeyword?.let { "%$it%" }
                )
                statement.setBoolean(4, activeOnly)

                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(mapRow(result))
                        }
                    }
                }
            }
        }
    }

    /**
     * Tìm danh mục theo ID.
     *
     * @param categoryId ID danh mục cần tìm.
     * @return Danh mục nếu tồn tại, ngược lại trả về null.
     */
    fun findById(categoryId: Long): Category? {
        val sql = """
            SELECT category_id, category_name, description,
                   is_active, created_at, updated_at
            FROM categories
            WHERE category_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, categoryId)

                statement.executeQuery().use { result ->
                    if (result.next()) mapRow(result) else null
                }
            }
        }
    }

    /**
     * Kiểm tra danh mục có tồn tại và đang hoạt động hay không.
     *
     * @param categoryId ID danh mục cần kiểm tra.
     * @return true nếu danh mục tồn tại và đang hoạt động.
     */
    fun existsActiveById(categoryId: Long): Boolean {
        val sql = """
        SELECT EXISTS (
            SELECT 1
            FROM categories
            WHERE category_id = ?
              AND is_active = TRUE
        )
    """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, categoryId)

                statement.executeQuery().use { result ->
                    result.next() && result.getBoolean(1)
                }
            }
        }
    }

    /**
     * Kiểm tra tên danh mục đã tồn tại hay chưa.
     *
     * So sánh không phân biệt chữ hoa, chữ thường và bỏ khoảng trắng
     * ở đầu, cuối tên.
     *
     * @param categoryName Tên danh mục cần kiểm tra.
     * @param excludeId ID danh mục bỏ qua khi cập nhật; null khi tạo mới.
     * @return true nếu tên danh mục đã tồn tại.
     */
    fun existsByName(
        categoryName: String,
        excludeId: Long? = null
    ): Boolean {
        val sql = """
            SELECT COUNT(*)
            FROM categories
            WHERE LOWER(TRIM(category_name)) = LOWER(TRIM(?))
              AND (? IS NULL OR category_id <> ?)
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, categoryName.trim())

                if (excludeId == null) {
                    statement.setNull(2, Types.BIGINT)
                    statement.setNull(3, Types.BIGINT)
                } else {
                    statement.setLong(2, excludeId)
                    statement.setLong(3, excludeId)
                }

                statement.executeQuery().use { result ->
                    result.next() && result.getInt(1) > 0
                }
            }
        }
    }

    /**
     * Kiểm tra danh mục có sản phẩm đang hoạt động hay không.
     *
     * @param categoryId ID danh mục cần kiểm tra.
     * @return true nếu tồn tại sản phẩm đang hoạt động thuộc danh mục.
     */
    fun hasActiveProducts(categoryId: Long): Boolean {
        val sql = """
            SELECT EXISTS (
                SELECT 1
                FROM products
                WHERE category_id = ?
                  AND is_active = TRUE
            )
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, categoryId)

                statement.executeQuery().use { result ->
                    result.next() && result.getBoolean(1)
                }
            }
        }
    }

    /**
     * Thêm danh mục mới vào database.
     *
     * @param category Thông tin danh mục cần thêm.
     * @return Danh mục vừa tạo, bao gồm ID do database sinh ra.
     * @throws IllegalStateException Nếu không lấy được ID hoặc không tìm thấy
     * danh mục sau khi thêm.
     */
    fun create(category: Category): Category {
        val sql = """
            INSERT INTO categories (
                category_name,
                description,
                is_active
            )
            VALUES (?, ?, TRUE)
        """.trimIndent()

        val generatedId = dataSource.connection.use { connection ->
            connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, category.categoryName.trim())
                statement.setString(2, category.description)

                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    if (keys.next()) {
                        keys.getLong(1)
                    } else {
                        throw IllegalStateException(
                            "Không lấy được ID danh mục vừa tạo."
                        )
                    }
                }
            }
        }

        return findById(generatedId)
            ?: throw IllegalStateException(
                "Không tìm thấy danh mục vừa tạo."
            )
    }

    /**
     * Cập nhật tên và mô tả của danh mục.
     *
     * @param categoryId ID danh mục cần cập nhật.
     * @param categoryName Tên danh mục mới.
     * @param description Mô tả mới của danh mục.
     * @return Danh mục sau cập nhật hoặc null nếu không tìm thấy.
     */
    fun update(
        categoryId: Long,
        categoryName: String,
        description: String?
    ): Category? {
        val sql = """
            UPDATE categories
            SET category_name = ?,
                description = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE category_id = ?
        """.trimIndent()

        val affectedRows = dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, categoryName.trim())
                statement.setString(2, description)
                statement.setLong(3, categoryId)

                statement.executeUpdate()
            }
        }

        return if (affectedRows == 0) {
            null
        } else {
            findById(categoryId)
        }
    }

    /**
     * Ngừng hoạt động danh mục mà không xóa vật lý dữ liệu.
     *
     * @param categoryId ID danh mục cần ngừng hoạt động.
     * @return true nếu có bản ghi được cập nhật; false nếu không tìm thấy
     * hoặc danh mục đã ngừng hoạt động.
     */
    fun deactivate(categoryId: Long): Boolean {
        val sql = """
            UPDATE categories
            SET is_active = FALSE,
                updated_at = CURRENT_TIMESTAMP
            WHERE category_id = ?
              AND is_active = TRUE
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, categoryId)
                statement.executeUpdate() > 0
            }
        }
    }
}
