package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("role") val role: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long
) {
    // Helper để convert sang User model
    fun toUser() = User(
        user_id = userId,
        email = email,
        fullName = fullName,
        role = role,
        is_active = true,
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}
