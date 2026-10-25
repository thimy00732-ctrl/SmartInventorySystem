
package com.sis.backend.controller

import com.sis.backend.dto.CategoryRequest
import com.sis.backend.entity.Category
import com.sis.backend.service.CategoryService

/**
 * Controller quản lý các yêu cầu liên quan đến danh mục.
 *
 * @property service Dịch vụ xử lý logic nghiệp vụ của danh mục.
 */
class CategoryController(
    private val service: CategoryService
) {

    /**
     * Lấy danh sách toàn bộ các danh mục hiện có.
     *
     * @return Danh sách các [Category].
     */
    fun getAll(): List<Category> {
        return service.getAll()
    }

    /**
     * Lấy thông tin chi tiết của một danh mục dựa trên định danh (ID).
     *
     * @param id Định danh duy nhất của danh mục cần tìm.
     * @return Đối tượng [Category] tương ứng.
     */
    fun getById(id: Long): Category {
        return service.getById(id)
    }

    /**
     * Tạo mới một danh mục dựa trên thông tin yêu cầu được cung cấp.
     *
     * @param request Dữ liệu yêu cầu chứa thông tin khởi tạo danh mục ([CategoryRequest]).
     * @return Đối tượng [Category] sau khi đã được tạo thành công.
     */
    fun create(request: CategoryRequest): Category {
        return service.create(request)
    }

    /**
     * Tạo mới một danh mục dựa trên thông tin yêu cầu được cung cấp.
     *
     * @param request Dữ liệu yêu cầu chứa thông tin khởi tạo danh mục ([CategoryRequest]).
     * @return Đối tượng [Category] sau khi đã được tạo thành công.
     */
    fun update(id: Long, request: CategoryRequest): Category {
        return service.update(id, request)
    }

    /**
     * Xóa một danh mục khỏi hệ thống dựa trên ID.
     *
     * @param id Định danh duy nhất của danh mục cần xóa.
     */
    fun delete(id: Long) {
        service.delete(id)
    }
}
