
package com.sis.android.data.repository

import com.sis.android.data.remote.MockAuthApi
import com.sis.android.data.session.SessionManager

class AuthRepository(
    private val sessionManager: SessionManager,
    private val mockAuthApi: MockAuthApi = MockAuthApi()
) {

    suspend fun login(
        username: String,
        password: String
    ): String {
        // Goi API dang nhap gia lap
        val token = mockAuthApi.login(username, password)

        // Xac dinh vai tro tu token
        val role = when (token) {
            "mock_admin_token" -> "ADMIN"
            "mock_staff_token" -> "STAFF"
            else -> throw IllegalArgumentException("Token không hợp lệ.")
        }

        // Luu phien dang nhap
        sessionManager.saveSession(token, role)

        return role
    }

    fun getSavedRole(): String? {
        if (!sessionManager.isLoggedIn()) {
            return null
        }

        return when (sessionManager.getToken()) {
            "mock_admin_token" -> "ADMIN"
            "mock_staff_token" -> "STAFF"
            else -> {
                sessionManager.clearSession()
                null
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
