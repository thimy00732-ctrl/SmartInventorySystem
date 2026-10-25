package com.sis.backend.service

import com.sis.backend.dto.CategoryRequest
import com.sis.backend.entity.Category
import com.sis.backend.exception.CategoryInUseException
import com.sis.backend.exception.CategoryNotFoundException
import com.sis.backend.exception.DuplicateCategoryException
import com.sis.backend.exception.InvalidCategoryException
import com.sis.backend.repository.CategoryRepository

/**
 * Lớp dịch vụ (service) xử lý logic nghiệp vụ liên quan đến quản lý danh mục.
 *
 * @property repository Kho lưu trữ [CategoryRepository] dùng để thao tác dữ liệu danh mục.
 */
class CategoryService(
    private val repository: CategoryRepository
) {
    /**
     * Lấy danh sách toàn bộ các danh mục trong hệ thống.
     *
     * @return Danh sách các đối tượng [Category].
     */
    fun getAll(): List<Category> = repository.findAll()

    /**
     * Lấy thông tin chi tiết của một danh mục dựa trên định danh (ID).
     *
     * @param id Định danh duy nhất của danh mục cần tìm.
     * @return Đối tượng [Category] tương ứng.
     * @throws InvalidCategoryException nếu ID không hợp lệ.
     * @throws CategoryNotFoundException nếu không tìm thấy danh mục với ID tương ứng.
     */
    fun getById(id: Long): Category {
        validateId(id)
        return repository.findById(id)
            ?: throw CategoryNotFoundException()
    }

    /**
     * Tạo mới một danh mục trong hệ thống dựa trên yêu cầu được cung cấp.
     *
     * @param request Dữ liệu yêu cầu chứa thông tin danh mục cần tạo ([CategoryRequest]).
     * @return Đối tượng [Category] sau khi đã được tạo thành công.
     * @throws InvalidCategoryException nếu dữ liệu yêu cầu không hợp lệ.
     * @throws DuplicateCategoryException nếu tên danh mục đã tồn tại.
     */
    fun create(request: CategoryRequest): Category {
        val name = validateName(request.categoryName)
        val description = normalizeDescription(request.description)
        val status = validateStatus(request.status)

        if (repository.existsByName(name)) {
            throw DuplicateCategoryException()
        }

        return repository.create(name, description, status)
    }

    /**
     * Cập nhật thông tin của một danh mục hiện có trong hệ thống.
     *
     * @param id Định danh duy nhất của danh mục cần cập nhật.
     * @param request Dữ liệu yêu cầu chứa thông tin mới của danh mục ([CategoryRequest]).
     * @return Đối tượng [Category] sau khi được cập nhật thành công.
     * @throws InvalidCategoryException nếu ID hoặc dữ liệu yêu cầu không hợp lệ.
     * @throws CategoryNotFoundException nếu không tìm thấy danh mục cần cập nhật.
     * @throws DuplicateCategoryException nếu tên danh mục mới bị trùng lặp.
     */
    fun update(id: Long, request: CategoryRequest): Category {
        validateId(id)

        if (repository.findById(id) == null) {
            throw CategoryNotFoundException()
        }

        val name = validateName(request.categoryName)
        val description = normalizeDescription(request.description)
        val status = validateStatus(request.status)

        if (repository.existsByName(name, excludeId = id)) {
            throw DuplicateCategoryException()
        }

        return repository.update(id, name, description, status)
            ?: throw CategoryNotFoundException()
    }

    /**
     * Xóa một danh mục khỏi hệ thống dựa trên định danh (ID).
     *
     * @param id Định danh duy nhất của danh mục cần xóa.
     * @throws InvalidCategoryException nếu ID không hợp lệ.
     * @throws CategoryNotFoundException nếu không tìm thấy danh mục cần xóa.
     * @throws CategoryInUseException nếu danh mục đang được sản phẩm sử dụng.
     */

    fun delete(id: Long) {
        validateId(id)

        if (repository.findById(id) == null) {
            throw CategoryNotFoundException()
        }

        if (repository.hasProducts(id)) {
            throw CategoryInUseException()
        }

        if (!repository.delete(id)) {
            throw CategoryNotFoundException()
        }
    }

    /**
     * Kiểm tra tính hợp lệ của định danh (ID) danh mục.
     *
     * @param id Định danh cần kiểm tra.
     * @throws InvalidCategoryException nếu ID nhỏ hơn hoặc bằng 0.
     */
    private fun validateId(id: Long) {
        if (id <= 0) {
            throw InvalidCategoryException("ID danh mục phải lớn hơn 0")
        }
    }

    /**
     * Kiểm tra và làm sạch tên danh mục.
     *
     * @param value Chuỗi tên danh mục cần kiểm tra.
     * @return Tên danh mục sau khi đã được chuẩn hóa.
     * @throws InvalidCategoryException nếu tên trống hoặc vượt quá độ dài cho phép.
     */
    private fun validateName(value: String): String {
        val name = value.trim()

        if (name.isEmpty()) {
            throw InvalidCategoryException("Tên danh mục không được để trống")
        }

        if (name.length > 120) {
            throw InvalidCategoryException(
                "Tên danh mục không được vượt quá 120 ký tự"
            )
        }

        return name
    }

    /**
     * Chuẩn hóa mô tả danh mục (loại bỏ khoảng trắng thừa, trả về `null` nếu trống).
     *
     * @param value Chuỗi mô tả cần chuẩn hóa.
     * @return Chuỗi mô tả đã chuẩn hóa hoặc `null`.
     */
    private fun normalizeDescription(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Kiểm tra tính hợp lệ của trạng thái danh mục.
     *
     * @param value Chuỗi trạng thái cần kiểm tra.
     * @return Chuỗi trạng thái đã viết hoa chuẩn hóa.
     * @throws InvalidCategoryException nếu trạng thái không hợp lệ.
     */
    private fun validateStatus(value: String): String {
        val status = value.trim().uppercase()

        if (status !in setOf("ACTIVE", "INACTIVE")) {
            throw InvalidCategoryException(
                "Trạng thái chỉ được là ACTIVE hoặc INACTIVE"
            )
        }

        return status
    }
}