package com.sis.android.data.repository

object AuthValidation {
    fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() || !EMAIL.matches(email.trim()) -> "Enter a valid email address."
        password.isBlank() -> "Password is required."
        else -> null
    }

    fun validatePasswordChange(current: String, new: String, confirm: String): String? = when {
        current.isBlank() -> "Enter your current password."
        new.length < 8 -> "New password must be at least 8 characters."
        new == current -> "New password must be different from the current password."
        confirm != new -> "New passwords do not match."
        else -> null
    }

    fun validateUser(name: String, email: String): String? = when {
        name.isBlank() -> "Full name is required."
        !EMAIL.matches(email.trim()) -> "Enter a valid email address."
        else -> null
    }

    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
}
