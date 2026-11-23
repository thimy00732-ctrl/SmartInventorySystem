package com.sis.backend.dto

import com.sis.backend.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTO cho UC36: Sổ kho - dòng chi tiết.
 */
@Serializable
data class StockLedgerItemResponse(
    val warehouseId: Long,
    val warehouseName: String,
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    @Serializable(with = BigDecimalSerializer::class)
    val openingQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val importQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val exportQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val transferInQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val transferOutQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val adjustNet: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val closingQty: BigDecimal
)

/**
 * DTO cho UC36: Sổ kho - tổng.
 */
@Serializable
data class StockLedgerTotalResponse(
    @Serializable(with = BigDecimalSerializer::class)
    val openingQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val importQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val exportQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val transferInQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val transferOutQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val adjustNet: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val closingQty: BigDecimal
)

/**
 * DTO cho UC36: Sổ kho - response với phân trang.
 */
@Serializable
data class StockLedgerResponse(
    val items: List<StockLedgerItemResponse>,
    val total: StockLedgerTotalResponse,
    val note: String = "Tính từ các phiếu đã hoàn tất; tồn đầu kỳ suy ngược từ tồn hiện tại",
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)

/**
 * DTO cho UC37: Lịch sử biến động - dòng chi tiết.
 */
@Serializable
data class TransactionMovementResponse(
    val transactionId: Long,
    val transactionType: String,
    val transactionSubtype: String?,
    val status: String,
    val txTime: String, // yyyy-MM-dd'T'HH:mm:ss
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    val batchCode: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val differenceQuantity: BigDecimal?,
    val warehouseId: Long,
    val warehouseName: String,
    val targetWarehouseId: Long?,
    val targetWarehouseName: String?,
    val createdById: Long?,
    val createdByName: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val delta: BigDecimal,
    val movementLabel: String,
    val affectsStock: Boolean // true nếu status = COMPLETED
)

/**
 * DTO cho UC38: Truy vết giao dịch - thông tin phiếu.
 */
@Serializable
data class TransactionDetailResponse(
    val transactionId: Long,
    val transactionType: String,
    val transactionSubtype: String?,
    val status: String,
    val note: String?,
    val rejectionReason: String?,
    val createdAt: String,
    val submittedAt: String?,
    val approvedAt: String?,
    val completedAt: String?,
    val cancelledAt: String?,
    val warehouseId: Long,
    val warehouseName: String,
    val targetWarehouseId: Long?,
    val targetWarehouseName: String?,
    val supplierId: Long?,
    val supplierName: String?,
    val createdById: Long?,
    val createdByName: String?,
    val approvedById: Long?,
    val approvedByName: String?,
    val details: List<TransactionDetailItemResponse>,
    val summary: TransactionSummaryResponse
)

/**
 * DTO cho UC38: Chi tiết dòng sản phẩm trong phiếu.
 */
@Serializable
data class TransactionDetailItemResponse(
    val detailId: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    val batchCode: String?,
    val manufactureDate: String?,
    val expiryDate: String?,
    val warehouseId: Long,
    val warehouseName: String,
    val targetWarehouseId: Long?,
    val targetWarehouseName: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val lineValue: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val systemQuantity: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val actualQuantity: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val differenceQuantity: BigDecimal?,
    val note: String?
)

/**
 * DTO cho UC38: Tổng quan phiếu.
 */
@Serializable
data class TransactionSummaryResponse(
    val totalLines: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val totalQuantity: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val totalValue: BigDecimal? // Chỉ IMPORT/EXPORT, loại khác null
)
