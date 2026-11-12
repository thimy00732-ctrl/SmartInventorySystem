package com.sis.backend.controller

import com.sis.backend.dto.PageResult
import com.sis.backend.dto.TransactionDetailResponse
import com.sis.backend.dto.TransactionMovementResponse
import com.sis.backend.service.TransactionService
import java.time.LocalDate

/**
 * Controller xử lý các request liên quan đến transactions (UC37, UC38).
 * 
 * @property transactionService Service xử lý nghiệp vụ transactions.
 */
class TransactionController(
    private val transactionService: TransactionService
) {
    
    /**
     * Lấy lịch sử biến động với phân trang (UC37).
     * 
     * GET /api/transactions/movements?productId&warehouseId&type&status&userId&fromDate&toDate&sortBy&sortDir&page&size
     * 
     * @return Danh sách movements với phân trang.
     */
    suspend fun getTransactionMovements(
        productId: Long? = null,
        warehouseId: Long? = null,
        type: String? = null,
        status: String? = null,
        userId: Long? = null,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
        sortBy: String = "txTime",
        sortDir: String = "DESC",
        page: Int = 1,
        size: Int = 10
    ): PageResult<TransactionMovementResponse> {
        return transactionService.getTransactionMovements(
            productId, warehouseId, type, status, userId, 
            fromDate, toDate, sortBy, sortDir, page, size
        )
    }
    
    /**
     * Lấy chi tiết giao dịch theo ID (UC38).
     * 
     * GET /api/transactions/{id}
     * 
     * @param transactionId ID giao dịch.
     * @return Chi tiết giao dịch.
     */
    suspend fun getTransactionDetail(transactionId: Long): TransactionDetailResponse {
        return transactionService.getTransactionDetail(transactionId)
    }
}
