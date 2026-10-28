package com.sis.backend.routes

import com.sis.backend.dto.ApiErrorResponse
import com.sis.backend.dto.LoginRequest
import com.sis.backend.dto.LogoutResponse
import com.sis.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                call.respond(HttpStatusCode.OK, authService.login(request))
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrorResponse(message = e.message ?: "Đăng nhập thất bại")
                )
            } catch (_: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiErrorResponse(message = "Lỗi xử lý đăng nhập")
                )
            }
        }

        post("/logout") {
            call.respond(
                HttpStatusCode.OK,
                LogoutResponse(
                    "Đăng xuất thành công. Hãy xóa access token phía client."
                )
            )
        }
    }
}