package com.sis.android.data.model

data class Account(
    val userId: Int,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val status: String,
    val mustChangePassword: Boolean
)

data class LoginRequest(val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: Account
)
data class ProfileUpdateRequest(val fullName: String, val phone: String?)
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)
data class CreateUserRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String
)
data class UpdateUserRequest(val fullName: String, val email: String, val phone: String?)
data class UpdateStatusRequest(val status: String)
data class UpdateRoleRequest(val role: String)
data class ApiMessage(val message: String)
