package com.sis.backend.service

import com.sis.backend.dto.ValuationDetailResponse
import com.sis.backend.dto.ValuationSummaryResponse
import com.sis.backend.repository.ValuationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service xử lý nghiệp vụ giá trị tồn kho (UC39).
 * 
 * CHỈ ADMIN được gọi (RolePolicy.INVENTORY_VALUATION).
 * 
 * @property valuationRepository Repository valuation.
 */
class ValuationService(
    private val valuationRepository: ValuationRepository
) {
    
    /**
     * Lấy tổng quan giá trị tồn kho (UC39 summary).
     * 
     * @param warehouseId Không dùng cho summary (chỉ dùng cho detail).
     * @param categoryId Lọc theo danh mục (null = tất cả).
     * @return Tổng quan giá trị.
     */
    suspend fun getSummary(
        warehouseId: Long? = null,
        categoryId: Long? = null
    ): ValuationSummaryResponse {
        return withContext(Dispatchers.IO) {
            // Spec: summary KHÔNG lọc warehouseId, chỉ lọc categoryId
            valuationRepository.getSummary(categoryId)
        }
    }
    
    /**
     * Lấy chi tiết giá trị tồn kho với phân trang (UC39 detail).
     * 
     * @param warehouseId Lọc theo kho (null = tất cả).
     * @param categoryId Lọc theo danh mục (null = tất cả).
     * @param keyword Tìm kiếm theo SKU hoặc tên sản phẩm.
     * @param page Trang hiện tại (bắt đầu từ 1).
     * @param size Số dòng mỗi trang.
     * @return Chi tiết giá trị kèm tổng và note.
     */
    suspend fun getDetail(
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): ValuationDetailResponse = withContext(Dispatchers.IO) {
        
        // Lấy summary để có total (không đổi khi chuyển trang)
        val summary = valuationRepository.getSummary(categoryId)
        
        // Lấy items với phân trang
        val (items, totalItems) = valuationRepository.getDetail(
            warehouseId = warehouseId,
            categoryId = categoryId,
            keyword = keyword,
            page = page,
            size = size
        )
        
        val totalPages = if (totalItems == 0L) {
            0
        } else {
            ((totalItems - 1) / size + 1).toInt()
        }
        
        ValuationDetailResponse(
            items = items,
            total = summary.totalValue,
            note = "Tính theo giá vốn hiện tại, không phản ánh giá vốn lịch sử",
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages
        )
    }
}
