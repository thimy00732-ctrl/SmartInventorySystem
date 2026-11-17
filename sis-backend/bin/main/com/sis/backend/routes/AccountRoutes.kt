package com.sis.backend.routes

import com.sis.backend.dto.ChangePasswordRequest
import com.sis.backend.dto.ProfileUpdateRequest
import com.sis.backend.security.authenticatedUser
import com.sis.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.accountRoutes(authService: AuthService) {
    authenticate("auth-jwt") {
        route("/api/account") {
            get("/me") {
                call.respondAuthOperation {
                    val userId = authenticatedUser()?.userId
                        ?: throw com.sis.backend.exception.AuthException(
                            HttpStatusCode.Unauthorized,
                            "INVALID_ACCESS_TOKEN",
                            "Token không hợp lệ"
                        )
                    respond(HttpStatusCode.OK, authService.getAccount(userId))
                }
            }

            patch("/me") {
                call.respondAuthOperation {
                    val userId = authenticatedUser()?.userId
                        ?: throw com.sis.backend.exception.AuthException(
                            HttpStatusCode.Unauthorized,
                            "INVALID_ACCESS_TOKEN",
                            "Token không hợp lệ"
                        )
                    val request = receive<ProfileUpdateRequest>()
                    respond(HttpStatusCode.OK, authService.updateProfile(userId, request))
                }
            }

            post("/change-password") {
                call.respondAuthOperation {
                    val userId = authenticatedUser()?.userId
                        ?: throw com.sis.backend.exception.AuthException(
                            HttpStatusCode.Unauthorized,
                            "INVALID_ACCESS_TOKEN",
                            "Token không hợp lệ"
                        )
                    val request = receive<ChangePasswordRequest>()
                    authService.changePassword(userId, request)
                    respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.")
                    )
                }
            }
        }
    }
}
