package com.sis.backend.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sis.backend.dto.CreateStaffRequest
import com.sis.backend.dto.UpdateUserRequest
import com.sis.backend.exception.AuthException
import com.sis.backend.security.JwtService
import com.sis.backend.security.installAccountAccessControl
import com.sis.backend.service.AuthService
import com.sis.backend.service.AuthUser
import com.sis.backend.service.AuthUserRepository
import com.sis.backend.service.TemporaryPasswordNotifier
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminUserRoutesTest {
    private val secret = "route-tests-use-a-secret-longer-than-thirty-two-bytes"

    @Test
    fun `duplicate email returns conflict and own email remains valid`() = testApplication {
        val repository = FakeRepository()
        val jwtService = JwtService(
            secret = secret,
            issuer = "route-test-issuer",
            audience = "route-test-audience"
        )
        val authService = AuthService(
            repository = repository,
            jwtService = jwtService,
            verifyPassword = { raw, hash -> hash == "hash:$raw" },
            hashPassword = { "hash:$it" },
            notifier = TemporaryPasswordNotifier { _, _, _, _ -> }
        )
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(Authentication) {
                jwt("auth-jwt") {
                    verifier(
                        JWT.require(Algorithm.HMAC256(secret))
                            .withIssuer("route-test-issuer")
                            .withAudience("route-test-audience")
                            .build()
                    )
                    validate { JWTPrincipal(it.payload) }
                }
            }
            installAccountAccessControl(repository, jwtService)
            routing { adminUserRoutes(authService) }
        }

        val token = jwtService.generateAccessToken(42, "admin@example.com", "ADMIN")
        val conflict = client.put("/api/admin/users/42") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"fullName":"Other User","email":"other@example.com","phone":null}""")
        }
        assertEquals(HttpStatusCode.Conflict, conflict.status)
        assertTrue(conflict.bodyAsText().contains("Email đã tồn tại trong hệ thống"))

        val sameEmail = client.put("/api/admin/users/42") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"fullName":"Admin Updated","email":"admin@example.com","phone":null}""")
        }
        assertEquals(HttpStatusCode.OK, sameEmail.status)
        assertEquals("admin@example.com", repository.findById(42)?.email)
    }

    private class FakeRepository : AuthUserRepository {
        private val users = mutableMapOf(
            42 to AuthUser(
                userId = 42,
                fullName = "Admin",
                email = "admin@example.com",
                phone = null,
                passwordHash = "hash:password",
                role = "ADMIN",
                status = "ACTIVE"
            ),
            43 to AuthUser(
                userId = 43,
                fullName = "Other User",
                email = "other@example.com",
                phone = null,
                passwordHash = "hash:password",
                role = "WAREHOUSE_STAFF",
                status = "ACTIVE"
            )
        )

        override fun findByEmail(email: String): AuthUser? =
            users.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

        override fun findById(userId: Int): AuthUser? = users[userId]
        override fun updateLastLogin(userId: Int) = Unit
        override fun listUsers(role: String?, status: String?): List<AuthUser> =
            users.values.filter { (role == null || role == it.role) && (status == null || status == it.status) }

        override fun createUser(
            request: CreateStaffRequest,
            passwordHash: String,
            expiresAt: java.time.LocalDateTime
        ): Int = 0

        override fun activateUser(userId: Int): Boolean = false
        override fun deleteInactiveUser(userId: Int) = Unit
        override fun updateStatus(userId: Int, status: String): Boolean = false
        override fun updateRole(userId: Int, role: String): Boolean = false
        override fun updateProfile(userId: Int, fullName: String, phone: String?): Boolean = false

        override fun updateUser(userId: Int, request: UpdateUserRequest): Boolean {
            val emailOwner = users.values.firstOrNull {
                it.email.equals(request.email, ignoreCase = true) && it.userId != userId
            }
            if (emailOwner != null) {
                throw AuthException(
                    HttpStatusCode.Conflict,
                    "EMAIL_ALREADY_EXISTS",
                    "Email đã tồn tại trong hệ thống"
                )
            }
            val current = users[userId] ?: return false
            users[userId] = current.copy(
                fullName = request.fullName,
                email = request.email,
                phone = request.phone
            )
            return true
        }

        override fun updatePassword(userId: Int, passwordHash: String): Boolean = false
        override fun storeRefreshToken(userId: Int, tokenHash: String, expiresAt: java.time.LocalDateTime) = Unit
        override fun rotateRefreshToken(
            oldTokenHash: String,
            newTokenHash: String,
            expiresAt: java.time.LocalDateTime
        ): AuthUser? = null

        override fun revokeRefreshToken(userId: Int, tokenHash: String) = Unit
    }
}
