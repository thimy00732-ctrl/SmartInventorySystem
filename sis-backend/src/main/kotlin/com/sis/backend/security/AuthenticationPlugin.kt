package com.sis.backend.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.response.*

/**
 * AuthenticationPlugin: Cấu hình JWT authentication cho Ktor
 * 
 * Theo tài liệu phân quyền UC04, UC07:
 * - Kiểm tra token hợp lệ
 * - Đưa user_id, email, role vào principal
 * - Trả về 401 nếu token không hợp lệ hoặc hết hạn
 */

data class JwtPrincipal(
    val userId: Int,
    val email: String,
    val role: String
) : Principal

fun Application.configureAuthentication() {
    // Phải match với JwtService config
    val jwtSecret = System.getenv("JWT_SECRET_KEY") 
        ?: "smart-inventory-secret-key-do-not-use-in-production"
    val jwtIssuer = "smart-inventory-system" // Phải match JwtService

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer) // Không có audience trong token
                    .build()
            )
            
            validate { credential ->
                // Lấy thông tin từ JWT payload
                // subject chứa userId (xem JwtService.generateAccessToken)
                val userId = credential.payload.subject?.toIntOrNull()
                val email = credential.payload.getClaim("email").asString()
                val role = credential.payload.getClaim("role").asString()
                val tokenType = credential.payload.getClaim("type").asString()
                
                // Chỉ chấp nhận access token (không phải refresh token)
                if (tokenType != "access") {
                    return@validate null
                }
                
                // Kiểm tra các claims có đầy đủ không
                if (userId != null && email != null && role != null) {
                    JwtPrincipal(userId, email, role)
                } else {
                    null
                }
            }
            
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized, 
                    mapOf("error" to "Thiếu hoặc token không hợp lệ")
                )
            }
        }
    }
}

/**
 * Extension function để lấy principal từ call
 */
fun ApplicationCall.getPrincipal(): JwtPrincipal? {
    return principal<JwtPrincipal>()
}

/**
 * Extension function để lấy email từ principal
 */
fun ApplicationCall.getUserEmail(): String {
    return getPrincipal()?.email 
        ?: throw IllegalStateException("Email not found in principal")
}

/**
 * Extension function để kiểm tra role có phải ADMIN không
 */
fun ApplicationCall.isAdmin(): Boolean {
    val principal = getPrincipal()
    return principal?.role == "ADMIN"
}

/**
 * Extension function để kiểm tra role có phải WAREHOUSE_STAFF không
 */
fun ApplicationCall.isStaff(): Boolean {
    val principal = getPrincipal()
    return principal?.role == "WAREHOUSE_STAFF"
}
