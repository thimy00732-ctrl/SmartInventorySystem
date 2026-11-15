package com.sis.android.data.repository

import com.sis.android.data.model.ManagedStaff
import com.sis.android.data.model.toManagedStaff
import com.sis.android.data.remote.ApiService
import com.sis.android.data.remote.CreateUserRequest
import com.sis.android.data.remote.UpdateUserRequest
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
            throw handleNetworkException(e, "Không thể tải danh sách nhân viên")
        }

        if (!response.isSuccessful) {
            throw handleHttpError(response.code(), response.errorBody()?.string())
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
            throw handleNetworkException(e, "Không thể tạo nhân viên")
        }

        if (!response.isSuccessful) {
            throw handleHttpError(response.code(), response.errorBody()?.string())
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
            throw handleNetworkException(e, "Không thể cập nhật nhân viên")
        }

        if (!response.isSuccessful) {
            throw handleHttpError(response.code(), response.errorBody()?.string())
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
            throw handleNetworkException(e, "Không thể cập nhật trạng thái")
        }

        if (!response.isSuccessful) {
            throw handleHttpError(response.code(), response.errorBody()?.string())
        }

        return response.body()?.toManagedStaff()
            ?: throw Exception("Phản hồi không hợp lệ từ máy chủ")
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun handleHttpError(code: Int, errorBody: String?): Exception {
        val parsed = parseError(errorBody)
        if (!parsed.isNullOrBlank()) return Exception(parsed)

        return when (code) {
            401 -> Exception("401 Unauthorized: Phiên làm việc hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.")
            403 -> Exception("403 Forbidden: Bạn không có quyền thực hiện thao tác này (Cần quyền Admin).")
            404 -> Exception("404 Not Found: Không tìm thấy dữ liệu yêu cầu.")
            500, 502, 503 -> Exception("500 Server Error: Lỗi máy chủ nội bộ. Vui lòng thử lại sau.")
            else -> Exception("Lỗi kết nối ($code). Vui lòng thử lại.")
        }
    }

    private fun handleNetworkException(e: Exception, actionMessage: String): IOException {
        return when (e) {
            is SocketTimeoutException -> IOException("Lỗi kết nối quá thời gian chờ (Timeout). Vui lòng kiểm tra lại mạng.")
            is UnknownHostException, is ConnectException -> IOException("Lỗi mất kết nối mạng: Không thể kết nối tới máy chủ.")
            is IOException -> e
            else -> IOException("$actionMessage: ${e.message}")
        }
    }

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
