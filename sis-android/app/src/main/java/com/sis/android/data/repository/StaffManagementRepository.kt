package com.sis.android.data.repository

import com.sis.android.data.model.ManagedStaff
import com.sis.android.data.model.toManagedStaff
import com.sis.android.data.remote.ApiService
import com.sis.android.data.remote.CreateUserRequest
import com.sis.android.data.remote.UpdateUserRequest
import java.io.IOException

/**
 * Repository quản lý nhân viên — gọi API backend thực.
 * Tất cả các phương thức đều là suspend function (chạy trên coroutine).
 */
class StaffManagementRepository(
    private val apiService: ApiService
) {

    /**
     * Lấy toàn bộ danh sách nhân viên.
     * GET /api/auth/users?limit=100&offset=0
     */
    suspend fun getStaff(bearerToken: String): List<ManagedStaff> {
        val response = try {
            apiService.getUsers(bearerToken, limit = 100, offset = 0)
        } catch (e: Exception) {
            throw IOException("Không thể tải danh sách nhân viên: ${e.message}")
        }

        if (!response.isSuccessful) {
            val msg = parseError(response.errorBody()?.string())
            throw Exception(msg ?: "Lỗi tải danh sách nhân viên (${response.code()})")
        }

        return response.body()?.staff?.map { it.toManagedStaff() } ?: emptyList()
    }

    /**
     * Tạo nhân viên mới.
     * POST /api/auth/users
     * Backend yêu cầu: email, password, full_name, phone, role ("ADMIN"|"WAREHOUSE_STAFF")
     */
    suspend fun addStaff(
        bearerToken: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ): ManagedStaff {
        val request = CreateUserRequest(
            email = email.trim(),
            password = password,
            fullName = fullName.trim(),
            phone = phone.trim(),
            role = role
        )

        val response = try {
            apiService.createUser(bearerToken, request)
        } catch (e: Exception) {
            throw IOException("Không thể tạo nhân viên: ${e.message}")
        }

        if (!response.isSuccessful) {
            val msg = parseError(response.errorBody()?.string())
            throw Exception(msg ?: "Lỗi tạo nhân viên (${response.code()})")
        }

        return response.body()?.toManagedStaff()
            ?: throw Exception("Phản hồi không hợp lệ từ máy chủ")
    }

    /**
     * Cập nhật thông tin nhân viên.
     * PUT /api/auth/users/{id}
     */
    suspend fun updateStaff(
        bearerToken: String,
        staff: ManagedStaff
    ): ManagedStaff {
        val request = UpdateUserRequest(
            email = staff.email.ifBlank { null },
            fullName = staff.fullName.ifBlank { null },
            phone = staff.phone.ifBlank { null },
            role = staff.role
        )

        val response = try {
            apiService.updateUser(bearerToken, staff.id, request)
        } catch (e: Exception) {
            throw IOException("Không thể cập nhật nhân viên: ${e.message}")
        }

        if (!response.isSuccessful) {
            val msg = parseError(response.errorBody()?.string())
            throw Exception(msg ?: "Lỗi cập nhật nhân viên (${response.code()})")
        }

        return response.body()?.toManagedStaff()
            ?: throw Exception("Phản hồi không hợp lệ từ máy chủ")
    }

    /**
     * Đặt trạng thái active/inactive cho nhân viên.
     * PUT /api/auth/users/{id} với status = "ACTIVE" | "INACTIVE"
     */
    suspend fun setActive(
        bearerToken: String,
        staffId: Int,
        isActive: Boolean
    ): ManagedStaff {
        val request = UpdateUserRequest(
            status = if (isActive) "ACTIVE" else "INACTIVE"
        )

        val response = try {
            apiService.updateUser(bearerToken, staffId, request)
        } catch (e: Exception) {
            throw IOException("Không thể cập nhật trạng thái: ${e.message}")
        }

        if (!response.isSuccessful) {
            val msg = parseError(response.errorBody()?.string())
            throw Exception(msg ?: "Lỗi cập nhật trạng thái (${response.code()})")
        }

        return response.body()?.toManagedStaff()
            ?: throw Exception("Phản hồi không hợp lệ từ máy chủ")
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun parseError(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val regex = """"(?:error|message)"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(body)?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
    }
}
