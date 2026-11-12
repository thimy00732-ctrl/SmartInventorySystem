package com.sis.backend.service

import com.sis.backend.dto.ProductThresholdResponse
import com.sis.backend.dto.UpdateThresholdRequest
import com.sis.backend.dto.UpdateThresholdResponse
import com.sis.backend.exception.InvalidThresholdException
import com.sis.backend.exception.ResourceNotFoundException
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.ThresholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service xử lý nghiệp vụ liên quan đến ngưỡng tồn kho (UC33).
 * 
 * @property productRepository Repository sản phẩm.
 * @property thresholdRepository Repository ngưỡng.
 */
class ThresholdService(
    private val productRepository: ProductRepository,
    private val thresholdRepository: ThresholdRepository
) {
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    
    /**
     * Lấy danh sách sản phẩm có thiết lập ngưỡng tồn kho (UC33 GET).
     * 
     * @param warehouseId Lọc theo kho (null = tất cả kho).
     * @return Danh sách sản phẩm kèm alertLevel.
     */
    suspend fun getProductsWithThresholds(warehouseId: Long? = null): List<ProductThresholdResponse> {
        return withContext(Dispatchers.IO) {
            thresholdRepository.findProductsWithThresholds(warehouseId)
        }
    }
    
    /**
     * Cập nhật ngưỡng tồn kho cho sản phẩm (UC33 PATCH).
     * 
     * Validation:
     * - minStock >= 0
     * - maxStock >= minStock
     * - Sản phẩm phải tồn tại
     * 
     * CHỈ ADMIN được gọi (RolePolicy.PRODUCT_THRESHOLDS_UPDATE).
     * 
     * @param productId Mã sản phẩm.
     * @param request Dữ liệu ngưỡng mới.
     * @return Thông tin ngưỡng sau khi cập nhật.
     * @throws ResourceNotFoundException Nếu sản phẩm không tồn tại.
     * @throws InvalidThresholdException Nếu ngưỡng không hợp lệ.
     */
    suspend fun updateThresholds(
        productId: Long,
        request: UpdateThresholdRequest
    ): UpdateThresholdResponse = withContext(Dispatchers.IO) {
        
        // Validation: minStock >= 0
        if (request.minStock < 0) {
            throw InvalidThresholdException("Ngưỡng tối thiểu không được âm.")
        }
        
        // Validation: maxStock >= minStock
        if (request.maxStock < request.minStock) {
            throw InvalidThresholdException("Ngưỡng tối đa phải lớn hơn hoặc bằng ngưỡng tối thiểu.")
        }
        
        // Kiểm tra sản phẩm tồn tại
        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Sản phẩm", productId)
        
        // Cập nhật ngưỡng
        val success = productRepository.updateThresholds(
            productId = productId,
            minStock = request.minStock,
            maxStock = request.maxStock
        )
        
        if (!success) {
            throw ResourceNotFoundException("Sản phẩm", productId)
        }
        
        // Trả về response
        UpdateThresholdResponse(
            productId = product.productId,
            sku = product.sku,
            productName = product.productName,
            minStock = request.minStock,
            maxStock = request.maxStock,
            updatedAt = LocalDateTime.now().format(dateTimeFormatter)
        )
    }
}
