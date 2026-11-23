package com.sis.backend.entity

/**
 * Enum loại giao dịch (transaction_type).
 * Khớp với ENUM trong schema.sql.
 */
enum class TransactionType {
    IMPORT,      // Nhập hàng
    EXPORT,      // Xuất hàng
    TRANSFER,    // Chuyển kho
    STOCKTAKE,   // Kiểm kê
    ADJUSTMENT   // Điều chỉnh
}
