package com.sis.backend.service

import com.sis.backend.dto.AccountResponse
import com.sis.backend.dto.ChangePasswordRequest
import com.sis.backend.dto.CreateStaffRequest
import com.sis.backend.dto.LoginRequest
import com.sis.backend.dto.LoginResponse
import com.sis.backend.dto.ProfileUpdateRequest
import com.sis.backend.dto.UpdateUserRequest
import com.sis.backend.exception.AuthException
import com.sis.backend.security.JwtService
import io.ktor.http.HttpStatusCode
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.LocalDateTime
import java.util.Base64

data class AuthUser(
    val userId: Int,
    val fullName: String,
    val email: String,
    val phone: String?,
    val passwordHash: String,
    val role: String,
    val status: String,
    val mustChangePassword: Boolean = false,
    val temporaryPasswordExpiresAt: LocalDateTime? = null,
    val passwordChangedAt: LocalDateTime? = null
)

interface AuthUserRepository {
    fun findByEmail(email: String): AuthUser?
    fun findById(userId: Int): AuthUser?
    fun updateLastLogin(userId: Int)
    fun listUsers(role: String?, status: String?): List<AuthUser>
    fun createUser(request: CreateStaffRequest, passwordHash: String, expiresAt: LocalDateTime): Int
    fun activateUser(userId: Int): Boolean
    fun deleteInactiveUser(userId: Int)
    fun updateStatus(userId: Int, status: String): Boolean
    fun updateRole(userId: Int, role: String): Boolean
    fun updateProfile(userId: Int, fullName: String, phone: String?): Boolean
    fun updateUser(userId: Int, request: UpdateUserRequest): Boolean
    fun updatePassword(userId: Int, passwordHash: String): Boolean
    fun storeRefreshToken(userId: Int, tokenHash: String, expiresAt: LocalDateTime)
    fun rotateRefreshToken(oldTokenHash: String, newTokenHash: String, expiresAt: LocalDateTime): AuthUser?
    fun revokeRefreshToken(userId: Int, tokenHash: String)
}

class AuthService(
    private val repository: AuthUserRepository,
    private val jwtService: JwtService,
    private val verifyPassword: (String, String) -> Boolean,
    private val hashPassword: (String) -> String,
    private val notifier: TemporaryPasswordNotifier,
    private val temporaryPasswordHours: Long = 24,
    private val refreshTokenDays: Long = 30,
    private val clock: Clock = Clock.systemUTC()
) {
    fun login(request: LoginRequest): LoginResponse {
        val email = request.email.trim().lowercase()
        if (email.isBlank() || request.password.isBlank()) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_CREDENTIALS",
                "Email và mật khẩu không được để trống"
            )
        }

        val user = repository.findByEmail(email)
            ?: throw invalidCredentials()
        if (user.status != "ACTIVE") {
            throw AuthException(
                HttpStatusCode.Forbidden,
                "ACCOUNT_INACTIVE",
                "Tài khoản đã bị vô hiệu hóa"
            )
        }
        if (!verifyPassword(request.password, user.passwordHash)) {
            throw invalidCredentials()
        }
        if (user.role !in VALID_ROLES) {
            throw AuthException(
                HttpStatusCode.Forbidden,
                "INVALID_ROLE",
                "Vai trò tài khoản không hợp lệ"
            )
        }
        if (user.mustChangePassword && isTemporaryPasswordExpired(user)) {
            throw AuthException(
                HttpStatusCode.Forbidden,
                "TEMPORARY_PASSWORD_EXPIRED",
                "Mật khẩu tạm thời đã hết hạn. Vui lòng liên hệ quản trị viên."
            )
        }

        repository.updateLastLogin(user.userId)
        return issueTokenPair(user)
    }

    fun refresh(refreshToken: String): LoginResponse {
        if (refreshToken.isBlank()) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "REFRESH_TOKEN_REQUIRED",
                "Refresh token không được để trống"
            )
        }
        val newRefreshToken = createOpaqueToken()
        val now = LocalDateTime.now(clock)
        val user = repository.rotateRefreshToken(
            oldTokenHash = hashToken(refreshToken),
            newTokenHash = hashToken(newRefreshToken),
            expiresAt = now.plusDays(refreshTokenDays)
        ) ?: throw AuthException(
            HttpStatusCode.Unauthorized,
            "INVALID_REFRESH_TOKEN",
            "Refresh token không hợp lệ, đã hết hạn hoặc đã bị thu hồi"
        )

        return LoginResponse(
            accessToken = accessTokenFor(user),
            refreshToken = newRefreshToken,
            expiresIn = jwtService.getExpiresInSeconds(),
            user = user.toAccountResponse()
        )
    }

    fun logout(userId: Int, refreshToken: String) {
        if (refreshToken.isBlank()) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "REFRESH_TOKEN_REQUIRED",
                "Refresh token không được để trống"
            )
        }
        repository.revokeRefreshToken(userId, hashToken(refreshToken))
    }

    fun getAccount(userId: Int): AccountResponse = requireActiveUser(userId).toAccountResponse()

    fun updateProfile(userId: Int, request: ProfileUpdateRequest): AccountResponse {
        validateFullName(request.fullName)
        validatePhone(request.phone)
        if (!repository.updateProfile(userId, request.fullName, request.phone)) {
            throw accountNotFound()
        }
        return getAccount(userId)
    }

    fun changePassword(userId: Int, request: ChangePasswordRequest) {
        val user = requireActiveUser(userId)
        if (!verifyPassword(request.currentPassword, user.passwordHash)) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "CURRENT_PASSWORD_INVALID",
                "Mật khẩu hiện tại không đúng"
            )
        }
        if (request.newPassword != request.confirmNewPassword) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "PASSWORD_CONFIRMATION_MISMATCH",
                "Mật khẩu mới và xác nhận mật khẩu không khớp"
            )
        }
        validatePassword(request.newPassword)
        if (!repository.updatePassword(userId, hashPassword(request.newPassword))) {
            throw accountNotFound()
        }
    }

    fun listUsers(role: String?, status: String?): List<AccountResponse> {
        if (role != null && role !in VALID_ROLES) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_ROLE",
                "Vai trò không hợp lệ"
            )
        }
        if (status != null && status !in VALID_STATUSES) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_STATUS",
                "Trạng thái phải là ACTIVE hoặc INACTIVE"
            )
        }
        return repository.listUsers(role, status).map { it.toAccountResponse() }
    }

    fun createUser(request: CreateStaffRequest): AccountResponse {
        validateFullName(request.fullName)
        validateEmail(request.email)
        validatePhone(request.phone)
        if (request.role !in VALID_ROLES) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_ROLE",
                "Vai trò không hợp lệ"
            )
        }

        val temporaryPassword = generateTemporaryPassword()
        val expiresAt = LocalDateTime.now(clock).plusHours(temporaryPasswordHours)
        val userId = repository.createUser(
            request = request,
            passwordHash = hashPassword(temporaryPassword),
            expiresAt = expiresAt
        )
        try {
            notifier.send(request.email.trim(), request.fullName.trim(), temporaryPassword, expiresAt)
            if (!repository.activateUser(userId)) {
                throw IllegalStateException("Không thể kích hoạt tài khoản mới")
            }
        } catch (e: Exception) {
            repository.deleteInactiveUser(userId)
            if (e is AuthException) throw e
            throw AuthException(
                HttpStatusCode.BadGateway,
                "TEMPORARY_PASSWORD_EMAIL_FAILED",
                "Không thể gửi mật khẩu tạm thời qua email"
            )
        }

        return repository.findById(userId)?.toAccountResponse()
            ?: throw IllegalStateException("Không thể tải tài khoản vừa tạo")
    }

    fun updateUser(userId: Int, request: UpdateUserRequest): AccountResponse {
        validateFullName(request.fullName)
        validateEmail(request.email)
        validatePhone(request.phone)
        if (!repository.updateUser(userId, request)) throw accountNotFound()
        return repository.findById(userId)?.toAccountResponse() ?: throw accountNotFound()
    }

    fun updateStatus(userId: Int, status: String) {
        if (status !in VALID_STATUSES) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_STATUS",
                "Trạng thái phải là ACTIVE hoặc INACTIVE"
            )
        }
        if (!repository.updateStatus(userId, status)) throw accountNotFound()
    }

    fun updateRole(userId: Int, role: String) {
        if (role !in VALID_ROLES) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_ROLE",
                "Vai trò không hợp lệ"
            )
        }
        if (!repository.updateRole(userId, role)) throw accountNotFound()
    }

    private fun issueTokenPair(user: AuthUser): LoginResponse {
        val refreshToken = createOpaqueToken()
        repository.storeRefreshToken(
            userId = user.userId,
            tokenHash = hashToken(refreshToken),
            expiresAt = LocalDateTime.now(clock).plusDays(refreshTokenDays)
        )
        return LoginResponse(
            accessToken = accessTokenFor(user),
            refreshToken = refreshToken,
            expiresIn = jwtService.getExpiresInSeconds(),
            user = user.toAccountResponse()
        )
    }

    private fun accessTokenFor(user: AuthUser) = jwtService.generateAccessToken(
        userId = user.userId,
        email = user.email,
        role = user.role
    )

    private fun requireActiveUser(userId: Int): AuthUser {
        val user = repository.findById(userId) ?: throw accountNotFound()
        if (user.status != "ACTIVE") {
            throw AuthException(
                HttpStatusCode.Forbidden,
                "ACCOUNT_INACTIVE",
                "Tài khoản đã bị vô hiệu hóa"
            )
        }
        return user
    }

    private fun isTemporaryPasswordExpired(user: AuthUser): Boolean =
        user.temporaryPasswordExpiresAt?.isBefore(LocalDateTime.now(clock)) == true

    private fun generateTemporaryPassword(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%&*"
        return buildString(16) {
            repeat(16) { append(alphabet[random.nextInt(alphabet.length)]) }
        }
    }

    private fun createOpaqueToken(): String {
        val bytes = ByteArray(48)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun validateFullName(fullName: String) {
        if (fullName.isBlank() || fullName.trim().length > 150) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_FULL_NAME",
                "Họ tên không được để trống và tối đa 150 ký tự"
            )
        }
    }

    private fun validateEmail(email: String) {
        val normalized = email.trim()
        if (normalized.length > 254 || !EMAIL_PATTERN.matches(normalized)) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_EMAIL",
                "Email không hợp lệ"
            )
        }
    }

    private fun validatePhone(phone: String?) {
        if (phone != null && phone.trim().length > 30) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_PHONE",
                "Số điện thoại tối đa 30 ký tự"
            )
        }
    }

    private fun validatePassword(password: String) {
        val byteLength = password.toByteArray(Charsets.UTF_8).size
        if (byteLength !in 8..72) {
            throw AuthException(
                HttpStatusCode.BadRequest,
                "INVALID_NEW_PASSWORD",
                "Mật khẩu mới phải có ít nhất 8 ký tự và tối đa 72 byte"
            )
        }
    }

    private fun invalidCredentials() = AuthException(
        HttpStatusCode.Unauthorized,
        "INVALID_CREDENTIALS",
        "Email hoặc mật khẩu không đúng"
    )

    private fun accountNotFound() = AuthException(
        HttpStatusCode.NotFound,
        "ACCOUNT_NOT_FOUND",
        "Không tìm thấy tài khoản"
    )

    private fun AuthUser.toAccountResponse() = AccountResponse(
        userId = userId,
        fullName = fullName,
        email = email,
        phone = phone,
        role = role,
        status = status,
        mustChangePassword = mustChangePassword
    )

    companion object {
        private val random = SecureRandom()
        private val VALID_ROLES = setOf("ADMIN", "WAREHOUSE_STAFF")
        private val VALID_STATUSES = setOf("ACTIVE", "INACTIVE")
        private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
