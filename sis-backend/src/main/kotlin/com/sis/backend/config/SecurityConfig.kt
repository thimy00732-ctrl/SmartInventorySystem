package com.sis.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val secret = System.getenv("JWT_SECRET")
        ?: "dev-only-secret-change-before-deployment-2026"
    val issuer = System.getenv("JWT_ISSUER")
        ?: "smart-inventory-backend"
    val audience = System.getenv("JWT_AUDIENCE")
        ?: "smart-inventory-client"

    require(secret.toByteArray(Charsets.UTF_8).size >= 32) {
        "JWT_SECRET phải có ít nhất 32 byte"
    }

    val verifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Smart Inventory"
            verifier(verifier)

            validate { credential ->
                val userId = credential.payload.subject?.toIntOrNull()
                val email = credential.payload.getClaim("email").asString()
                val role = credential.payload.getClaim("role").asString()

                if (
                    userId != null &&
                    userId > 0 &&
                    !email.isNullOrBlank() &&
                    role in setOf("ADMIN", "WAREHOUSE_STAFF")
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}