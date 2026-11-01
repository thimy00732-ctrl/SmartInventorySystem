package com.sis.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.*

/**
 * JwtService: Quản lý JWT (JSON Web Token)
 * - Tạo access token (hợp lệ 1 giờ)
 * - Tạo refresh token (hợp lệ 7 ngày)
 * - Xác minh và giải mã JWT
 * - UC01: Tạo JWT khi đăng nhập
 * - UC07: Xác minh JWT trên tất cả requests
 */
class JwtService {

    // Secret key để ký JWT - nên lấy từ environment variable trong production
    private val secretKey = System.getenv("JWT_SECRET_KEY") ?: "smart-inventory-secret-key-do-not-use-in-production"

    // Issuer của JWT
    private val issuer = "smart-inventory-system"

    // Algorithm dùng để ký JWT
    private val algorithm = Algorithm.HMAC256(secretKey)

    // Verifier dùng để xác minh JWT
    private val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * UC01: Tạo access token (hợp lệ 1 giờ)
     * Access token dùng để xác thực người dùng trên API requests
     * @param userId - id của người dùng
     * @param email - email của người dùng
     * @param role - role của người dùng (ADMIN hoặc WAREHOUSE_STAFF)
     * @return JWT access token string
     */
    fun generateAccessToken(userId: Int, email: String, role: String): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(3600) // 1 giờ

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString()) // user_id
            .withClaim("email", email)
            .withClaim("role", role)
            .withClaim("type", "access") // loại token
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    /**
     * UC01: Tạo refresh token (hợp lệ 7 ngày)
     * Refresh token dùng để lấy access token mới mà không cần đăng nhập lại (UC07)
     * @param userId - id của người dùng
     * @return JWT refresh token string
     */
    fun generateRefreshToken(userId: Int): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(604800) // 7 ngày (604800 giây)

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString()) // user_id
            .withClaim("type", "refresh") // loại token
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    /**
     * UC07: Xác minh access token
     * Kiểm tra xem token có hợp lệ không và trích xuất thông tin từ token
     * @param token - JWT access token string
     * @return TokenClaims object chứa user_id, email, role, hoặc null nếu token không hợp lệ
     */
    fun verifyAccessToken(token: String): TokenClaims? {
        return try {
            val decodedJWT = verifier.verify(token)

            // Kiểm tra xem type có phải "access" không
            val tokenType = decodedJWT.getClaim("type").asString()
            if (tokenType != "access") {
                return null
            }

            TokenClaims(
                userId = decodedJWT.subject.toIntOrNull() ?: return null,
                email = decodedJWT.getClaim("email").asString() ?: "",
                role = decodedJWT.getClaim("role").asString() ?: "",
                expiresAt = decodedJWT.expiresAtAsInstant
            )
        } catch (e: Exception) {
            // Token không hợp lệ, hết hạn, hoặc bị giả mạo
            null
        }
    }

    /**
     * UC07: Xác minh refresh token
     * Kiểm tra xem refresh token có hợp lệ không
     * @param token - JWT refresh token string
     * @return userId nếu token hợp lệ, null nếu không
     */
    fun verifyRefreshToken(token: String): Int? {
        return try {
            val decodedJWT = verifier.verify(token)

            // Kiểm tra xem type có phải "refresh" không
            val tokenType = decodedJWT.getClaim("type").asString()
            if (tokenType != "refresh") {
                return null
            }

            decodedJWT.subject.toIntOrNull()
        } catch (e: Exception) {
            // Token không hợp lệ hoặc hết hạn
            null
        }
    }

    /**
     * Helper: Trích xuất user_id từ access token mà không xác minh
     * Dùng khi chỉ cần lấy user_id nhanh (nhưng nên xác minh trước khi sử dụng)
     * @param token - JWT token string
     * @return userId nếu giải mã được, null nếu không
     */
    fun getUserIdFromToken(token: String): Int? {
        return try {
            val decodedJWT = JWT.decode(token)
            decodedJWT.subject.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Data class chứa thông tin từ JWT claims
 * @param userId - id của người dùng
 * @param email - email của người dùng
 * @param role - role của người dùng
 * @param expiresAt - thời gian hết hạn của token
 */
data class TokenClaims(
    val userId: Int,
    val email: String,
    val role: String,
    val expiresAt: Instant
)
