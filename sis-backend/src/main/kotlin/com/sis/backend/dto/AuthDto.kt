package com.sis.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: AccountResponse
)

@Serializable
data class AccountResponse(
    val userId: Int,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val status: String,
    val mustChangePassword: Boolean = false
)

@Serializable
data class ApiErrorResponse(
    val success: Boolean = false,
    val message: String
)

@Serializable
data class CreateStaffRequest(
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val password: String,
    val role: String = "WAREHOUSE_STAFF"
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
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class LogoutResponse(
    val message: String
)