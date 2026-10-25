package com.sis.backend.repository

import com.sis.backend.entity.Category
import com.sis.backend.mapper.mapCategory
import java.sql.Connection
import javax.sql.DataSource

/**
 * Lớp kho lưu trữ (repository) quản lý các thao tác cơ sở dữ liệu cho thực thể danh mục.
 *
 * @property dataSource Nguồn dữ liệu kết nối cơ sở dữ liệu ([DataSource]).
 */
class CategoryRepository(
    private val dataSource: DataSource
) {
    /**
     * Lấy danh sách toàn bộ các danh mục được sắp xếp theo ID giảm dần.
     *
     * @return Danh sách các đối tượng [Category].
     */
    fun findAll(): List<Category> {
        val sql = """
            SELECT category_id, category_name, description,
                   status, created_at, updated_at
            FROM categories
            ORDER BY category_id DESC
        """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val categories = mutableListOf<Category>()
                    while (rs.next()) {
                        categories.add(mapCategory(rs))
                    }
                    return categories
                }
            }
        }
    }

    /**
     * Tìm kiếm một danh mục dựa trên định danh (ID).
     *
     * @param id Định danh duy nhất của danh mục cần tìm.
     * @return Đối tượng [Category] nếu tìm thấy, ngược lại trả về `null`.
     */
    fun findById(id: Long): Category? {
        val sql = """
            SELECT category_id, category_name, description,
                   status, created_at, updated_at
            FROM categories
            WHERE category_id = ?
        """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) mapCategory(rs) else null
                }
            }
        }
    }

    /**
     * Kiểm tra xem tên danh mục đã tồn tại trong hệ thống hay chưa, hỗ trợ loại trừ theo ID khi cập nhật.
     *
     * @param name Tên danh mục cần kiểm tra.
     * @param excludeId Định danh danh mục cần loại trừ khỏi kết quả kiểm tra (tùy chọn).
     * @return `true` nếu tên danh mục đã tồn tại, ngược lại trả về `false`.
     */
    fun existsByName(
        name: String,
        excludeId: Long? = null
    ): Boolean {
        val sql = if (excludeId == null) {
            "SELECT 1 FROM categories WHERE LOWER(category_name) = LOWER(?) LIMIT 1"
        } else {
            """
            SELECT 1 FROM categories
            WHERE LOWER(category_name) = LOWER(?)
              AND category_id <> ?
            LIMIT 1
            """.trimIndent()
        }

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, name)

                if (excludeId != null) {
                    stmt.setLong(2, excludeId)
                }

                stmt.executeQuery().use { rs ->
                    return rs.next()
                }
            }
        }
    }

    /**
     * Thêm mới một bản ghi danh mục vào cơ sở dữ liệu.
     *
     * @param name Tên của danh mục.
     * @param description Mô tả chi tiết về danh mục.
     * @param status Trạng thái hoạt động của danh mục.
     * @return Đối tượng [Category] vừa được tạo thành công.
     */
    fun create(
        name: String,
        description: String?,
        status: String
    ): Category {
        val sql = """
            INSERT INTO categories (category_name, description, status)
            VALUES (?, ?, ?)
        """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(
                sql,
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { stmt ->
                stmt.setString(1, name)
                stmt.setString(2, description)
                stmt.setString(3, status)
                stmt.executeUpdate()

                stmt.generatedKeys.use { keys ->
                    check(keys.next()) {
                        "Không lấy được ID danh mục vừa tạo"
                    }
                    return findById(keys.getLong(1))
                        ?: error("Không tìm thấy danh mục vừa tạo")
                }
            }
        }
    }

    /**
     * Cập nhật thông tin của một danh mục hiện có trong cơ sở dữ liệu.
     *
     * @param id Định danh duy nhất của danh mục cần cập nhật.
     * @param name Tên mới của danh mục.
     * @param description Mô tả mới của danh mục.
     * @param status Trạng thái mới của danh mục.
     * @return Đối tượng [Category] sau khi được cập nhật, hoặc `null` nếu không tìm thấy.
     */
    fun update(
        id: Long,
        name: String,
        description: String?,
        status: String
    ): Category? {
        val sql = """
            UPDATE categories
            SET category_name = ?, description = ?, status = ?
            WHERE category_id = ?
        """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, name)
                stmt.setString(2, description)
                stmt.setString(3, status)
                stmt.setLong(4, id)
                stmt.executeUpdate()
            }
        }

        return findById(id)
    }

    /**
     * Kiểm tra xem danh mục có đang được sử dụng bởi sản phẩm nào hay không.
     *
     * @param id Định danh duy nhất của danh mục cần kiểm tra.
     * @return `true` nếu có sản phẩm thuộc danh mục này, ngược lại trả về `false`.
     */
    fun hasProducts(id: Long): Boolean {
        val sql = """
            SELECT 1 FROM products
            WHERE category_id = ?
            LIMIT 1
        """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeQuery().use { rs ->
                    return rs.next()
                }
            }
        }
    }

    /**
     * Xóa một danh mục khỏi cơ sở dữ liệu dựa trên định danh (ID).
     *
     * @param id Định danh duy nhất của danh mục cần xóa.
     * @return `true` nếu xóa thành công, ngược lại trả về `false`.
     */

    fun delete(id: Long): Boolean {
        val sql = "DELETE FROM categories WHERE category_id = ?"

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                return stmt.executeUpdate() > 0
            }
        }
    }
}