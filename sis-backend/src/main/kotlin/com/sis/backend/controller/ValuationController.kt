package com.sis.backend.controller

import com.sis.backend.dto.ValuationDetailResponse
import com.sis.backend.dto.ValuationSummaryResponse
import com.sis.backend.service.ValuationService

/**
 * Controller xử lý các request liên quan đến giá trị tồn kho (UC39).
 * 
 * CHỈ ADMIN được gọi.
 * 
 * @property valuationService Service xử lý nghiệp vụ valuation.
 */
class ValuationController(
    private val valuationService: ValuationService
) {
    
    /**
     * Lấy tổng quan giá trị tồn kho (UC39 summary).
     * 
     * GET /api/inventory/valuation/summary?warehouseId&categoryId
     * 
     * Note: warehouseId không dùng cho summary, chỉ lọc categoryId.
     * 
     * @param warehouseId Không dùng (spec yêu cầu chỉ lọc categoryId).
     * @param categoryId Lọc theo danh mục.
     * @return Tổng quan giá trị.
     */
    suspend fun getSummary(
        warehouseId: Long? = null,
        categoryId: Long? = null
    ): ValuationSummaryResponse {
        return valuationService.getSummary(warehouseId, categoryId)
    }
    
    /**
     * Lấy chi tiết giá trị tồn kho với phân trang (UC39 detail).
     * 
     * GET /api/inventory/valuation?warehouseId&categoryId&keyword&page&size
     * 
     * @param warehouseId Lọc theo kho.
     * @param categoryId Lọc theo danh mục.
     * @param keyword Tìm kiếm theo SKU hoặc tên sản phẩm.
     * @param page Trang hiện tại.
     * @param size Số dòng mỗi trang.
     * @return Chi tiết giá trị kèm tổng.
     */
    suspend fun getDetail(
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): ValuationDetailResponse {
        return valuationService.getDetail(warehouseId, categoryId, keyword, page, size)
    }
}
