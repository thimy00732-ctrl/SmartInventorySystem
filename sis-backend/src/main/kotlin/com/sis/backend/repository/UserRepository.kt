package com.sis.backend.repository

import com.sis.backend.entity.User
import javax.sql.DataSource
import java.time.LocalDateTime
import java.sql.Timestamp

/**
 * UserRepository: Quản lý tất cả các thao tác database liên quan đến User
 * - Lưu trữ, cập nhật, tìm kiếm người dùng
 * - Sử dụng JDBC và HikariCP connection pool
 */
class UserRepository(private val dataSource: DataSource) {

    /**
     * UC01: Tìm người dùng theo email (dùng khi đăng nhập)
     * @param email - email của người dùng
     * @return User object nếu tìm thấy, null nếu không
     */
    fun findByEmail(email: String): User? {
        val query = """
            SELECT user_id, email, password_hash, role, status, 
                   last_login_at, created_at, updated_at, 
                   must_change_password, temp_password_expires_at, password_changed_at
            FROM users 
            WHERE email = ? AND status = 'ACTIVE'
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, email)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        mapResultSetToUser(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * UC03: Tìm người dùng theo user_id (dùng khi cập nhật hoặc xem chi tiết)
     * @param userId - id của người dùng
     * @return User object nếu tìm thấy, null nếu không
     */
    fun findById(userId: Int): User? {
        val query = """
            SELECT user_id, email, password_hash, role, status, 
                   last_login_at, created_at, updated_at,
                   must_change_password, temp_password_expires_at, password_changed_at
            FROM users 
            WHERE user_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setInt(1, userId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        mapResultSetToUser(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * UC01: Lưu người dùng mới vào database (tạo tài khoản)
     * @param user - User object chứa thông tin người dùng
     * @return User object với user_id được tạo tự động bởi database
     */
    fun save(user: User): User {
        val query = """
            INSERT INTO users (email, password_hash, role, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, user.email)
                statement.setString(2, user.password_hash)
                statement.setString(3, user.role)
                statement.setString(4, user.status)
                statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
                statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()))

                statement.executeUpdate()

                // Lấy user_id được tạo tự động
                statement.generatedKeys.use { keys ->
                    if (keys.next()) {
                        user.copy(user_id = keys.getInt(1), created_at = LocalDateTime.now())
                    } else {
                        throw RuntimeException("Không thể tạo người dùng mới")
                    }
                }
            }
        }
    }

    /**
     * UC01: Cập nhật last_login_at khi người dùng đăng nhập thành công
     * @param userId - id của người dùng
     */
    fun updateLastLogin(userId: Int) {
        val query = """
            UPDATE users 
            SET last_login_at = ? 
            WHERE user_id = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()))
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
        }
    }

    /**
     * UC03: Cập nhật thông tin người dùng (email, role, status)
     * @param userId - id của người dùng
     * @param email - email mới (có thể null nếu không cập nhật)
     * @param role - role mới (có thể null nếu không cập nhật)
     * @param status - status mới (có thể null nếu không cập nhật)
     * @return true nếu cập nhật thành công, false nếu không
     */
    fun update(userId: Int, email: String?, role: String?, status: String?): Boolean {
        // Xây dựng query động tùy thuộc vào những trường nào cần cập nhật
        val updates = mutableListOf<String>()
        val params = mutableListOf<Any>()

        if (email != null) {
            updates.add("email = ?")
            params.add(email)
        }
        if (role != null) {
            updates.add("role = ?")
            params.add(role)
        }
        if (status != null) {
            updates.add("status = ?")
            params.add(status)
        }

        if (updates.isEmpty()) {
            return false // Không có gì để cập nhật
        }

        updates.add("updated_at = ?")
        params.add(Timestamp.valueOf(LocalDateTime.now()))
        params.add(userId)

        val query = "UPDATE users SET ${updates.joinToString(", ")} WHERE user_id = ?"

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                params.forEachIndexed { index, value ->
                    when (value) {
                        is String -> statement.setString(index + 1, value)
                        is Timestamp -> statement.setTimestamp(index + 1, value)
                        is Int -> statement.setInt(index + 1, value)
                    }
                }
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * UC06: Cập nhật mật khẩu mới khi người dùng thay đổi mật khẩu
     * @param userId - id của người dùng
     * @param newPasswordHash - hash của mật khẩu mới
     * @return true nếu cập nhật thành công
     */
    fun updatePassword(userId: Int, newPasswordHash: String): Boolean {
        val query = """
            UPDATE users 
            SET password_hash = ?, password_changed_at = ?, updated_at = ?
            WHERE user_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newPasswordHash)
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()))
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
                statement.setInt(4, userId)
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * UC03/UC02: Soft delete - vô hiệu hóa người dùng (không xóa khỏi database)
     * Đặt status = 'INACTIVE' để bảo toàn dữ liệu kiểm toán
     * @param userId - id của người dùng
     * @return true nếu vô hiệu hóa thành công
     */
    fun deactivate(userId: Int): Boolean {
        val query = """
            UPDATE users 
            SET status = 'INACTIVE', updated_at = ?
            WHERE user_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()))
                statement.setInt(2, userId)
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * UC03: Kích hoạt lại người dùng (đặt status = 'ACTIVE')
     * @param userId - id của người dùng
     * @return true nếu kích hoạt thành công
     */
    fun activate(userId: Int): Boolean {
        val query = """
            UPDATE users 
            SET status = 'ACTIVE', updated_at = ?
            WHERE user_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()))
                statement.setInt(2, userId)
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * Lấy tất cả người dùng (dùng cho danh sách quản lý)
     * @return List<User> của tất cả người dùng
     */
    fun findAll(): List<User> {
        val query = """
            SELECT user_id, email, password_hash, role, status, 
                   last_login_at, created_at, updated_at,
                   must_change_password, temp_password_expires_at, password_changed_at
            FROM users 
            ORDER BY created_at DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(query).use { resultSet ->
                    val users = mutableListOf<User>()
                    while (resultSet.next()) {
                        users.add(mapResultSetToUser(resultSet))
                    }
                    users
                }
            }
        }
    }

    /**
     * Helper function: Chuyển đổi ResultSet thành User object
     * @param resultSet - kết quả truy vấn từ database
     * @return User object
     */
    private fun mapResultSetToUser(resultSet: java.sql.ResultSet): User {
        return User(
            user_id = resultSet.getInt("user_id"),
            email = resultSet.getString("email"),
            password_hash = resultSet.getString("password_hash"),
            role = resultSet.getString("role"),
            status = resultSet.getString("status"),
            last_login_at = resultSet.getTimestamp("last_login_at")?.toLocalDateTime(),
            created_at = resultSet.getTimestamp("created_at")?.toLocalDateTime(),
            updated_at = resultSet.getTimestamp("updated_at")?.toLocalDateTime(),
            must_change_password = resultSet.getBoolean("must_change_password"),
            temp_password_expires_at = resultSet.getTimestamp("temp_password_expires_at")?.toLocalDateTime(),
            password_changed_at = resultSet.getTimestamp("password_changed_at")?.toLocalDateTime()
        )
    }
}
