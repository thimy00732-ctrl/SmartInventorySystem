package com.sis.android.data.remote

import com.sis.android.data.model.Account
import com.sis.android.data.model.ApiMessage
import com.sis.android.data.model.ChangePasswordRequest
import com.sis.android.data.model.CreateUserRequest
import com.sis.android.data.model.LoginRequest
import com.sis.android.data.model.LoginResponse
import com.sis.android.data.model.ProfileUpdateRequest
import com.sis.android.data.model.RefreshRequest
import com.sis.android.data.model.UpdateRoleRequest
import com.sis.android.data.model.UpdateStatusRequest
import com.sis.android.data.model.UpdateUserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    fun refresh(@Body request: RefreshRequest): Call<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshRequest): ApiMessage

    @GET("api/account/me")
    suspend fun getMe(): Account

    @PATCH("api/account/me")
    suspend fun updateMe(@Body request: ProfileUpdateRequest): Account

    @POST("api/account/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiMessage

    @GET("api/admin/users")
    suspend fun users(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null
    ): List<Account>

    @POST("api/admin/users")
    suspend fun createUser(@Body request: CreateUserRequest): Account

    @PUT("api/admin/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UpdateUserRequest
    ): Account

    @PATCH("api/admin/users/{id}/status")
    suspend fun updateStatus(@Path("id") id: Int, @Body request: UpdateStatusRequest): ApiMessage

    @PATCH("api/admin/users/{id}/role")
    suspend fun updateRole(@Path("id") id: Int, @Body request: UpdateRoleRequest): ApiMessage
}