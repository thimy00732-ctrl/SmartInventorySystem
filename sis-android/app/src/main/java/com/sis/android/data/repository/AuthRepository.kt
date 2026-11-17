package com.sis.android.data.repository

import com.google.gson.JsonParser
import com.sis.android.data.model.Account
import com.sis.android.data.model.ChangePasswordRequest
import com.sis.android.data.model.CreateUserRequest
import com.sis.android.data.model.LoginRequest
import com.sis.android.data.model.ProfileUpdateRequest
import com.sis.android.data.model.RefreshRequest
import com.sis.android.data.model.UpdateRoleRequest
import com.sis.android.data.model.UpdateStatusRequest
import com.sis.android.data.model.UpdateUserRequest
import com.sis.android.data.remote.ApiService
import com.sis.android.data.session.Session
import com.sis.android.data.session.SessionManager
import retrofit2.HttpException

class AuthRepository(
    private val api: ApiService,
    private val sessions: SessionManager
) {
    suspend fun login(email: String, password: String): Account {
        val result = api.login(LoginRequest(email.trim(), password))
        sessions.save(Session(result.accessToken, result.refreshToken, result.expiresIn, result.user))
        return result.user
    }

    suspend fun refreshAccount(): Account {
        val user = api.getMe()
        sessions.updateUser(user)
        return user
    }

    suspend fun logout() {
        val refreshToken = sessions.get()?.refreshToken
        try {
            if (!refreshToken.isNullOrBlank()) api.logout(RefreshRequest(refreshToken))
        } finally {
            sessions.clear()
        }
    }

    suspend fun updateProfile(name: String, phone: String?): Account {
        val user = api.updateMe(ProfileUpdateRequest(name.trim(), phone?.trim()?.ifBlank { null }))
        sessions.updateUser(user)
        return user
    }

    suspend fun changePassword(current: String, new: String, confirm: String) {
        api.changePassword(ChangePasswordRequest(current, new, confirm))
        try {
            logout()
        } catch (_: Exception) {
            sessions.clear()
        }
    }

    suspend fun listUsers(role: String?, status: String?) = api.users(role, status)
    suspend fun createUser(request: CreateUserRequest) {
        api.createUser(request)
    }
    suspend fun updateUser(id: Int, request: UpdateUserRequest) {
        api.updateUser(id, request)
    }
    suspend fun setStatus(id: Int, status: String) {
        api.updateStatus(id, UpdateStatusRequest(status))
    }
    suspend fun setRole(id: Int, role: String) {
        api.updateRole(id, UpdateRoleRequest(role))
    }

    companion object {
        fun errorMessage(error: Throwable): String {
            if (error is HttpException) {
                val body = error.response()?.errorBody()?.string()
                if (!body.isNullOrBlank()) {
                    runCatching {
                        JsonParser.parseString(body).asJsonObject.get("message")?.asString
                    }.getOrNull()?.let { return it }
                }
                return when (error.code()) {
                    401 -> "Your session has expired. Please sign in again."
                    403 -> "You do not have permission to perform this action."
                    else -> "Request failed (${error.code()})."
                }
            }
            return error.message?.takeIf { it.isNotBlank() } ?: "Something went wrong."
        }
    }
}