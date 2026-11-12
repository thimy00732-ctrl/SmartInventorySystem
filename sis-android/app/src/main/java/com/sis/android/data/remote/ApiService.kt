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
import com.sis.android.data.model.Supplier

interface ApiService {

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
