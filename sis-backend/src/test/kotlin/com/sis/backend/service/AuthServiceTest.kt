package com.sis.backend.service

import com.sis.backend.dto.ChangePasswordRequest
import com.sis.backend.dto.CreateStaffRequest
import com.sis.backend.dto.LoginRequest
import com.sis.backend.dto.ProfileUpdateRequest
import com.sis.backend.dto.UpdateUserRequest
import com.sis.backend.exception.AuthException
import com.sis.backend.security.JwtService
import io.ktor.http.HttpStatusCode
import java.security.MessageDigest
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthServiceTest {
    private val repository = FakeAuthUserRepository()
    private val sentTemporaryPasswords = mutableListOf<String>()
    private val service = AuthService(
        repository = repository,
        jwtService = JwtService(secret = "test-secret-that-is-at-least-32-bytes-long"),
        verifyPassword = { raw, hash -> hash == "hash:$raw" },
        hashPassword = { "hash:$it" },
        notifier = TemporaryPasswordNotifier { _, _, password, _ ->
            sentTemporaryPasswords += password
        }
    )

    @Test
    fun `login returns and stores refresh token`() {
        val result = service.login(LoginRequest(" USER@example.com ", "correct-password"))

        assertEquals(42, result.user.userId)
        assertTrue(result.accessToken.isNotBlank())
        assertTrue(result.refreshToken.isNotBlank())
        assertEquals(1, repository.lastLoginUpdates)
        assertEquals(hash(result.refreshToken), repository.activeRefreshTokenHash)
    }

    @Test
    fun `refresh rotation rejects reuse of previous token`() {
        val first = service.login(LoginRequest("user@example.com", "correct-password"))

        val rotated = service.refresh(first.refreshToken)

        assertNotEquals(first.refreshToken, rotated.refreshToken)
        assertFailsWith<AuthException> {
            service.refresh(first.refreshToken)
        }
    }

    @Test
    fun `change password requires matching confirmation before persistence`() {
        assertFailsWith<AuthException> {
            service.changePassword(
                42,
                ChangePasswordRequest(
                    currentPassword = "correct-password",
                    newPassword = "new-password",
                    confirmNewPassword = "different-password"
                )
            )
        }

        assertEquals(0, repository.passwordUpdates)
    }

    @Test
    fun `profile update does not expose or update email`() {
        val updated = service.updateProfile(42, ProfileUpdateRequest("New Name", "0900000000"))

        assertEquals("New Name", updated.fullName)
        assertEquals("user@example.com", updated.email)
        assertEquals("0900000000", updated.phone)
    }

    @Test
    fun `admin update allows retaining own email`() {
        repository.existingEmails["user@example.com"] = 42

        val updated = service.updateUser(
            42,
            UpdateUserRequest("Updated Name", "user@example.com", null)
        )

        assertEquals("user@example.com", updated.email)
    }

    @Test
    fun `new account is activated only after temporary password email is sent`() {
        val created = service.createUser(
            CreateStaffRequest(
                fullName = "New Staff",
                email = "staff@example.com",
                phone = null
            )
        )

        assertEquals("staff@example.com", created.email)
        assertEquals(1, sentTemporaryPasswords.size)
        assertTrue(repository.createdUserWasActivated)
    }

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private class FakeAuthUserRepository : AuthUserRepository {
        private var user = AuthUser(
            userId = 42,
            fullName = "Test User",
            email = "user@example.com",
            phone = null,
            passwordHash = "hash:correct-password",
            role = "ADMIN",
            status = "ACTIVE"
        )
        var lastLoginUpdates = 0
        var passwordUpdates = 0
        var activeRefreshTokenHash: String? = null
        var createdUserWasActivated = false
        val existingEmails = mutableMapOf<String, Int>()

        override fun findByEmail(email: String): AuthUser? =
            user.takeIf { it.email.equals(email, ignoreCase = true) }

        override fun findById(userId: Int): AuthUser? = user.takeIf { it.userId == userId }

        override fun updateLastLogin(userId: Int) {
            lastLoginUpdates++
        }

        override fun listUsers(role: String?, status: String?): List<AuthUser> =
            listOf(user).filter { (role == null || role == it.role) && (status == null || status == it.status) }

        override fun createUser(
            request: CreateStaffRequest,
            passwordHash: String,
            expiresAt: LocalDateTime
        ): Int {
            user = AuthUser(
                userId = 84,
                fullName = request.fullName,
                email = request.email,
                phone = request.phone,
                passwordHash = passwordHash,
                role = request.role,
                status = "INACTIVE",
                mustChangePassword = true,
                temporaryPasswordExpiresAt = expiresAt
            )
            return user.userId
        }

        override fun activateUser(userId: Int): Boolean {
            createdUserWasActivated = true
            user = user.copy(status = "ACTIVE")
            return true
        }

        override fun deleteInactiveUser(userId: Int) = Unit

        override fun updateStatus(userId: Int, status: String): Boolean {
            user = user.copy(status = status)
            return true
        }

        override fun updateRole(userId: Int, role: String): Boolean {
            user = user.copy(role = role)
            return true
        }

        override fun updateProfile(userId: Int, fullName: String, phone: String?): Boolean {
            user = user.copy(fullName = fullName, phone = phone)
            return true
        }

        override fun updateUser(userId: Int, request: UpdateUserRequest): Boolean {
            val emailOwner = existingEmails[request.email]
            if (emailOwner != null && emailOwner != userId) {
                throw AuthException(
                    HttpStatusCode.Conflict,
                    "EMAIL_ALREADY_EXISTS",
                    "Email đã tồn tại trong hệ thống"
                )
            }
            user = user.copy(fullName = request.fullName, email = request.email, phone = request.phone)
            return true
        }

        override fun updatePassword(userId: Int, passwordHash: String): Boolean {
            passwordUpdates++
            user = user.copy(passwordHash = passwordHash, mustChangePassword = false)
            return true
        }

        override fun storeRefreshToken(userId: Int, tokenHash: String, expiresAt: LocalDateTime) {
            activeRefreshTokenHash = tokenHash
        }

        override fun rotateRefreshToken(
            oldTokenHash: String,
            newTokenHash: String,
            expiresAt: LocalDateTime
        ): AuthUser? {
            if (activeRefreshTokenHash != oldTokenHash) return null
            activeRefreshTokenHash = newTokenHash
            return user
        }

        override fun revokeRefreshToken(userId: Int, tokenHash: String) {
            if (activeRefreshTokenHash == tokenHash) activeRefreshTokenHash = null
        }
    }
}
