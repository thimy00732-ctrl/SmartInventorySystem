package com.sis.android.data.remote

import com.sis.android.data.model.ApiResponse
import com.sis.android.data.model.Category
import com.sis.android.data.model.Product
import retrofit2.http.*
import retrofit2.Response
import com.sis.android.data.model.ProductPageResponse
import com.sis.android.data.model.CreateProductRequest
import com.sis.android.data.model.UpdateProductRequest
import com.sis.android.data.model.ProductResponse
import com.sis.android.data.model.ProductFilterOptions
interface ApiService {

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

    @POST("api/products")
    suspend fun createProduct(
        @Body request: CreateProductRequest
    ): Product

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Long,
        @Body request: UpdateProductRequest
    ): ProductResponse

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Long)


    @GET("api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ApiResponse<Product>

    @PATCH("api/products/{id}/deactivate") // 👈 Sửa thành @PATCH cho đúng với backend
    suspend fun deactivateProduct(@Path("id") productId: Long): ProductResponse

    @PATCH("api/products/{id}/reactivate")
    suspend fun reactivateProduct(
        @Path("id") id: Long
    )

    // --- CATEGORIES (Danh mục sản phẩm) ---
    @GET("api/categories")
    suspend fun getCategories(): List<Category>  // ← BỎ ApiResponse wrapper

    @POST("api/categories")
    suspend fun createCategory(
        @Body category: Category
    ): Category  // ← BỎ ApiResponse wrapper

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body category: Category
    ): Category  // ← BỎ ApiResponse wrapper

    @PATCH("api/categories/{id}/deactivate")
    suspend fun deactivateCategory(
        @Path("id") id: Long
    ): Unit  // ← BỎ ApiResponse wrapper
}