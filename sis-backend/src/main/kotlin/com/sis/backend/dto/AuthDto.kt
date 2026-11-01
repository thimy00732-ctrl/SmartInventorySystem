package com.sis.backend.dto

import kotlinx.serialization.Serializable

// UC01: Đăng nhập
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val user_id: Int,
    val email: String,
    val role: String,
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long
)

// UC02: Đăng xuất
@Serializable
data class LogoutRequest(
    val refresh_token: String
)

// UC03: Quản lý người dùng (tạo/cập nhật)
@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    val role: String // "ADMIN" hoặc "WAREHOUSE_STAFF"
)

@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val role: String? = null,
    val status: String? = null // "ACTIVE" hoặc "INACTIVE"
)

@Serializable
data class UserResponse(
    val user_id: Int,
    val email: String,
    val role: String,
    val status: String,
    val last_login_at: String?,
    val created_at: String?
)

// UC04: Kiểm soát truy cập (JWT validation)
@Serializable
data class TokenValidationResponse(
    val valid: Boolean,
    val user_id: Int?,
    val email: String?,
    val role: String?,
    val message: String?
)

// UC05: Quản lý hồ sơ người dùng
@Serializable
data class UpdateProfileRequest(
    val email: String? = null
)

@Serializable
data class ProfileResponse(
    val user_id: Int,
    val email: String,
    val role: String,
    val status: String,
    val last_login_at: String?,
    val created_at: String?
)

// UC06: Thay đổi mật khẩu
@Serializable
data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

@Serializable
data class ChangePasswordResponse(
    val message: String,
    val success: Boolean
)

// UC07: Làm mới token
@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)

@Serializable
data class RefreshTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long
)

// Generic response wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)
