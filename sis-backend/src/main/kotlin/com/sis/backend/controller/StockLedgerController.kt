package com.sis.backend.controller

import com.sis.backend.dto.StockLedgerResponse
import com.sis.backend.service.StockLedgerService
import java.time.LocalDate

/**
 * Controller xử lý các request liên quan đến sổ kho (UC36).
 * 
 * @property stockLedgerService Service xử lý nghiệp vụ sổ kho.
 */
class StockLedgerController(
    private val stockLedgerService: StockLedgerService
) {
    
    /**
     * Lấy sổ kho với phân trang (UC36).
     * 
     * GET /api/stock-ledger?fromDate&toDate&warehouseId&categoryId&keyword&page&size
     * 
     * @param fromDate Ngày bắt đầu (yyyy-MM-dd, bắt buộc).
     * @param toDate Ngày kết thúc (yyyy-MM-dd, bắt buộc).
     * @param warehouseId Lọc theo kho.
     * @param categoryId Lọc theo danh mục.
     * @param keyword Tìm kiếm theo SKU hoặc tên sản phẩm.
     * @param page Trang hiện tại.
     * @param size Số dòng mỗi trang.
     * @return Sổ kho với phân trang.
     */
    suspend fun getStockLedger(
        fromDate: LocalDate?,
        toDate: LocalDate?,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): StockLedgerResponse {
        return stockLedgerService.getStockLedger(
            fromDate, toDate, warehouseId, categoryId, keyword, page, size
        )
    }
}
