
package com.sis.android.data.model

data class ManagedStaff(
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val branch: String,
    val role: String = "STAFF",
    val isActive: Boolean = true
)
