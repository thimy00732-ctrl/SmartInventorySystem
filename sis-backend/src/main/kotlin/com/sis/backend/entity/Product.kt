package com.sis.backend.entity

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Đại diện cho thông tin một sản phẩm trong hệ thống.
 *
 * @property productId Mã định danh sản phẩm.
 * @property sku Mã SKU duy nhất của sản phẩm.
 * @property barcode Mã vạch của sản phẩm.
 * @property productName Tên sản phẩm.
 * @property imageUrl Đường dẫn hình ảnh sản phẩm.
 * @property categoryId Mã danh mục sản phẩm.
 * @property supplierId Mã nhà cung cấp.
 * @property unit Đơn vị tính.
 * @property costPrice Giá vốn.
 * @property salePrice Giá bán.
 * @property minStock Ngưỡng tồn kho tối thiểu.
 * @property maxStock Ngưỡng tồn kho tối đa.
 * @property isActive Trạng thái hoạt động.
 * @property createdAt Thời điểm tạo sản phẩm.
 * @property updatedAt Thời điểm cập nhật sản phẩm gần nhất.
 */
data class Product(
    val productId: Long,
    val sku: String,
    val barcode: String?,
    val productName: String,
    val imageUrl: String?,
    val categoryId: Long?,
    val supplierId: Long?,
    val unit: String,
    val costPrice: BigDecimal,
    val salePrice: BigDecimal,
    val minStock: Int,
    val maxStock: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
