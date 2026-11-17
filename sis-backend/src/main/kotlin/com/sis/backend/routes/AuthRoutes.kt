package com.sis.backend.routes

import com.sis.backend.dto.LoginRequest
import com.sis.backend.dto.LogoutResponse
import com.sis.backend.dto.RefreshRequest
import com.sis.backend.security.authenticatedUser
import com.sis.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/login") {
            call.respondAuthOperation {
                val request = receive<LoginRequest>()
                respond(HttpStatusCode.OK, authService.login(request))
            }
        }

        post("/refresh") {
            call.respondAuthOperation {
                val request = receive<RefreshRequest>()
                respond(HttpStatusCode.OK, authService.refresh(request.refreshToken))
            }
        }

        authenticate("auth-jwt") {
            post("/logout") {
                call.respondAuthOperation {
                    val userId = authenticatedUser()?.userId
                        ?: throw com.sis.backend.exception.AuthException(
                            HttpStatusCode.Unauthorized,
                            "INVALID_ACCESS_TOKEN",
                            "Token không hợp lệ"
                        )
                    val request = receive<RefreshRequest>()
                    authService.logout(userId, request.refreshToken)
                    respond(HttpStatusCode.OK, LogoutResponse("Đăng xuất thành công"))
                }
            }
        }
    }
}
