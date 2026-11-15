package com.sis.android.data.remote

import com.google.gson.annotations.SerializedName
import com.sis.android.data.model.ApiResponse
import com.sis.android.data.model.Category
import com.sis.android.data.model.Product
import com.sis.android.data.model.ProductPageResponse
import com.sis.android.data.model.CreateProductRequest
import com.sis.android.data.model.UpdateProductRequest
import com.sis.android.data.model.ProductResponse
import com.sis.android.data.model.ProductFilterOptions
import com.sis.android.data.model.Supplier
import retrofit2.http.*
import retrofit2.Response

// ─── Auth DTOs ─────────────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("role") val role: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class CreateUserRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("role") val role: String
)

data class UpdateUserRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null
)

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

// ─── API Interface ─────────────────────────────────────────────────────────────

interface ApiService {

    // ========== AUTH & USER MANAGEMENT ==========
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Map<String, String>>

    @POST("api/auth/users")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<UserResponse>

    @PUT("api/auth/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @GET("api/auth/users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<UserResponse>

    @GET("api/auth/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<StaffListResponse>

    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Map<String, Any>>

    // ========== PRODUCTS ==========
    
    @GET("api/products")
    suspend fun getProducts(
        @Query("keyword") keyword: String? = null,
        @Query("warehouseId") warehouseId: Long? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("supplierId") supplierId: Long? = null,
        @Query("isActive") isActive: Boolean? = null,
        @Query("sortBy") sortBy: String? = "created_at",
        @Query("sortDirection") sortDirection: String? = "DESC",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ProductPageResponse

    @GET("api/products/filter-options")
    suspend fun getFilterOptions(): Response<ProductFilterOptions>

    @GET("api/inventory/filter-options")
    suspend fun getInventoryFilterOptions(): Response<ProductFilterOptions>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): ApiResponse<Product>

    @GET("api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ApiResponse<Product>

    @POST("api/products")
    suspend fun createProduct(
        @Body request: CreateProductRequest
    ): Product

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Long,
        @Body request: UpdateProductRequest
    ): ProductResponse

    @PATCH("api/products/{id}/deactivate")
    suspend fun deactivateProduct(@Path("id") productId: Long): ProductResponse

    @PATCH("api/products/{id}/reactivate")
    suspend fun reactivateProduct(
        @Path("id") id: Long
    )

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Long)

    // ========== CATEGORIES ==========
    
    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    @POST("api/categories")
    suspend fun createCategory(
        @Body category: Category
    ): Category

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body category: Category
    ): Category

    @PATCH("api/categories/{id}/deactivate")
    suspend fun deactivateCategory(
        @Path("id") id: Long
    )

    @PATCH("api/categories/{id}/reactivate")
    suspend fun reactivateCategory(
        @Path("id") id: Long
    )

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long
    )

    // ========== SUPPLIERS ==========
    
    @GET("api/suppliers")
    suspend fun getSuppliers(
        @Query("keyword") keyword: String? = null,
        @Query("activeOnly") activeOnly: Boolean? = true
    ): List<Supplier>

    @GET("api/suppliers/{id}")
    suspend fun getSupplierById(
        @Path("id") id: Long
    ): Supplier

    @POST("api/suppliers")
    suspend fun createSupplier(
        @Body supplier: Supplier
    ): Supplier

    @PUT("api/suppliers/{id}")
    suspend fun updateSupplier(
        @Path("id") id: Long,
        @Body supplier: Supplier
    ): Supplier

    @PATCH("api/suppliers/{id}/deactivate")
    suspend fun deactivateSupplier(
        @Path("id") id: Long
    )

    @PATCH("api/suppliers/{id}/reactivate")
    suspend fun reactivateSupplier(
        @Path("id") id: Long
    )

    @DELETE("api/suppliers/{id}")
    suspend fun deleteSupplier(
        @Path("id") id: Long
    )
}
