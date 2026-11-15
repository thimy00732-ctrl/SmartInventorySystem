package com.sis.backend.entity

import java.time.LocalDateTime

/**
 * Entity đại diện cho người dùng trong hệ thống
 * Tương ứng với bảng users trong cơ sở dữ liệu
 */
data class User(
    val user_id: Int? = null,
    val email: String,
    val password_hash: String,
    val full_name: String? = null,  // Họ tên người dùng
    val phone: String? = null,      // Số điện thoại
    val role: String, // "ADMIN" hoặc "WAREHOUSE_STAFF"
    val status: String = "ACTIVE", // "ACTIVE" hoặc "INACTIVE"
    val last_login_at: LocalDateTime? = null,
    val created_at: LocalDateTime? = null,
    val updated_at: LocalDateTime? = null,
    val must_change_password: Boolean = false,
    val temp_password_expires_at: LocalDateTime? = null,
    val password_changed_at: LocalDateTime? = null
)

/**
 * Entity đại diện cho refresh token
 * Tương ứng với bảng refresh_tokens trong cơ sở dữ liệu
 */
data class RefreshToken(
    val token_id: Int? = null,
    val user_id: Int,
    val refresh_token: String,
    val expires_at: LocalDateTime,
    val revoked_at: LocalDateTime? = null,
    val created_at: LocalDateTime? = null
)
