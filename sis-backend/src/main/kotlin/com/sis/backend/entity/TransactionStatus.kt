package com.sis.backend.entity

/**
 * Enum trạng thái giao dịch (status).
 * Khớp với ENUM trong schema.sql.
 */
enum class TransactionStatus {
    DRAFT,       // Nháp
    PENDING,     // Chờ duyệt
    APPROVED,    // Đã duyệt
    REJECTED,    // Từ chối
    COMPLETED,   // Hoàn tất (ảnh hưởng tồn kho)
    CANCELLED    // Hủy
}
