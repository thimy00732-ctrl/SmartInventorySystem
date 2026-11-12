package com.sis.backend.service

import com.sis.backend.dto.StockLedgerResponse
import com.sis.backend.exception.ValidationException
import com.sis.backend.repository.StockLedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Service xử lý nghiệp vụ sổ kho (UC36).
 * 
 * @property stockLedgerRepository Repository sổ kho.
 */
class StockLedgerService(
    private val stockLedgerRepository: StockLedgerRepository
) {
    
    /**
     * Lấy sổ kho với phân trang (UC36).
     * 
     * Validation:
     * - fromDate và toDate bắt buộc
     * - fromDate <= toDate
     * - Khoảng thời gian <= 366 ngày
     * 
     * @param fromDate Ngày bắt đầu (bắt buộc).
     * @param toDate Ngày kết thúc (bắt buộc).
     * @param warehouseId Lọc theo kho.
     * @param categoryId Lọc theo danh mục.
     * @param keyword Tìm kiếm theo SKU hoặc tên sản phẩm.
     * @param page Trang hiện tại.
     * @param size Số dòng mỗi trang.
     * @return Sổ kho với phân trang và tổng.
     * @throws ValidationException Nếu ngày không hợp lệ.
     */
    suspend fun getStockLedger(
        fromDate: LocalDate?,
        toDate: LocalDate?,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): StockLedgerResponse = withContext(Dispatchers.IO) {
        
        // Validation: dates required
        if (fromDate == null) {
            throw ValidationException("fromDate là bắt buộc")
        }
        if (toDate == null) {
            throw ValidationException("toDate là bắt buộc")
        }
        
        // Validation: fromDate <= toDate
        if (fromDate.isAfter(toDate)) {
            throw ValidationException("fromDate không được lớn hơn toDate")
        }
        
        // Validation: khoảng <= 366 ngày
        val daysBetween = ChronoUnit.DAYS.between(fromDate, toDate)
        if (daysBetween > 366) {
            throw ValidationException("Khoảng thời gian không được vượt quá 366 ngày")
        }
        
        // Lấy data với phân trang
        val (items, totalItems) = stockLedgerRepository.getStockLedger(
            fromDate = fromDate,
            toDate = toDate,
            warehouseId = warehouseId,
            categoryId = categoryId,
            keyword = keyword,
            page = page,
            size = size
        )
        
        // Lấy tổng (không phân trang)
        val total = stockLedgerRepository.getStockLedgerTotal(
            fromDate = fromDate,
            toDate = toDate,
            warehouseId = warehouseId,
            categoryId = categoryId,
            keyword = keyword
        )
        
        val totalPages = if (totalItems == 0L) {
            0
        } else {
            ((totalItems - 1) / size + 1).toInt()
        }
        
        StockLedgerResponse(
            items = items,
            total = total,
            note = "Tính từ các phiếu đã hoàn tất; tồn đầu kỳ suy ngược từ tồn hiện tại",
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages
        )
    }
}
