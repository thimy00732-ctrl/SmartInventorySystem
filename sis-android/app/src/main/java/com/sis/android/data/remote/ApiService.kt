package com.sis.android.data.remote

import com.sis.android.data.model.ApiResponse
import com.sis.android.data.model.Category
import com.sis.android.data.model.Product
import retrofit2.http.*
import com.sis.android.data.model.ProductPageResponse
import com.sis.android.data.model.CreateProductRequest
import com.sis.android.data.model.UpdateProductRequest
import com.sis.android.data.model.ProductResponse
interface ApiService {

    @GET("api/products")
    suspend fun getProducts(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100
    ): ProductPageResponse  // <-- BỎ ApiResponse wrapper



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
    suspend fun deleteProduct(
        @Path(value = "id") id: Long
    ): ApiResponse<Void> // Hoặc bỏ hẳn kiểu trả về nếu dùng Response thuần của Retrofit

    @GET("api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ApiResponse<Product>

    @PATCH("api/products/{id}/deactivate")
    suspend fun deactivateProduct(
        @Path("id") id: Long
    )

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