package com.sis.android.data.session

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "sis_session",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ROLE = "role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "remembered_email"
        private const val KEY_REMEMBER = "remember_login"
        private const val KEY_EXPIRY = "login_expiry"

        private const val SEVEN_DAYS_MILLIS = 7L * 24 * 60 * 60 * 1000
    }

    /** Lưu session sau khi đăng nhập thành công */
    fun saveSession(
        accessToken: String,
        refreshToken: String,
        role: String,
        userId: Int
    ) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_ROLE, role)
            .putInt(KEY_USER_ID, userId)
            .apply()
    }

    /** Lấy access token (dùng cho Authorization header) */
    fun getToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    /** Lấy refresh token (dùng để gia hạn session) */
    fun getRefreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    fun getRole(): String? = preferences.getString(KEY_ROLE, null)

    fun getUserId(): Int = preferences.getInt(KEY_USER_ID, -1)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    /** Bearer token sẵn sàng dùng trong header Authorization */
    fun getBearerToken(): String? = getToken()?.let { "Bearer $it" }

    fun setRememberLogin(remember: Boolean) {
        preferences.edit().putBoolean(KEY_REMEMBER, remember).apply()
    }

    fun shouldRememberLogin(): Boolean =
        preferences.getBoolean(KEY_REMEMBER, false)

    fun saveRememberedEmail(email: String) {
        preferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getRememberedEmail(): String =
        preferences.getString(KEY_EMAIL, "") ?: ""

    fun clearRememberedEmail() {
        preferences.edit().remove(KEY_EMAIL).apply()
    }

    fun saveLoginExpiry() {
        val expiryTime = System.currentTimeMillis() + SEVEN_DAYS_MILLIS
        preferences.edit().putLong(KEY_EXPIRY, expiryTime).apply()
    }

    fun isSessionExpired(): Boolean {
        val expiryTime = preferences.getLong(KEY_EXPIRY, 0L)
        return expiryTime == 0L || System.currentTimeMillis() >= expiryTime
    }

    /** Cập nhật access token mới (sau khi refresh) */
    fun updateAccessToken(newAccessToken: String, newRefreshToken: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
            .putString(KEY_REFRESH_TOKEN, newRefreshToken)
            .apply()
    }

    fun clearSession() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ROLE)
            .remove(KEY_USER_ID)
            .remove(KEY_EXPIRY)
            .apply()
    }

    fun logoutAndClearRememberedEmail() {
        clearSession()
        clearRememberedEmail()
        setRememberLogin(false)
    }
}
