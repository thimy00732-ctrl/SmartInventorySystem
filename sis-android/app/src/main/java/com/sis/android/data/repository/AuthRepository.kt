package com.sis.android.data.repository

import com.sis.android.data.remote.ApiService
import com.sis.android.data.remote.LoginRequest
import com.sis.android.data.remote.LogoutRequest
import com.sis.android.data.session.SessionManager
import java.io.IOException

class AuthRepository(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) {

    /**
     * Đăng nhập bằng API thực.
     * @return role của người dùng ("ADMIN" hoặc "WAREHOUSE_STAFF")
     * @throws IOException khi không kết nối được server
     * @throws IllegalArgumentException khi sai email/mật khẩu hoặc lỗi từ server
     */
    suspend fun login(
        email: String,
        password: String,
        rememberLogin: Boolean
    ): String {
        val normalizedEmail = email.trim()

        val response = try {
            apiService.login(LoginRequest(normalizedEmail, password))
        } catch (e: Exception) {
            throw when (e) {
                is java.net.SocketTimeoutException -> IOException("Lỗi kết nối quá thời gian chờ (Timeout). Vui lòng thử lại.")
                is java.net.UnknownHostException, is java.net.ConnectException -> IOException("Lỗi mất kết nối mạng: Không thể kết nối tới máy chủ.")
                else -> IOException("Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng.")
            }
        }

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.errorBody()?.string())
                ?: when (response.code()) {
                    401 -> "401 Unauthorized: Mật khẩu không chính xác hoặc tài khoản đã bị khóa."
                    403 -> "403 Forbidden: Tài khoản của bạn bị từ chối truy cập."
                    else -> "Lỗi đăng nhập (${response.code()}). Vui lòng thử lại."
                }
            throw IllegalArgumentException(errorMsg)
        }

        val body = response.body()
            ?: throw IllegalArgumentException("Phản hồi từ máy chủ không hợp lệ.")

        // Lưu session với JWT thực
        sessionManager.saveSession(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            role = body.role,
            userId = body.userId
        )
        sessionManager.setRememberLogin(rememberLogin)

        if (rememberLogin) {
            sessionManager.saveRememberedEmail(normalizedEmail)
            sessionManager.saveLoginExpiry()
        } else {
            sessionManager.clearRememberedEmail()
            sessionManager.saveLoginExpiry()
        }

        return body.role
    }

    /**
     * Đăng xuất — gọi API để thu hồi refresh token, sau đó xóa session local.
     */
    suspend fun logout() {
        val refreshToken = sessionManager.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            try {
                apiService.logout(LogoutRequest(refreshToken))
            } catch (_: Exception) {
                // Nếu API lỗi vẫn xóa session local
            }
        }
        sessionManager.clearSession()
    }

    /**
     * Đăng xuất và xóa email đã ghi nhớ.
     */
    suspend fun logoutAndForgetEmail() {
        logout()
        sessionManager.logoutAndClearRememberedEmail()
    }

    fun getRememberedEmail(): String = sessionManager.getRememberedEmail()

    fun shouldRememberLogin(): Boolean = sessionManager.shouldRememberLogin()

    /**
     * Lấy role đã lưu nếu session vẫn còn hợp lệ (dùng khi mở lại app).
     * @return role nếu session còn hạn, null nếu cần đăng nhập lại.
     */
    fun getSavedRole(): String? {
        if (!sessionManager.shouldRememberLogin()) {
            sessionManager.clearSession()
            return null
        }
        if (sessionManager.isSessionExpired()) {
            sessionManager.clearSession()
            sessionManager.setRememberLogin(false)
            return null
        }
        if (!sessionManager.isLoggedIn()) return null

        return sessionManager.getRole()
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /** Trích xuất message lỗi từ JSON response body */
    private fun parseErrorMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            // Backend trả về {"error": "..."} hoặc {"message": "..."}
            val regex = """"(?:error|message)"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(body)?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
    }
}
