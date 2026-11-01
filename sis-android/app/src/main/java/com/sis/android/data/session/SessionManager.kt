
package com.sis.android.data.session

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "sis_session",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role"
        private const val KEY_EMAIL = "remembered_email"
        private const val KEY_REMEMBER = "remember_login"
        private const val KEY_EXPIRY = "login_expiry"

        private const val SEVEN_DAYS_MILLIS =
            7L * 24 * 60 * 60 * 1000
    }

    fun saveSession(token: String, role: String) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun getRole(): String? {
        return preferences.getString(KEY_ROLE, null)
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun setRememberLogin(remember: Boolean) {
        preferences.edit()
            .putBoolean(KEY_REMEMBER, remember)
            .apply()
    }

    fun shouldRememberLogin(): Boolean {
        return preferences.getBoolean(KEY_REMEMBER, false)
    }

    fun saveRememberedEmail(email: String) {
        preferences.edit()
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getRememberedEmail(): String {
        return preferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun clearRememberedEmail() {
        preferences.edit()
            .remove(KEY_EMAIL)
            .apply()
    }

    fun saveLoginExpiry() {
        val expiryTime = System.currentTimeMillis() + SEVEN_DAYS_MILLIS

        preferences.edit()
            .putLong(KEY_EXPIRY, expiryTime)
            .apply()
    }

    fun isSessionExpired(): Boolean {
        val expiryTime = preferences.getLong(KEY_EXPIRY, 0L)

        return expiryTime == 0L ||
                System.currentTimeMillis() >= expiryTime
    }

    fun clearSession() {
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_ROLE)
            .remove(KEY_EXPIRY)
            .apply()
    }

    fun logoutAndClearRememberedEmail() {
        clearSession()
        clearRememberedEmail()
        setRememberLogin(false)
    }
}
