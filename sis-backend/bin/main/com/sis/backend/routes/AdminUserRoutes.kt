package com.sis.backend.routes

import com.sis.backend.dto.CreateStaffRequest
import com.sis.backend.dto.UpdateRoleRequest
import com.sis.backend.dto.UpdateStatusRequest
import com.sis.backend.dto.UpdateUserRequest
import com.sis.backend.security.authenticatedUser
import com.sis.backend.security.isAdmin
import com.sis.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.adminUserRoutes(authService: AuthService) {
    authenticate("auth-jwt") {
        route("/api/admin/users") {
            get {
                call.respondAuthOperation {
                    requireAdmin()
                    val role = request.queryParameters["role"]
                    val status = request.queryParameters["status"]
                    respond(HttpStatusCode.OK, authService.listUsers(role, status))
                }
            }

            post {
                call.respondAuthOperation {
                    requireAdmin()
                    val request = receive<CreateStaffRequest>()
                    respond(HttpStatusCode.Created, authService.createUser(request))
                }
            }

            put("/{id}") {
                val rawId = call.parameters["id"]
                call.respondAuthOperation {
                    requireAdmin()
                    val userId = parseUserId(rawId)
                    val request = receive<UpdateUserRequest>()
                    respond(HttpStatusCode.OK, authService.updateUser(userId, request))
                }
            }

            patch("/{id}/status") {
                val rawId = call.parameters["id"]
                call.respondAuthOperation {
                    requireAdmin()
                    val userId = parseUserId(rawId)
                    val request = receive<UpdateStatusRequest>()
                    authService.updateStatus(userId, request.status)
                    respond(HttpStatusCode.OK, mapOf("message" to "Cập nhật trạng thái thành công"))
                }
            }

            patch("/{id}/role") {
                val rawId = call.parameters["id"]
                call.respondAuthOperation {
                    requireAdmin()
                    val userId = parseUserId(rawId)
                    val request = receive<UpdateRoleRequest>()
                    authService.updateRole(userId, request.role)
                    respond(HttpStatusCode.OK, mapOf("message" to "Cập nhật vai trò thành công"))
                }
            }
        }
    }
}

private suspend fun ApplicationCall.requireAdmin() {
    if (!authenticatedUser().isAdmin()) {
        throw com.sis.backend.exception.AuthException(
            HttpStatusCode.Forbidden,
            "FORBIDDEN",
            "Bạn không có quyền thực hiện thao tác này"
        )
    }
}

private fun parseUserId(rawId: String?): Int =
    rawId?.toIntOrNull()?.takeIf { it > 0 }
        ?: throw com.sis.backend.exception.AuthException(
            HttpStatusCode.BadRequest,
            "INVALID_USER_ID",
            "Mã tài khoản không hợp lệ"
        )
