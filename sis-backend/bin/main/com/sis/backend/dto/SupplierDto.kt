package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * Dữ liệu gửi lên khi tạo nhà cung cấp.
 */
@Serializable
data class CreateSupplierRequest(
    val supplierName: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

/**
 * Dữ liệu gửi lên khi cập nhật nhà cung cấp.
 */
@Serializable
data class UpdateSupplierRequest(
    val supplierName: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

/**
 * Thông tin cơ bản của nhà cung cấp.
 */
@Serializable
data class SupplierResponse(
    val supplierId: Long,
    val supplierName: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Thông tin sản phẩm thuộc nhà cung cấp.
 *
 * Các trường tương ứng với bảng products.
 */
@Serializable
data class SupplierProductResponse(
    val productId: Long,
    val sku: String,
    val barcode: String?,
    val productName: String,
    val imageUrl: String?,
    val categoryId: Long?,
    val supplierId: Long?,
    val unit: String,
    val costPrice: Double,
    val salePrice: Double,
    val minStock: Int,
    val maxStock: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Chi tiết một sản phẩm trong giao dịch nhập hàng.
 *
 * Các trường tương ứng với bảng transaction_details.
 */
@Serializable
data class SupplierTransactionDetailResponse(
    val detailId: Long,
    val transactionId: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val batchCode: String,
    val manufactureDate: String?,
    val expiryDate: String?,
    val quantity: Int,
    val unitPrice: Double,
    val systemQuantity: Int?,
    val actualQuantity: Int?,
    val differenceQuantity: Int?,
    val note: String?
)

/**
 * Lịch sử một giao dịch nhập hàng của nhà cung cấp.
 *
 * Các trường tương ứng với bảng transactions.
 */
@Serializable
data class SupplierTransactionResponse(
    val transactionId: Long,
    val warehouseId: Long,
    val targetWarehouseId: Long?,
    val supplierId: Long?,
    val userId: Long,
    val approvedBy: Long?,
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
    val details: List<SupplierTransactionDetailResponse>
)

/**
 * Thông tin chi tiết nhà cung cấp.
 *
 * Bao gồm thông tin nhà cung cấp, danh sách sản phẩm
 * và lịch sử giao dịch nhập hàng.
 */
@Serializable
data class SupplierDetailResponse(
    val supplierId: Long,
    val supplierName: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val products: List<SupplierProductResponse>,
    val transactions: List<SupplierTransactionResponse>
)