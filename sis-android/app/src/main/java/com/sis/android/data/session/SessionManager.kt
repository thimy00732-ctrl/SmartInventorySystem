package com.sis.android.data.session

import android.content.Context
import com.google.gson.Gson
import com.sis.android.data.model.Account

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: Account
)

class SessionManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "sis_session",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    @Synchronized
    fun get(): Session? {
        val access = preferences.getString(KEY_ACCESS, null) ?: return null
        val refresh = preferences.getString(KEY_REFRESH, null) ?: return null
        val userJson = preferences.getString(KEY_USER, null) ?: return null
        val user = runCatching { gson.fromJson(userJson, Account::class.java) }.getOrNull()
            ?: return null
        return Session(access, refresh, preferences.getLong(KEY_EXPIRY, 0), user)
    }

    @Synchronized
    fun save(session: Session) {
        preferences.edit()
            .putString(KEY_ACCESS, session.accessToken)
            .putString(KEY_REFRESH, session.refreshToken)
            .putLong(KEY_EXPIRY, session.expiresIn)
            .putString(KEY_USER, gson.toJson(session.user))
            .apply()
    }

    @Synchronized
    fun updateTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        preferences.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .putLong(KEY_EXPIRY, expiresIn)
            .apply()
    }

    @Synchronized
    fun updateUser(user: Account) {
        preferences.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    @Synchronized
    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_EXPIRY = "expires_in"
        const val KEY_USER = "user"
    }
}