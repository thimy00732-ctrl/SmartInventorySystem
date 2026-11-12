package com.sis.backend.controller

import com.sis.backend.dto.ProductThresholdResponse
import com.sis.backend.dto.UpdateThresholdRequest
import com.sis.backend.dto.UpdateThresholdResponse
import com.sis.backend.service.ThresholdService

/**
 * Controller xử lý các request liên quan đến ngưỡng tồn kho (UC33).
 * 
 * @property thresholdService Service xử lý nghiệp vụ ngưỡng.
 */
class ThresholdController(
    private val thresholdService: ThresholdService
) {
    
    /**
     * Lấy danh sách sản phẩm có thiết lập ngưỡng tồn kho (UC33 GET).
     * 
     * GET /api/products/thresholds?warehouseId={id}
     * 
     * @param warehouseId Lọc theo kho (null = tất cả kho).
     * @return Danh sách sản phẩm kèm alertLevel.
     */
    suspend fun getProductsWithThresholds(warehouseId: Long? = null): List<ProductThresholdResponse> {
        return thresholdService.getProductsWithThresholds(warehouseId)
    }
    
    /**
     * Cập nhật ngưỡng tồn kho cho sản phẩm (UC33 PATCH).
     * 
     * PATCH /api/products/{id}/thresholds
     * 
     * CHỈ ADMIN được gọi (RolePolicy.PRODUCT_THRESHOLDS_UPDATE).
     * 
     * @param productId Mã sản phẩm.
     * @param request Dữ liệu ngưỡng mới.
     * @return Thông tin ngưỡng sau khi cập nhật.
     */
    suspend fun updateThresholds(
        productId: Long,
        request: UpdateThresholdRequest
    ): UpdateThresholdResponse {
        return thresholdService.updateThresholds(productId, request)
    }
}
