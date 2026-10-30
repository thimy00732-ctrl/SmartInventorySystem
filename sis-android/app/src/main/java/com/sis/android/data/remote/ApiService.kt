package com.sis.android.data.remote

import com.sis.android.data.model.Category
import com.sis.android.data.model.Product
import retrofit2.http.*

interface ApiService {

    @GET("api/products")
    suspend fun getProducts(
        @Query("q") keyword: String? = null
    ): List<Product>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): Product

    @POST("api/products")
    suspend fun createProduct(
        @Body product: Product
    ): Product

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body product: Product
    ): Product

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    )

    @GET("api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Product

    // --- CATEGORIES (Danh mục sản phẩm) ---
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

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long
    )
}