package com.sis.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

class JwtService(
    private val secret: String = System.getenv("JWT_SECRET")
        ?: "dev-only-secret-change-before-deployment-2026",
    private val issuer: String = System.getenv("JWT_ISSUER")
        ?: "smart-inventory-backend",
    private val audience: String = System.getenv("JWT_AUDIENCE")
        ?: "smart-inventory-client",
    private val expiresInMs: Long = 3_600_000L
) {
    private val algorithm: Algorithm by lazy {
        require(secret.toByteArray(Charsets.UTF_8).size >= 32) {
            "JWT_SECRET phải có ít nhất 32 byte"
        }
        Algorithm.HMAC256(secret)
    }

    fun generateAccessToken(
        userId: Int,
        email: String,
        role: String
    ): String {
        require(userId > 0)
        require(role in setOf("ADMIN", "WAREHOUSE_STAFF"))

        val now = System.currentTimeMillis()

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("role", role)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + expiresInMs))
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT {
        require(token.isNotBlank()) { "JWT không được để trống" }

        return JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
            .verify(token)
    }

    fun getExpiresInSeconds(): Long = expiresInMs / 1000
}