package com.sis.android.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ─── DTOs (khớp với backend AuthDto.kt) ───────────────────────────────────────

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

/** Tạo nhân viên mới — dùng bởi Admin */
data class CreateUserRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("role") val role: String   // "ADMIN" | "WAREHOUSE_STAFF"
)

/** Cập nhật thông tin nhân viên — dùng bởi Admin */
data class UpdateUserRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null  // "ACTIVE" | "INACTIVE"
)

/** Thông tin chi tiết một nhân viên — trả về từ backend */
data class UserResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String,
    @SerializedName("last_login_at") val lastLoginAt: String?,
    @SerializedName("created_at") val createdAt: String?
)

/** Danh sách nhân viên — trả về từ GET /api/auth/users */
data class StaffListResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("staff") val staff: List<UserResponse>
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)

// ─── Retrofit API Interface ────────────────────────────────────────────────────

interface ApiService {

    // UC01: Đăng nhập
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // UC02: Đăng xuất
    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Map<String, String>>

    // UC03: Tạo nhân viên mới (Admin only)
    @POST("api/auth/users")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<UserResponse>

    // UC03: Cập nhật nhân viên (Admin only)
    @PUT("api/auth/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    // UC08: Lấy chi tiết 1 nhân viên theo ID
    @GET("api/auth/users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<UserResponse>

    // Lấy danh sách nhân viên (Admin only, có phân trang)
    @GET("api/auth/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<StaffListResponse>

    // UC07: Làm mới access token
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    // UC06: Đổi mật khẩu
    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Map<String, Any>>
}
