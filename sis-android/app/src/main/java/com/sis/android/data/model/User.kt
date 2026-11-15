package com.sis.android.data.model

/**
 * Thông tin người dùng đã đăng nhập
 * Roles: "ADMIN" | "WAREHOUSE_STAFF"
 */
data class User(
    val user_id: Int,           // Giữ tên cũ cho tương thích với backend
    val email: String,
    val fullName: String?,      // Optional
    val role: String,
    val is_active: Boolean = true,
    val phone: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null
)
