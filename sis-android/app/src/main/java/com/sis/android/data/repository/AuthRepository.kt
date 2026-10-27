
package com.sis.android.data.repository

import com.sis.android.data.remote.MockAuthApi
import com.sis.android.data.session.SessionManager

class AuthRepository(
    private val sessionManager: SessionManager,
    private val mockAuthApi: MockAuthApi = MockAuthApi()
) {

    suspend fun login(
        email: String,
        password: String,
        rememberLogin: Boolean
    ): String {

        val normalizedEmail = email.trim()
        val token = mockAuthApi.login(normalizedEmail, password)

        val role = when (token) {
            "mock_admin_token" -> "ADMIN"
            "mock_staff_token" -> "STAFF"
            else -> throw IllegalArgumentException("Token không hợp lệ.")
        }

        // Lưu phiên đăng nhập cho phiên sử dụng hiện tại.
        sessionManager.saveSession(token, role)
        sessionManager.setRememberLogin(rememberLogin)

        if (rememberLogin) {
            // Ghi nhớ email và thời hạn đăng nhập 7 ngày.
            sessionManager.saveRememberedEmail(normalizedEmail)
            sessionManager.saveLoginExpiry()
        } else {
            // Không ghi nhớ email và không giữ phiên qua lần mở app sau.
            sessionManager.clearRememberedEmail()
            sessionManager.saveLoginExpiry()
        }

        return role
    }

    fun getRememberedEmail(): String {
        return sessionManager.getRememberedEmail()
    }

    fun shouldRememberLogin(): Boolean {
        return sessionManager.shouldRememberLogin()
    }

    fun getSavedRole(): String? {
        // Không tự đăng nhập nếu người dùng không chọn ghi nhớ.
        if (!sessionManager.shouldRememberLogin()) {
            sessionManager.clearSession()
            return null
        }

        // Hết hạn sau 7 ngày.
        if (sessionManager.isSessionExpired()) {
            sessionManager.clearSession()
            sessionManager.setRememberLogin(false)
            return null
        }

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
        // Đăng xuất nhưng vẫn giữ email đã ghi nhớ.
        sessionManager.clearSession()
    }

    fun logoutAndForgetEmail() {
        // Đăng xuất và xóa email đã ghi nhớ.
        sessionManager.logoutAndClearRememberedEmail()
    }
}
