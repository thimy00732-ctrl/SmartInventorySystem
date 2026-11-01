package com.sis.backend.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {
    fun hashPassword(plainPassword: String): String {
        require(plainPassword.isNotBlank()) {
            "Mật khẩu không được để trống"
        }
        return BCrypt.withDefaults()
            .hashToString(12, plainPassword.toCharArray())
    }

    fun verifyPassword(
        plainPassword: String,
        passwordHash: String
    ): Boolean {
        if (plainPassword.isEmpty() || passwordHash.isBlank()) return false

        return try {
            BCrypt.verifyer()
                .verify(plainPassword.toCharArray(), passwordHash)
                .verified
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}