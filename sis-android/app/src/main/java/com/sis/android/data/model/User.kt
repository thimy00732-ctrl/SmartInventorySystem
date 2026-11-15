package com.sis.android.data.model

data class User(
    val user_id: Int,
    val email: String,
    val fullName: String,
    val role: String,
    val is_active: Boolean
)
