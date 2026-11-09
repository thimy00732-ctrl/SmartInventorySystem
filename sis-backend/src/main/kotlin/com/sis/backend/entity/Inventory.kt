package com.sis.backend.entity

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entity đại diện cho tồn kho theo kho và lô hàng.
 *
 * @property inventoryId ID tồn kho (primary key).
 * @property warehouseId ID kho.
 * @property warehouseName Tên kho (từ JOIN).
 * @property productId ID sản phẩm.
 * @property productName Tên sản phẩm (từ JOIN).
 * @property sku Mã SKU sản phẩm (từ JOIN).
 * @property barcode Mã barcode sản phẩm (từ JOIN).
 * @property categoryId ID danh mục sản phẩm (từ JOIN).
 * @property categoryName Tên danh mục (từ JOIN).
 * @property supplierId ID nhà cung cấp (từ JOIN).
 * @property supplierName Tên nhà cung cấp (từ JOIN).
 * @property unit Đơn vị tính.
 * @property batchCode Mã lô hàng (rỗng nếu không quản lý theo lô).
 * @property manufactureDate Ngày sản xuất.
 * @property expiryDate Ngày hết hạn.
 * @property stockQuantity Số lượng tồn kho.
 * @property reservedQuantity Số lượng đã giữ chỗ.
 * @property availableQuantity Số lượng khả dụng (computed: stock - reserved).
 * @property updatedAt Thời điểm cập nhật cuối cùng.
 */
data class Inventory(
    val inventoryId: Long,
    val warehouseId: Long,
    val warehouseName: String,
    val productId: Long,
    val productName: String,
    val sku: String,
    val barcode: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val supplierId: Long?,
    val supplierName: String?,
    val unit: String,
    val batchCode: String,
    val manufactureDate: LocalDate?,
    val expiryDate: LocalDate?,
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val updatedAt: LocalDateTime
)
