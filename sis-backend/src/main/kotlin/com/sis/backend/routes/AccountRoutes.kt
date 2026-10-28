package com.sis.backend.routes

import com.sis.backend.dto.ApiErrorResponse
import com.sis.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.accountRoutes(authService: AuthService) {
    authenticate("auth-jwt") {
        route("/api/account") {
            get("/me") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toIntOrNull()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiErrorResponse(message = "Token không hợp lệ")
                    )
                    return@get
                }

                try {
                    call.respond(
                        HttpStatusCode.OK,
                        authService.getAccount(userId)
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiErrorResponse(
                            message = e.message ?: "Không thể lấy tài khoản"
                        )
                    )
                } catch (_: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiErrorResponse(message = "Lỗi lấy thông tin tài khoản")
                    )
                }
            }
        }
    }
}