package com.sis.android.data.model

import com.sis.android.data.remote.UserResponse

/**
 * Model nhân viên dùng trong UI layer.
 * Roles hợp lệ từ backend: "ADMIN" | "WAREHOUSE_STAFF"
 * Status hợp lệ từ backend: "ACTIVE" | "INACTIVE"
 */
data class ManagedStaff(
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val role: String = "WAREHOUSE_STAFF",
    val status: String = "ACTIVE",
    val lastLoginAt: String? = null,
    val createdAt: String? = null
) {
    /** Tiện ích: kiểm tra nhân viên đang active */
    val isActive: Boolean get() = status == "ACTIVE"
}

/** Chuyển đổi từ UserResponse (API) sang ManagedStaff (UI) */
fun UserResponse.toManagedStaff() = ManagedStaff(
    id = userId,
    fullName = fullName ?: email,   // fallback về email nếu chưa có tên
    email = email,
    phone = phone ?: "",
    role = role,
    status = status,
    lastLoginAt = lastLoginAt,
    createdAt = createdAt
)
