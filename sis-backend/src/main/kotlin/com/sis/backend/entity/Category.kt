package com.sis.backend.entity

import kotlinx.serialization.Serializable

/**
 * Thực thể biểu diễn thông tin danh mục trong hệ thống.
 *
 * @property categoryId Định danh duy nhất của danh mục.
 * @property categoryName Tên của danh mục.
 * @property description Mô tả chi tiết về danh mục (có thể để trống).
 * @property isActive Trạng thái hoạt động của danh mục.
 * @property createdAt Thời điểm khởi tạo danh mục dưới dạng chuỗi.
 * @property updatedAt Thời điểm cập nhật thông tin danh mục gần nhất dưới dạng chuỗi.
 */
@Serializable
data class Category(
    val categoryId: Long? = null,
    val categoryName: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
