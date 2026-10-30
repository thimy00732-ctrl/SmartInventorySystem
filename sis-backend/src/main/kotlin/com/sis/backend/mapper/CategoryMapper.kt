
package com.sis.backend.mapper

import com.sis.backend.dto.CategoryResponse
import com.sis.backend.entity.Category

/**
 * Chuyển đổi dữ liệu danh mục giữa Entity và DTO.
 *
 * Mapper giúp tách biệt cấu trúc dữ liệu nội bộ
 * với cấu trúc dữ liệu trả về cho client.
 */
object CategoryMapper {

    /**
     * Chuyển đổi Category Entity thành CategoryResponse.
     *
     * @param category Entity danh mục cần chuyển đổi.
     * @return DTO chứa thông tin danh mục trả về cho client.
     */
    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            categoryId = requireNotNull(category.categoryId) {
                "ID danh mục không được null khi trả dữ liệu."
            },
            categoryName = category.categoryName,
            description = category.description,
            isActive = category.isActive,
            createdAt = category.createdAt,
            updatedAt = category.updatedAt
        )
    }

    /**
     * Chuyển đổi danh sách Entity thành danh sách DTO.
     *
     * @param categories Danh sách Entity danh mục.
     * @return Danh sách DTO danh mục.
     */
    fun toResponseList(
        categories: List<Category>
    ): List<CategoryResponse> {
        return categories.map(::toResponse)
    }
}
