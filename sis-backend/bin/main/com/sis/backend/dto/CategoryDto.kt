package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * DTO chứa dữ liệu yêu cầu tạo mới danh mục.
 *
 * @property categoryName Tên danh mục cần tạo.
 * @property description Mô tả chi tiết về danh mục (có thể để trống).
 */
@Serializable
data class CreateCategoryRequest(
    val categoryName: String,
    val description: String? = null
)

/**
 * DTO chứa dữ liệu yêu cầu cập nhật danh mục.
 *
 * @property categoryName Tên mới của danh mục.
 * @property description Mô tả mới của danh mục (có thể để trống).
 */
@Serializable
data class UpdateCategoryRequest(
    val categoryName: String,
    val description: String? = null
)

/**
 * DTO biểu diễn thông tin danh mục trả về cho client.
 *
 * @property categoryId Định danh duy nhất của danh mục.
 * @property categoryName Tên danh mục.
 * @property description Mô tả chi tiết về danh mục.
 * @property isActive Trạng thái hoạt động của danh mục.
 * @property createdAt Thời điểm tạo danh mục.
 * @property updatedAt Thời điểm cập nhật danh mục gần nhất.
 */
@Serializable
data class CategoryResponse(
    val categoryId: Long,
    val categoryName: String,
    val description: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
