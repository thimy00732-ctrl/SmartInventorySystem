package com.sis.android.data.repository

import com.sis.android.data.model.Product
import com.sis.android.data.remote.RetrofitClient

class ProductRepository {

    // Sử dụng ApiService đã cấu hình Retrofit
    private val apiService = RetrofitClient.apiService

    /** Lấy danh sách sản phẩm từ API /api/products (hỗ trợ tìm kiếm từ khóa q) */
    suspend fun getProducts(keyword: String? = null): List<Product> {
        return try {
            apiService.getProducts(keyword = keyword)
        } catch (e: Exception) {
            throw Exception("Lỗi kết nối Server: ${e.localizedMessage ?: "Không thể tải danh sách sản phẩm"}")
        }
    }

    /** Xem chi tiết sản phẩm theo ID từ API /api/products/{id} */
    suspend fun getProductById(productId: Long): Product? {
        return try {
            apiService.getProductById(productId)
        } catch (e: Exception) {
            null
        }
    }

    /** Tạo sản phẩm mới qua API POST /api/products */
    suspend fun createProduct(product: Product): Product {
        return apiService.createProduct(product)
    }

    /** Cập nhật sản phẩm qua API PUT /api/products/{id} */
    suspend fun updateProduct(productId: Long, product: Product): Product {
        return apiService.updateProduct(productId, product)
    }

    /** Xóa sản phẩm qua API DELETE /api/products/{id} */
    suspend fun deleteProduct(productId: Long) {
        apiService.deleteProduct(productId)
    }

    // Thêm vào trong class ProductRepository
    suspend fun getProductByBarcode(barcode: String): Product? {
        return try {
            RetrofitClient.apiService.getProductByBarcode(barcode)
        } catch (e: Exception) {
            null // Trả về null nếu không tìm thấy hoặc lỗi kết nối
        }
    }
}