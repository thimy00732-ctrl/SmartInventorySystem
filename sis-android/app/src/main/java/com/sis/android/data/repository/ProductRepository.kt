package com.sis.android.data.repository

import com.sis.android.data.model.Product
import com.sis.android.data.remote.RetrofitClient
import com.sis.android.data.model.CreateProductRequest
import com.sis.android.data.model.UpdateProductRequest

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
            val request = CreateProductRequest(
                sku = product.sku.takeIf { it.isNotBlank() },
                barcode = product.barcode?.takeIf { it.isNotBlank() },
                productName = product.productName,
                imageUrl = product.imageUrl,
                categoryId = product.categoryId,
                supplierId = product.supplierId,
                unit = product.unit,
                costPrice = product.costPrice.toString(),
                salePrice = product.salePrice.toString(),
                minStock = product.minStock,
                maxStock = product.maxStock,

            )

            // Gọi API
            val response = apiService.createProduct(request)
            response

        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("DEBUG_ERROR", "❌ Lỗi HTTP ${e.code()}: $errorBody")
            e.printStackTrace()
            null

        } catch (e: com.google.gson.JsonSyntaxException) {
            android.util.Log.e("DEBUG_ERROR", "❌ Lỗi Parse JSON (Lệch kiểu dữ liệu): ${e.message}")
            e.printStackTrace()
            null

        } catch (e: Exception) {
            android.util.Log.e("DEBUG_ERROR", "❌ Lỗi khác: ${e.javaClass.name} - ${e.localizedMessage}", e)
            e.printStackTrace()
            null
        }
    }

    /** Cập nhật sản phẩm qua API PUT /api/products/{id} */
    /** Cập nhật sản phẩm qua API PUT /api/products/{id} */
    /** Cập nhật sản phẩm qua API PUT /api/products/{id} */
    suspend fun updateProduct(productId: Long, product: Product): Product? {
        return try {
            val request = UpdateProductRequest(
                sku = product.sku,
                barcode = product.barcode,
                productName = product.productName,
                imageUrl = product.imageUrl,
                categoryId = product.categoryId,
                supplierId = product.supplierId,
                unit = product.unit,
                costPrice = product.costPrice.toString(),
                salePrice = product.salePrice.toString(),
                minStock = product.minStock,
                maxStock = product.maxStock
            )

            val response = apiService.updateProduct(productId, request)

            // Map từ ProductResponse sang Product (chỉ dùng các thuộc tính có sẵn)
            Product(
                productId = response.productId,
                sku = response.sku,
                barcode = response.barcode,
                productName = response.productName,
                imageUrl = response.imageUrl,
                categoryId = response.categoryId,
                supplierId = response.supplierId,
                unit = response.unit,
                costPrice = response.costPrice.toDoubleOrNull() ?: 0.0,
                salePrice = response.salePrice.toDoubleOrNull() ?: 0.0,
                minStock = response.minStock,
                maxStock = response.maxStock,
                stockQuantity = response.stockQuantity,
                reservedQuantity = response.reservedQuantity,
                isActive = response.isActive
                // Các thuộc tính availableQuantity, createdAt, updatedAt sẽ tự động được tính toán hoặc bỏ qua vì không nằm trong constructor chính
            )
        } catch (e: retrofit2.HttpException) {
            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (_: Exception) {
                "Không thể đọc error body"
            }
            android.util.Log.e("DEBUG_ERROR", "❌ Lỗi HTTP ${e.code()} chi tiết từ Server: $errorBody")
            null
        } catch (e: Exception) {
            android.util.Log.e("ProductRepo", "❌ Error updating product: ${e.message}", e)
            null
        }
    }

    /** Xóa sản phẩm (Backend sẽ kiểm tra điều kiện đã ngừng kinh doanh chưa) */

    suspend fun deleteProduct(productId: Long): Boolean {
        return try {
            apiService.deleteProduct(productId)
            true // Nếu không throw exception nghĩa là xóa thành công trên server
        } catch (e: Exception) {
            val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
            android.util.Log.e("ProductRepo", "❌ Lỗi xóa sản phẩm: ${e.message} | Body: $errorBody", e)
            false
        }
    }

    /** Tìm kiếm sản phẩm theo Barcode */
    suspend fun getProductByBarcode(barcode: String): Product? {
        return try {
            val response = apiService.getProductByBarcode(barcode)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    /** Ngừng kinh doanh sản phẩm qua API */
    suspend fun deactivateProduct(productId: Long): Boolean {
        return try {
            apiService.deactivateProduct(productId)
            true
        } catch (e: Exception) {
            val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
            android.util.Log.e("ProductRepo", "❌ Lỗi ngừng kinh doanh: ${e.message} | Body: $errorBody", e)
            false
        }
    }

    /** Kích hoạt lại sản phẩm */
    suspend fun reactivateProduct(productId: Long): Boolean {
        return try {
            apiService.reactivateProduct(productId)
            true
        } catch (e: Exception) {
            android.util.Log.e("ProductRepo", "❌ Error reactivating product: ${e.message}", e)
            false
        }
    }
}