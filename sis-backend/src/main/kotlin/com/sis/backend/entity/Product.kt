package com.sis.backend.entity

import kotlinx.serialization.Serializable

/**
 * Đại diện cho một sản phẩm được lưu trong hệ thống quản lý kho.
 *
 * @property productId Mã định danh duy nhất của sản phẩm.
 * @property sku Mã SKU duy nhất của sản phẩm.
 * @property barcode Mã vạch của sản phẩm, có thể không có.
 * @property productName Tên sản phẩm.
 * @property description Mô tả sản phẩm, có thể không có.
 * @property unit Đơn vị tính của sản phẩm.
 * @property minStock Số lượng tồn kho tối thiểu.
 * @property categoryId Mã danh mục, có thể không thuộc danh mục nào.
 * @property status Trạng thái của sản phẩm.
 * @property createdBy Mã người tạo sản phẩm, có thể không có.
 * @property createdAt Thời điểm tạo sản phẩm.
 * @property updatedAt Thời điểm cập nhật sản phẩm gần nhất.
 */
@Serializable
data class Product(
    val productId: Long,
    val sku: String,
    val barcode: String?,
    val productName: String,
    val description: String?,
    val unit: String,
    val minStock: Double,
    val categoryId: Long?,
    val status: String,
    val createdBy: Long?,
    val createdAt: String,
    val updatedAt: String
)