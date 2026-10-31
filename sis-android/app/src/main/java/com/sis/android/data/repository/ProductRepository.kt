package com.sis.android.data.repository

import com.sis.android.data.model.Product
import com.sis.android.data.remote.RetrofitClient

class ProductRepository {

    // Sử dụng ApiService đã cấu hình Retrofit
    private val apiService = RetrofitClient.apiService

    /** Lấy danh sách sản phẩm từ API /api/products (hỗ trợ tìm kiếm từ khóa q) */
    suspend fun getProducts(keyword: String? = null): List<Product> {
        return try {
            println("🔵 Đang gọi API: keyword=$keyword")
            val pageResponse = apiService.getProducts(keyword = keyword, page = 1, size = 100)
            println("🟢 Response: items size=${pageResponse.items.size}")

            pageResponse.items  // <-- Lấy trực tiếp items
        } catch (e: Exception) {
            println("🔴 LỖI: ${e.message}")
            e.printStackTrace()
            throw Exception("Lỗi kết nối Server: ${e.localizedMessage ?: "Không thể tải danh sách sản phẩm"}")
        }
    }




    /** Xem chi tiết sản phẩm theo ID từ API /api/products/{id} */
    suspend fun getProductById(productId: Long): Product? {
        return try {
            val response = apiService.getProductById(productId)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    /** Tạo sản phẩm mới qua API POST /api/products */
    suspend fun createProduct(product: Product): Product? {
        return try {
            val response = apiService.createProduct(product)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    /** Cập nhật sản phẩm qua API PUT /api/products/{id} */
    suspend fun updateProduct(productId: Long, product: Product): Product? {
        return try {
            val response = apiService.updateProduct(productId, product)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    /** Xóa sản phẩm qua API DELETE /api/products/{id} */
    suspend fun deleteProduct(productId: Long): Boolean {
        return try {
            val response = apiService.deleteProduct(productId)
            response.success
        } catch (e: Exception) {
            false
        }
    }

    /** Tìm kiếm sản phẩm theo Barcode */
    suspend fun getProductByBarcode(barcode: String): Product? {
        return try {
            val response = apiService.getProductByBarcode(barcode)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null // Trả về null nếu không tìm thấy hoặc lỗi kết nối
        }
    }
}