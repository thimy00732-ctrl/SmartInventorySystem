package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * Dữ liệu yêu cầu dùng để tạo mới hoặc cập nhật một danh mục.
 *
 * @property categoryName Tên của danh mục.
 * @property description Mô tả chi tiết về danh mục (có thể để trống).
 * @property status Trạng thái hoạt động của danh mục (mặc định là "ACTIVE").
 */
@Serializable
data class CategoryRequest(
    val categoryName: String,
    val description: String? = null,
    val status: String = "ACTIVE"
)

/**
 * Dữ liệu phản hồi chứa thông tin chi tiết của một danh mục.
 *
 * @property categoryId Định danh duy nhất của danh mục.
 * @property categoryName Tên của danh mục.
 * @property description Mô tả chi tiết về danh mục.
 * @property status Trạng thái hoạt động của danh mục.
 * @property createdAt Thời điểm khởi tạo danh mục dưới dạng chuỗi.
 * @property updatedAt Thời điểm cập nhật thông tin danh mục gần nhất dưới dạng chuỗi.
 */
@Serializable
data class CategoryResponse(
    val categoryId: Long,
    val categoryName: String,
    val description: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)