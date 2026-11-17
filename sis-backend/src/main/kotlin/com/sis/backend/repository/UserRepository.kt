package com.sis.backend.repository

import com.sis.backend.dto.CreateStaffRequest
import com.sis.backend.dto.UpdateUserRequest
import com.sis.backend.exception.AuthException
import com.sis.backend.service.AuthUser
import com.sis.backend.service.AuthUserRepository
import io.ktor.http.HttpStatusCode
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

class UserRepository(
    private val dataSource: DataSource
) : AuthUserRepository {

    private val columns = """
        user_id, full_name, email, phone, password_hash,
        role, status, must_change_password,
        temp_password_expires_at, password_changed_at
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

    override fun listUsers(role: String?, status: String?): List<AuthUser> {
        require(role == null || role in VALID_ROLES) { "Role không hợp lệ" }
        require(status == null || status in VALID_STATUSES) {
            "Trạng thái phải là ACTIVE hoặc INACTIVE"
        }

        val conditions = mutableListOf<String>()
        if (role != null) conditions += "role = ?"
        if (status != null) conditions += "status = ?"
        val whereClause = if (conditions.isEmpty()) "" else "WHERE ${conditions.joinToString(" AND ")}"
        val sql = "SELECT $columns FROM users $whereClause ORDER BY user_id DESC"

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                var index = 1
                role?.let { statement.setString(index++, it) }
                status?.let { statement.setString(index, it) }
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) add(result.toAuthUser())
                    }
                }
            }
        }
    }

    override fun createUser(
        request: CreateStaffRequest,
        passwordHash: String,
        expiresAt: LocalDateTime
    ): Int {
        val sql = """
            INSERT INTO users (
                full_name, email, phone, password_hash, role, status,
                must_change_password, temp_password_expires_at,
                password_changed_at
            )
            VALUES (?, ?, ?, ?, ?, 'INACTIVE', TRUE, ?, NULL)
        """.trimIndent()

        return try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setString(1, request.fullName.trim())
                    statement.setString(2, request.email.trim().lowercase())
                    statement.setString(3, request.phone?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setString(4, passwordHash)
                    statement.setString(5, request.role)
                    statement.setTimestamp(6, Timestamp.valueOf(expiresAt))
                    check(statement.executeUpdate() == 1) {
                        "Không thể tạo tài khoản"
                    }
                    statement.generatedKeys.use { keys ->
                        check(keys.next()) { "Không lấy được ID tài khoản" }
                        keys.getInt(1)
                    }
                }
            }
        } catch (e: SQLException) {
            if (e.errorCode == MYSQL_DUPLICATE_ENTRY) {
                throw AuthException(
                    HttpStatusCode.Conflict,
                    "EMAIL_ALREADY_EXISTS",
                    "Email đã tồn tại trong hệ thống"
                )
            }
            throw e
        }
    }

    override fun activateUser(userId: Int): Boolean =
        updateSingleUser("UPDATE users SET status = 'ACTIVE' WHERE user_id = ? AND status = 'INACTIVE'", userId)

    override fun deleteInactiveUser(userId: Int) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "DELETE FROM users WHERE user_id = ? AND status = 'INACTIVE'"
            ).use { statement ->
                statement.setInt(1, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun updateStatus(userId: Int, status: String): Boolean {
        require(status in VALID_STATUSES) {
            "Trạng thái phải là ACTIVE hoặc INACTIVE"
        }
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val changed = connection.prepareStatement(
                    "UPDATE users SET status = ? WHERE user_id = ?"
                ).use { statement ->
                    statement.setString(1, status)
                    statement.setInt(2, userId)
                    statement.executeUpdate() > 0
                }
                if (changed && status == "INACTIVE") {
                    revokeTokens(connection, userId)
                }
                connection.commit()
                changed
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun updateRole(userId: Int, role: String): Boolean {
        require(role in VALID_ROLES) { "Role không hợp lệ" }
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val changed = connection.prepareStatement(
                    "UPDATE users SET role = ? WHERE user_id = ?"
                ).use { statement ->
                    statement.setString(1, role)
                    statement.setInt(2, userId)
                    statement.executeUpdate() > 0
                }
                if (changed) revokeTokens(connection, userId)
                connection.commit()
                changed
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun updateProfile(userId: Int, fullName: String, phone: String?): Boolean {
        val sql = "UPDATE users SET full_name = ?, phone = ? WHERE user_id = ?"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, fullName.trim())
                statement.setString(2, phone?.trim()?.takeIf(String::isNotEmpty))
                statement.setInt(3, userId)
                statement.executeUpdate() > 0
            }
        }
    }

    override fun updateUser(userId: Int, request: UpdateUserRequest): Boolean {
        val sql = """
            UPDATE users SET full_name = ?, email = ?, phone = ?
            WHERE user_id = ?
        """.trimIndent()
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val emailInUse = connection.prepareStatement(
                    """
                        SELECT user_id FROM users
                        WHERE LOWER(email) = LOWER(?) AND user_id <> ?
                        LIMIT 1
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, request.email.trim())
                    statement.setInt(2, userId)
                    statement.executeQuery().use { result -> result.next() }
                }
                if (emailInUse) {
                    throw AuthException(
                        HttpStatusCode.Conflict,
                        "EMAIL_ALREADY_EXISTS",
                        "Email đã tồn tại trong hệ thống"
                    )
                }
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, request.fullName.trim())
                    statement.setString(2, request.email.trim().lowercase())
                    statement.setString(3, request.phone?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setInt(4, userId)
                    val changed = statement.executeUpdate() > 0
                    connection.commit()
                    changed
                }
            } catch (e: SQLException) {
                connection.rollback()
                if (e.errorCode == MYSQL_DUPLICATE_ENTRY) {
                    throw AuthException(
                        HttpStatusCode.Conflict,
                        "EMAIL_ALREADY_EXISTS",
                        "Email đã tồn tại trong hệ thống"
                    )
                }
                throw e
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun updatePassword(userId: Int, passwordHash: String): Boolean {
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val changed = connection.prepareStatement(
                    """
                        UPDATE users
                        SET password_hash = ?, must_change_password = FALSE,
                            temp_password_expires_at = NULL,
                            password_changed_at = CURRENT_TIMESTAMP
                        WHERE user_id = ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, passwordHash)
                    statement.setInt(2, userId)
                    statement.executeUpdate() > 0
                }
                if (changed) revokeTokens(connection, userId)
                connection.commit()
                changed
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun storeRefreshToken(
        userId: Int,
        tokenHash: String,
        expiresAt: LocalDateTime
    ) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                    INSERT INTO refresh_tokens (user_id, refresh_token, expires_at, revoked_at)
                    VALUES (?, ?, ?, NULL)
                """.trimIndent()
            ).use { statement ->
                statement.setInt(1, userId)
                statement.setString(2, tokenHash)
                statement.setTimestamp(3, Timestamp.valueOf(expiresAt))
                statement.executeUpdate()
            }
        }
    }

    override fun rotateRefreshToken(
        oldTokenHash: String,
        newTokenHash: String,
        expiresAt: LocalDateTime
    ): AuthUser? {
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.prepareStatement(
                    """
                        SELECT ${columns.split(",").joinToString(", ") { "u.${it.trim()}" }}
                        FROM refresh_tokens rt
                        JOIN users u ON u.user_id = rt.user_id
                        WHERE rt.refresh_token = ?
                          AND rt.revoked_at IS NULL
                          AND rt.expires_at > CURRENT_TIMESTAMP
                          AND u.status = 'ACTIVE'
                          AND (
                              u.must_change_password = FALSE
                              OR u.temp_password_expires_at IS NULL
                              OR u.temp_password_expires_at > CURRENT_TIMESTAMP
                          )
                        FOR UPDATE
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, oldTokenHash)
                    statement.executeQuery().use { result ->
                        if (result.next()) result.toAuthUser() else null
                    }
                }
                if (user == null) {
                    connection.rollback()
                    return@use null
                }

                val revoked = connection.prepareStatement(
                    """
                        UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP
                        WHERE refresh_token = ? AND revoked_at IS NULL
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, oldTokenHash)
                    statement.executeUpdate()
                }
                if (revoked != 1) {
                    connection.rollback()
                    return@use null
                }

                connection.prepareStatement(
                    """
                        INSERT INTO refresh_tokens (user_id, refresh_token, expires_at, revoked_at)
                        VALUES (?, ?, ?, NULL)
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, user.userId)
                    statement.setString(2, newTokenHash)
                    statement.setTimestamp(3, Timestamp.valueOf(expiresAt))
                    statement.executeUpdate()
                }
                connection.commit()
                user
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun revokeRefreshToken(userId: Int, tokenHash: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                    UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP
                    WHERE user_id = ? AND refresh_token = ? AND revoked_at IS NULL
                """.trimIndent()
            ).use { statement ->
                statement.setInt(1, userId)
                statement.setString(2, tokenHash)
                statement.executeUpdate()
            }
        }
    }

    private fun updateSingleUser(sql: String, userId: Int): Boolean =
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.executeUpdate() > 0
            }
        }

    private fun revokeTokens(connection: java.sql.Connection, userId: Int) {
        connection.prepareStatement(
            """
                UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND revoked_at IS NULL
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, userId)
            statement.executeUpdate()
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
        mustChangePassword = getBoolean("must_change_password"),
        temporaryPasswordExpiresAt = getTimestamp("temp_password_expires_at")?.toLocalDateTime(),
        passwordChangedAt = getTimestamp("password_changed_at")?.toLocalDateTime()
    )

    companion object {
        private const val MYSQL_DUPLICATE_ENTRY = 1062
        private val VALID_ROLES = setOf("ADMIN", "WAREHOUSE_STAFF")
        private val VALID_STATUSES = setOf("ACTIVE", "INACTIVE")
    }
}
