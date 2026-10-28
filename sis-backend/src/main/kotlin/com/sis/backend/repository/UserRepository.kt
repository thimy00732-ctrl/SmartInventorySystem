package com.sis.backend.repository

import com.sis.backend.dto.CreateStaffRequest
import com.sis.backend.service.AuthUser
import com.sis.backend.service.AuthUserRepository
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

class UserRepository(
    private val dataSource: DataSource
) : AuthUserRepository {

    private val columns = """
        user_id, full_name, email, phone, password_hash,
        role, status, must_change_password
    """.trimIndent()

    override fun findByEmail(email: String): AuthUser? {
        val sql = """
            SELECT $columns FROM users
            WHERE LOWER(email) = LOWER(?)
            LIMIT 1
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, email.trim())
                statement.executeQuery().use { result ->
                    if (result.next()) result.toAuthUser() else null
                }
            }
        }
    }

    override fun findById(userId: Int): AuthUser? {
        val sql = """
            SELECT $columns FROM users
            WHERE user_id = ?
            LIMIT 1
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toAuthUser() else null
                }
            }
        }
    }

    override fun updateLastLogin(userId: Int) {
        val sql = """
            UPDATE users SET last_login_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use {
                it.setInt(1, userId)
                it.executeUpdate()
            }
        }
    }

    fun listStaff(): List<AuthUser> {
        val sql = """
            SELECT $columns FROM users
            WHERE role = 'WAREHOUSE_STAFF'
            ORDER BY user_id DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) add(result.toAuthUser())
                    }
                }
            }
        }
    }

    fun createStaff(
        request: CreateStaffRequest,
        passwordHash: String
    ): Int {
        require(request.fullName.isNotBlank()) {
            "Họ tên không được để trống"
        }
        require(request.email.isNotBlank()) {
            "Email không được để trống"
        }
        require(request.role == "WAREHOUSE_STAFF") {
            "Chỉ được tạo tài khoản nhân viên kho"
        }

        val sql = """
            INSERT INTO users (
                full_name, email, phone, password_hash, role, status,
                must_change_password, password_changed_at
            )
            VALUES (?, ?, ?, ?, 'WAREHOUSE_STAFF', 'ACTIVE', TRUE, NULL)
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, request.fullName.trim())
                statement.setString(2, request.email.trim().lowercase())
                statement.setString(
                    3,
                    request.phone?.trim()?.takeIf { it.isNotEmpty() }
                )
                statement.setString(4, passwordHash)

                check(statement.executeUpdate() == 1) {
                    "Không thể tạo tài khoản nhân viên"
                }

                statement.generatedKeys.use { keys ->
                    check(keys.next()) {
                        "Không lấy được ID nhân viên"
                    }
                    keys.getInt(1)
                }
            }
        }
    }

    fun updateStatus(userId: Int, status: String): Boolean {
        require(status in setOf("ACTIVE", "INACTIVE")) {
            "Trạng thái phải là ACTIVE hoặc INACTIVE"
        }

        val sql = """
            UPDATE users SET status = ?
            WHERE user_id = ? AND role = 'WAREHOUSE_STAFF'
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, status)
                statement.setInt(2, userId)
                statement.executeUpdate() > 0
            }
        }
    }

    fun updateRole(userId: Int, role: String): Boolean {
        require(role in setOf("ADMIN", "WAREHOUSE_STAFF")) {
            "Role không hợp lệ"
        }

        val sql = """
            UPDATE users SET role = ? WHERE user_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, role)
                statement.setInt(2, userId)
                statement.executeUpdate() > 0
            }
        }
    }

    private fun ResultSet.toAuthUser() = AuthUser(
        userId = getInt("user_id"),
        fullName = getString("full_name"),
        email = getString("email"),
        phone = getString("phone"),
        passwordHash = getString("password_hash"),
        role = getString("role"),
        status = getString("status"),
        mustChangePassword = getBoolean("must_change_password")
    )
}