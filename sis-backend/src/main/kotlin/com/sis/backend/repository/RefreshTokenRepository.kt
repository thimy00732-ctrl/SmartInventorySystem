package com.sis.backend.repository

import com.sis.backend.entity.RefreshToken
import javax.sql.DataSource
import java.time.LocalDateTime
import java.sql.Timestamp

/**
 * RefreshTokenRepository: Quản lý refresh tokens trong database
 * - Lưu, xóa, tìm kiếm refresh tokens
 * - Dùng để làm mới access token mà không cần đăng nhập lại (UC07)
 */
class RefreshTokenRepository(private val dataSource: DataSource) {

    /**
     * UC01/UC07: Lưu refresh token mới vào database
     * Khi người dùng đăng nhập, tạo 1 refresh token hợp lệ 7 ngày
     * @param refreshToken - RefreshToken object
     * @return RefreshToken object với token_id được tạo tự động
     */
    fun save(refreshToken: RefreshToken): RefreshToken {
        val query = """
            INSERT INTO refresh_tokens (user_id, refresh_token, expires_at, created_at)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setInt(1, refreshToken.user_id)
                statement.setString(2, refreshToken.refresh_token)
                statement.setTimestamp(3, Timestamp.valueOf(refreshToken.expires_at))
                statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))

                statement.executeUpdate()

                // Lấy token_id được tạo tự động
                statement.generatedKeys.use { keys ->
                    if (keys.next()) {
                        refreshToken.copy(
                            token_id = keys.getInt(1),
                            created_at = LocalDateTime.now()
                        )
                    } else {
                        throw RuntimeException("Không thể lưu refresh token")
                    }
                }
            }
        }
    }

    /**
     * UC07: Tìm refresh token theo token string
     * Dùng để xác minh refresh token có hợp lệ không
     * @param token - refresh token string
     * @return RefreshToken object nếu tìm thấy và chưa hết hạn, null nếu không
     */
    fun findByToken(token: String): RefreshToken? {
        val query = """
            SELECT token_id, user_id, refresh_token, expires_at, revoked_at, created_at
            FROM refresh_tokens 
            WHERE refresh_token = ? 
            AND expires_at > ? 
            AND revoked_at IS NULL
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, token)
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()))
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        mapResultSetToRefreshToken(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * UC02: Thu hồi refresh token (đặt revoked_at)
     * Khi người dùng đăng xuất, hủy bỏ refresh token để tránh sử dụng lại
     * @param tokenId - id của refresh token
     * @return true nếu hủy thành công
     */
    fun revoke(tokenId: Int): Boolean {
        val query = """
            UPDATE refresh_tokens 
            SET revoked_at = ? 
            WHERE token_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()))
                statement.setInt(2, tokenId)
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * UC02: Thu hồi tất cả refresh tokens của một user (khi vô hiệu hóa tài khoản)
     * @param userId - id của người dùng
     * @return số lượng tokens đã bị hủy
     */
    fun revokeAllByUserId(userId: Int): Int {
        val query = """
            UPDATE refresh_tokens 
            SET revoked_at = ? 
            WHERE user_id = ? AND revoked_at IS NULL
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()))
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
        }
    }

    /**
     * UC07: Tìm refresh token theo user_id (dùng khi cần xem lịch sử token)
     * @param userId - id của người dùng
     * @return List<RefreshToken> của tất cả tokens của user
     */
    fun findByUserId(userId: Int): List<RefreshToken> {
        val query = """
            SELECT token_id, user_id, refresh_token, expires_at, revoked_at, created_at
            FROM refresh_tokens 
            WHERE user_id = ?
            ORDER BY created_at DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setInt(1, userId)
                statement.executeQuery().use { resultSet ->
                    val tokens = mutableListOf<RefreshToken>()
                    while (resultSet.next()) {
                        tokens.add(mapResultSetToRefreshToken(resultSet))
                    }
                    tokens
                }
            }
        }
    }

    /**
     * Xóa refresh tokens hết hạn (cần chạy định kỳ để dọn dẹp database)
     * @param beforeDate - xóa các tokens hết hạn trước ngày này
     * @return số lượng tokens đã xóa
     */
    fun deleteExpired(beforeDate: LocalDateTime): Int {
        val query = """
            DELETE FROM refresh_tokens 
            WHERE expires_at < ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setTimestamp(1, Timestamp.valueOf(beforeDate))
                statement.executeUpdate()
            }
        }
    }

    /**
     * Helper function: Chuyển đổi ResultSet thành RefreshToken object
     * @param resultSet - kết quả truy vấn từ database
     * @return RefreshToken object
     */
    private fun mapResultSetToRefreshToken(resultSet: java.sql.ResultSet): RefreshToken {
        return RefreshToken(
            token_id = resultSet.getInt("token_id"),
            user_id = resultSet.getInt("user_id"),
            refresh_token = resultSet.getString("refresh_token"),
            expires_at = resultSet.getTimestamp("expires_at").toLocalDateTime(),
            revoked_at = resultSet.getTimestamp("revoked_at")?.toLocalDateTime(),
            created_at = resultSet.getTimestamp("created_at")?.toLocalDateTime()
        )
    }
}
