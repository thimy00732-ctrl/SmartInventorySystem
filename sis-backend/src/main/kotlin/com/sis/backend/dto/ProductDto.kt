package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * Chứa dữ liệu đầu vào để tạo mới hoặc cập nhật sản phẩm.
 *
 * @property sku Mã SKU của sản phẩm.
 * @property barcode Mã vạch, có thể không được cung cấp.
 * @property productName Tên sản phẩm.
 * @property description Mô tả sản phẩm.
 * @property unit Đơn vị tính.
 * @property minStock Số lượng tồn kho tối thiểu.
 * @property categoryId Mã danh mục, có thể là null.
 * @property status Trạng thái sản phẩm.
 */
@Serializable
data class ProductRequest(
    val sku: String,
    val barcode: String? = null,
    val productName: String,
    val description: String? = null,
    val unit: String = "piece",
    val minStock: Double = 0.0,
    val categoryId: Long? = null,
    val status: String = "ACTIVE"
)