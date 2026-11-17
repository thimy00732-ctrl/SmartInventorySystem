package com.sis.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: AccountResponse
)

@Serializable
data class AccountResponse(
    val userId: Int,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val status: String,
    val mustChangePassword: Boolean
)

@Serializable
data class ProfileUpdateRequest(
    val fullName: String,
    val phone: String?
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)

@Serializable
data class CreateStaffRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String = "WAREHOUSE_STAFF"
)

@Serializable
data class UpdateUserRequest(
    val fullName: String,
    val email: String,
    val phone: String?
)

@Serializable
data class UpdateStatusRequest(
    val status: String
)

@Serializable
data class UpdateRoleRequest(
    val role: String
)

@Serializable
data class LogoutResponse(
    val message: String
)

@Serializable
data class ApiErrorResponse(
    val message: String,
    val code: String = "REQUEST_FAILED"
)
