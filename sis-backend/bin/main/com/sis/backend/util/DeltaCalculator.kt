package com.sis.backend.util

import java.math.BigDecimal

/**
 * Hàm CHUNG tính delta cho UC36 (Sổ kho) và UC37 (Lịch sử biến động).
 * 
 * Quy tắc delta theo spec:
 * - IMPORT: +quantity
 * - EXPORT: -quantity
 * - TRANSFER (kho nguồn): -quantity
 * - TRANSFER (kho đích): +quantity
 * - STOCKTAKE: COALESCE(difference_quantity, 0)
 * - ADJUSTMENT + SURPLUS: +quantity
 * - ADJUSTMENT + SHORTAGE/DAMAGE: -quantity
 * - ADJUSTMENT + OTHER: COALESCE(difference_quantity, 0)
 */
object DeltaCalculator {
    
    /**
     * Tính delta cho một dòng transaction_detail.
     * 
     * @param transactionType Loại giao dịch (IMPORT, EXPORT, TRANSFER, STOCKTAKE, ADJUSTMENT).
     * @param adjustmentReason Lý do điều chỉnh (SURPLUS, SHORTAGE, DAMAGE, OTHER).
     * @param quantity Số lượng trong transaction_details.
     * @param differenceQuantity Chênh lệch (dùng cho STOCKTAKE và ADJUSTMENT OTHER).
     * @param isSource true nếu là kho nguồn (dùng cho TRANSFER).
     * @return Delta value (âm = giảm, dương = tăng).
     */
    fun calculate(
        transactionType: String,
        adjustmentReason: String?,
        quantity: BigDecimal?,
        differenceQuantity: BigDecimal?,
        isSource: Boolean = true
    ): BigDecimal {
        return when (transactionType) {
            "IMPORT" -> quantity ?: BigDecimal.ZERO
            
            "EXPORT" -> quantity?.negate() ?: BigDecimal.ZERO
            
            "TRANSFER" -> if (isSource) {
                quantity?.negate() ?: BigDecimal.ZERO
            } else {
                quantity ?: BigDecimal.ZERO
            }
            
            "STOCKTAKE" -> differenceQuantity ?: BigDecimal.ZERO
            
            "ADJUSTMENT" -> when (adjustmentReason) {
                "SURPLUS" -> quantity ?: BigDecimal.ZERO
                "SHORTAGE", "DAMAGE" -> quantity?.negate() ?: BigDecimal.ZERO
                "OTHER" -> differenceQuantity ?: BigDecimal.ZERO
                else -> BigDecimal.ZERO
            }
            
            else -> BigDecimal.ZERO
        }
    }
    
    /**
     * Lấy movement kind cho UC36/UC37.
     * 
     * @param transactionType Loại giao dịch.
     * @param adjustmentReason Lý do điều chỉnh.
     * @param isSource true nếu là kho nguồn.
     * @return Movement kind.
     */
    fun getMovementKind(
        transactionType: String,
        adjustmentReason: String?,
        isSource: Boolean = true
    ): String {
        return when (transactionType) {
            "IMPORT" -> "NHẬP"
            "EXPORT" -> "XUẤT"
            "TRANSFER" -> if (isSource) "CHUYỂN XUẤT" else "CHUYỂN NHẬP"
            "STOCKTAKE" -> "KIỂM KÊ"
            "ADJUSTMENT" -> "ĐIỀU CHỈNH"
            else -> "UNKNOWN"
        }
    }
    
    /**
     * Tạo movement label cho UC37.
     * 
     * @param transactionType Loại giao dịch.
     * @param adjustmentReason Lý do điều chỉnh.
     * @return Label mô tả biến động.
     */
    fun getMovementLabel(
        transactionType: String,
        adjustmentReason: String?
    ): String {
        return when (transactionType) {
            "IMPORT" -> "Nhập hàng"
            "EXPORT" -> "Xuất hàng"
            "TRANSFER" -> "Chuyển kho"
            "STOCKTAKE" -> "Kiểm kê"
            "ADJUSTMENT" -> when (adjustmentReason) {
                "SURPLUS" -> "Thừa hàng"
                "SHORTAGE" -> "Thiếu hàng"
                "DAMAGE" -> "Hư hỏng"
                "OTHER" -> "Khác"
                else -> "Điều chỉnh"
            }
            else -> "Không xác định"
        }
    }
}
