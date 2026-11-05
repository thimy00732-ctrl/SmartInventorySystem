package com.sis.android.data.model

/**
 * Thông tin người dùng đã đăng nhập, được lưu từ LoginResponse.
 * Roles: "ADMIN" | "WAREHOUSE_STAFF"
 */
data class User(
    val userId: Int,
    val email: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
