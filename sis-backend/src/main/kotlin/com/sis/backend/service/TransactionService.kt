package com.sis.backend.service

import com.sis.backend.dto.PageResult
import com.sis.backend.dto.TransactionDetailResponse
import com.sis.backend.dto.TransactionMovementResponse
import com.sis.backend.exception.ResourceNotFoundException
import com.sis.backend.exception.ValidationException
import com.sis.backend.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Service xử lý nghiệp vụ transactions (UC37, UC38).
 * 
 * @property transactionRepository Repository transactions.
 */
class TransactionService(
    private val transactionRepository: TransactionRepository
) {
    
    private val validTransactionTypes = setOf(
        "IMPORT", "EXPORT", "TRANSFER", "STOCKTAKE", "ADJUSTMENT"
    )
    
    private val validStatuses = setOf(
        "DRAFT", "PENDING", "APPROVED", "COMPLETED", "REJECTED", "CANCELLED"
    )
    
    /**
     * Lấy lịch sử biến động với phân trang (UC37).
     * 
     * Validation:
     * - type phải thuộc 5 giá trị TransactionType
     * - status phải thuộc 6 giá trị TransactionStatus
     * - fromDate <= toDate nếu cả 2 có giá trị
     * 
     * @param productId Lọc theo sản phẩm.
     * @param warehouseId Lọc theo kho.
     * @param type Transaction type.
     * @param status Transaction status.
     * @param userId Lọc theo người tạo hoặc duyệt.
     * @param fromDate Ngày bắt đầu.
     * @param toDate Ngày kết thúc.
     * @param sortBy Cột sắp xếp.
     * @param sortDir Chiều sắp xếp.
     * @param page Trang hiện tại.
     * @param size Số dòng mỗi trang.
     * @return Danh sách movements với phân trang.
     * @throws ValidationException Nếu tham số không hợp lệ.
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
    ): PageResult<TransactionMovementResponse> = withContext(Dispatchers.IO) {
        
        // Validation: type
        type?.let {
            if (it !in validTransactionTypes) {
                throw ValidationException(
                    "type phải là một trong: ${validTransactionTypes.joinToString(", ")}"
                )
            }
        }
        
        // Validation: status
        status?.let {
            if (it !in validStatuses) {
                throw ValidationException(
                    "status phải là một trong: ${validStatuses.joinToString(", ")}"
                )
            }
        }
        
        // Validation: fromDate <= toDate
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw ValidationException("fromDate không được lớn hơn toDate")
        }
        
        val (items, totalItems) = transactionRepository.getTransactionMovements(
            productId = productId,
            warehouseId = warehouseId,
            type = type,
            status = status,
            userId = userId,
            fromDate = fromDate,
            toDate = toDate,
            sortBy = sortBy,
            sortDir = sortDir,
            page = page,
            size = size
        )
        
        val totalPages = if (totalItems == 0L) {
            0
        } else {
            ((totalItems - 1) / size + 1).toInt()
        }
        
        PageResult(
            items = items,
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages
        )
    }
    
    /**
     * Lấy chi tiết giao dịch theo ID (UC38).
     * 
     * @param transactionId ID giao dịch.
     * @return Chi tiết giao dịch.
     * @throws ResourceNotFoundException Nếu không tồn tại.
     */
    suspend fun getTransactionDetail(transactionId: Long): TransactionDetailResponse = withContext(Dispatchers.IO) {
        transactionRepository.getTransactionDetail(transactionId)
            ?: throw ResourceNotFoundException("Transaction", transactionId)
    }
}
