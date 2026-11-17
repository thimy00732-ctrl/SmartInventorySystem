package com.sis.backend.security

import com.sis.backend.dto.ApiErrorResponse
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.sis.backend.service.AuthUserRepository
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpMethod

data class AuthenticatedUserContext(
    val userId: Int,
    val role: String,
    val mustChangePassword: Boolean
)

private val AuthenticatedUserKey = AttributeKey<AuthenticatedUserContext>("authenticated-user")

fun Application.installAccountAccessControl(
    repository: AuthUserRepository,
    jwtService: JwtService
) {
    intercept(ApplicationCallPipeline.Call) {
        val path = call.request.path()
        if (!path.startsWith("/api/") || path in PUBLIC_PATHS) {
            proceed()
            return@intercept
        }

        val token = call.request.headers["Authorization"]
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
            ?.trim()
        if (token.isNullOrBlank()) {
            call.respondAuthError(
                HttpStatusCode.Unauthorized,
                "AUTHENTICATION_REQUIRED",
                "Vui lòng đăng nhập để tiếp tục"
            )
            finish()
            return@intercept
        }

        val jwt: DecodedJWT? = try {
            jwtService.verifyToken(token)
        } catch (_: JWTVerificationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
        val userId = jwt?.subject?.toIntOrNull()
        val user = if (jwt == null || userId == null) null else {
            repository.findById(userId)?.takeIf {
                it.status == "ACTIVE" &&
                    it.role == jwt.getClaim("role").asString()
            }
        }
        if (user == null) {
            call.respondAuthError(
                HttpStatusCode.Unauthorized,
                "INVALID_ACCESS_TOKEN",
                "Token không hợp lệ hoặc tài khoản đã bị vô hiệu hóa"
            )
            finish()
            return@intercept
        }

        call.attributes.put(
            AuthenticatedUserKey,
            AuthenticatedUserContext(user.userId, user.role, user.mustChangePassword)
        )
        if (
            user.mustChangePassword &&
            path !in PASSWORD_CHANGE_ALLOWED_PATHS &&
            !(path == "/api/account/me" && call.request.httpMethod == HttpMethod.Get)
        ) {
            call.respondAuthError(
                HttpStatusCode.Forbidden,
                "PASSWORD_CHANGE_REQUIRED",
                "Bạn phải đổi mật khẩu trước khi sử dụng chức năng này"
            )
            finish()
            return@intercept
        }
        if (user.passwordChangedAt != null) {
            val issuedAt = jwt?.issuedAt?.time
            val changedAt = user.passwordChangedAt.atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            if (issuedAt != null && issuedAt < changedAt) {
                call.respondAuthError(
                    HttpStatusCode.Unauthorized,
                    "ACCESS_TOKEN_REVOKED",
                    "Phiên đăng nhập đã hết hiệu lực. Vui lòng đăng nhập lại."
                )
                finish()
                return@intercept
            }
        }
        proceed()
    }
}

fun ApplicationCall.authenticatedUser(): AuthenticatedUserContext? =
    attributes.getOrNull(AuthenticatedUserKey)

fun AuthenticatedUserContext?.isAdmin(): Boolean = this?.role == "ADMIN"

private suspend fun ApplicationCall.respondAuthError(
    status: HttpStatusCode,
    code: String,
    message: String
) {
    respond(status, ApiErrorResponse(message = message, code = code))
}

private val PUBLIC_PATHS = setOf("/api/auth/login", "/api/auth/refresh")
private val PASSWORD_CHANGE_ALLOWED_PATHS =
    setOf("/api/account/change-password", "/api/auth/logout")
