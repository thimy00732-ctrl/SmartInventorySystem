package com.sis.android.data.remote

import com.sis.android.data.model.ApiResponse
import com.sis.android.data.model.Category
import com.sis.android.data.model.Product
import retrofit2.http.*
import com.sis.android.data.model.ProductPageResponse

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
        @Body product: Product
    ): ApiResponse<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body product: Product
    ): ApiResponse<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    @GET("api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ApiResponse<Product>

    // --- CATEGORIES (Danh mục sản phẩm) ---
    @GET("api/categories")
    suspend fun getCategories(): ApiResponse<List<Category>>

    @POST("api/categories")
    suspend fun createCategory(
        @Body category: Category
    ): ApiResponse<Category>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body category: Category
    ): ApiResponse<Category>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long
    ): ApiResponse<Unit>
}