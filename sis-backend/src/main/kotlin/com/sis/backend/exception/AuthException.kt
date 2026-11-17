package com.sis.backend.exception

import io.ktor.http.HttpStatusCode

class AuthException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String
) : RuntimeException(message)
