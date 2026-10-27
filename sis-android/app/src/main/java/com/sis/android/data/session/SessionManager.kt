
package com.sis.android.data.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        appContext,
        "sis_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Lưu token và vai trò người dùng
    fun saveSession(token: String, role: String) {
        preferences.edit()
            .putString("token", token)
            .putString("role", role)
            .apply()
    }

    // Lấy token đã lưu
    fun getToken(): String? {
        return preferences.getString("token", null)
    }

    // Lấy vai trò người dùng
    fun getRole(): String? {
        return preferences.getString("role", null)
    }

    // Kiểm tra đã có token hay chưa
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    // Xóa phiên đăng nhập
    fun clearSession() {
        preferences.edit()
            .clear()
            .apply()
    }
}
