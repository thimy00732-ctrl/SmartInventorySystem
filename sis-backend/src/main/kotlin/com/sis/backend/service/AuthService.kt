package com.sis.backend.service

import com.sis.backend.dto.AccountResponse
import com.sis.backend.dto.LoginRequest
import com.sis.backend.dto.LoginResponse
import com.sis.backend.security.JwtService

data class AuthUser(
    val userId: Int,
    val fullName: String,
    val email: String,
    val phone: String?,
    val passwordHash: String,
    val role: String,
    val status: String,
    val mustChangePassword: Boolean = false
)

interface AuthUserRepository {
    fun findByEmail(email: String): AuthUser?
    fun findById(userId: Int): AuthUser?
    fun updateLastLogin(userId: Int)
}

class AuthService(
    private val repository: AuthUserRepository,
    private val jwtService: JwtService,
    private val verifyPassword: (String, String) -> Boolean
) {
    fun login(request: LoginRequest): LoginResponse {
        val email = request.email.trim().lowercase()

        require(email.isNotBlank() && request.password.isNotBlank()) {
            "Email và mật khẩu không được để trống"
        }

        val user = repository.findByEmail(email)
            ?: throw IllegalArgumentException("Email hoặc mật khẩu không đúng")

        require(user.status == "ACTIVE") {
            "Tài khoản đã bị vô hiệu hóa"
        }

        if (!verifyPassword(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Email hoặc mật khẩu không đúng")
        }

        require(user.role in setOf("ADMIN", "WAREHOUSE_STAFF")) {
            "Role tài khoản không hợp lệ"
        }

        repository.updateLastLogin(user.userId)

        return LoginResponse(
            accessToken = jwtService.generateAccessToken(
                userId = user.userId,
                email = user.email,
                role = user.role
            ),
            expiresIn = jwtService.getExpiresInSeconds(),
            user = user.toAccountResponse()
        )
    }

    fun getAccount(userId: Int): AccountResponse {
        val user = repository.findById(userId)
            ?: throw IllegalArgumentException("Không tìm thấy tài khoản")

        require(user.status == "ACTIVE") {
            "Tài khoản đã bị vô hiệu hóa"
        }

        return user.toAccountResponse()
    }

    private fun AuthUser.toAccountResponse() = AccountResponse(
        userId = userId,
        fullName = fullName,
        email = email,
        phone = phone,
        role = role,
        status = status,
        mustChangePassword = mustChangePassword
    )
}