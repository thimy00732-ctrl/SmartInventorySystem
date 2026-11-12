package com.sis.backend.service

import com.sis.backend.dto.CreateCategoryRequest
import com.sis.backend.dto.UpdateCategoryRequest
import com.sis.backend.entity.Category
import com.sis.backend.exception.CategoryInUseException
import com.sis.backend.exception.CategoryNotFoundException
import com.sis.backend.exception.DuplicateCategoryException
import com.sis.backend.exception.InvalidCategoryException
import com.sis.backend.repository.CategoryRepository

/**
 * Xử lý nghiệp vụ quản lý danh mục sản phẩm.
 *
 * @property categoryRepository Repository thao tác với dữ liệu danh mục.
 */
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    /**
     * Lấy danh sách danh mục, có thể lọc theo từ khóa
     * và trạng thái hoạt động.
     *
     * @param keyword Từ khóa tìm kiếm theo tên danh mục.
     * @param activeOnly Nếu true, chỉ lấy danh mục đang hoạt động.
     * @return Danh sách danh mục phù hợp với điều kiện lọc.
     */
    fun getCategories(
        keyword: String? = null,
        activeOnly: Boolean = false
    ): List<Category> {
        return categoryRepository.findAll(
            keyword = keyword?.trim()?.ifEmpty { null },
            activeOnly = activeOnly
        )
    }

    /**
     * Lấy thông tin danh mục theo ID.
     *
     * @param categoryId ID danh mục cần tìm.
     * @return Thông tin danh mục.
     * @throws InvalidCategoryException Nếu ID không hợp lệ.
     * @throws CategoryNotFoundException Nếu không tìm thấy danh mục.
     */
    fun getCategoryById(categoryId: Long): Category {
        if (categoryId <= 0) {
            throw InvalidCategoryException(
                "ID danh mục phải lớn hơn 0."
            )
        }

        return categoryRepository.findById(categoryId)
            ?: throw CategoryNotFoundException()
    }

    /**
     * Tạo danh mục mới sau khi kiểm tra dữ liệu đầu vào.
     *
     * @param request Dữ liệu yêu cầu tạo danh mục.
     * @return Danh mục vừa được tạo.
     * @throws InvalidCategoryException Nếu dữ liệu không hợp lệ.
     * @throws DuplicateCategoryException Nếu tên danh mục đã tồn tại.
     */
    fun createCategory(
        request: CreateCategoryRequest
    ): Category {
        val categoryName = request.categoryName.trim()
        val description = request.description
            ?.trim()
            ?.ifEmpty { null }

        validateCategoryData(categoryName, description)

        if (categoryRepository.existsByName(categoryName)) {
            throw DuplicateCategoryException()
        }

        val category = Category(
            categoryName = categoryName,
            description = description,
            isActive = true
        )

        return categoryRepository.create(category)
    }

    /**
     * Cập nhật thông tin một danh mục đang hoạt động.
     *
     * @param categoryId ID danh mục cần cập nhật.
     * @param request Dữ liệu cập nhật danh mục.
     * @return Danh mục sau khi cập nhật.
     * @throws InvalidCategoryException Nếu ID hoặc dữ liệu không hợp lệ.
     * @throws CategoryNotFoundException Nếu không tìm thấy danh mục.
     * @throws DuplicateCategoryException Nếu tên đã được danh mục khác sử dụng.
     */
    fun updateCategory(
        categoryId: Long,
        request: UpdateCategoryRequest
    ): Category {
        if (categoryId <= 0) {
            throw InvalidCategoryException(
                "ID danh mục phải lớn hơn 0."
            )
        }

        val existingCategory = categoryRepository.findById(categoryId)
            ?: throw CategoryNotFoundException()

        // Cho phép cập nhật cả category đã ngừng hoạt động
        // để có thể sửa thông tin trước khi kích hoạt lại

        val categoryName = request.categoryName.trim()
        val description = request.description
            ?.trim()
            ?.ifEmpty { null }

        validateCategoryData(categoryName, description)

        if (categoryRepository.existsByName(categoryName, categoryId)) {
            throw DuplicateCategoryException()
        }

        return categoryRepository.update(
            categoryId = categoryId,
            categoryName = categoryName,
            description = description
        ) ?: throw CategoryNotFoundException()
    }

    /**
     * Ngừng hoạt động danh mục mà không xóa dữ liệu vật lý.
     *
     * Không cho phép ngừng hoạt động nếu vẫn còn sản phẩm
     * đang hoạt động thuộc danh mục đó.
     *
     * @param categoryId ID danh mục cần ngừng hoạt động.
     * @return true nếu thao tác thành công.
     * @throws InvalidCategoryException Nếu ID không hợp lệ.
     * @throws CategoryNotFoundException Nếu không tìm thấy danh mục.
     * @throws CategoryInUseException Nếu còn sản phẩm đang hoạt động.
     */
    fun deactivateCategory(categoryId: Long): Boolean {
        if (categoryId <= 0) {
            throw InvalidCategoryException(
                "ID danh mục phải lớn hơn 0."
            )
        }

        val category = categoryRepository.findById(categoryId)
            ?: throw CategoryNotFoundException()

        if (!category.isActive) {
            throw InvalidCategoryException(
                "Danh mục đã ngừng hoạt động."
            )
        }

        if (categoryRepository.hasActiveProducts(categoryId)) {
            throw CategoryInUseException()
        }

        val deactivated = categoryRepository.deactivate(categoryId)

        if (!deactivated) {
            throw CategoryNotFoundException()
        }

        return true
    }

    /**
     * Kiểm tra dữ liệu danh mục trước khi tạo hoặc cập nhật.
     *
     * @param categoryName Tên danh mục sau khi loại bỏ khoảng trắng.
     * @param description Mô tả danh mục, có thể null.
     * @throws InvalidCategoryException Nếu dữ liệu không hợp lệ.
     */
    private fun validateCategoryData(
        categoryName: String,
        description: String?
    ) {
        if (categoryName.isBlank()) {
            throw InvalidCategoryException(
                "Tên danh mục không được để trống."
            )
        }

        if (categoryName.length > 100) {
            throw InvalidCategoryException(
                "Tên danh mục không được vượt quá 100 ký tự."
            )
        }

        if (description != null && description.length > 500) {
            throw InvalidCategoryException(
                "Mô tả danh mục không được vượt quá 500 ký tự."
            )
        }
    }

    /* UPDATE 2024: Thêm nghiệp vụ kích hoạt lại danh mục */
    /**
     * Kích hoạt lại danh mục đã ngừng hoạt động.
     *
     * @param categoryId ID danh mục cần kích hoạt lại.
     * @return true nếu thao tác thành công.
     * @throws InvalidCategoryException Nếu ID không hợp lệ hoặc danh mục đang hoạt động.
     * @throws CategoryNotFoundException Nếu không tìm thấy danh mục.
     */
    fun reactivateCategory(categoryId: Long): Boolean {
        if (categoryId <= 0) {
            throw InvalidCategoryException(
                "ID danh mục phải lớn hơn 0."
            )
        }

        val category = categoryRepository.findById(categoryId)
            ?: throw CategoryNotFoundException()

        if (category.isActive) {
            throw InvalidCategoryException(
                "Danh mục đang hoạt động."
            )
        }

        val reactivated = categoryRepository.reactivate(categoryId)

        if (!reactivated) {
            throw CategoryNotFoundException()
        }

        return true
    }

    /* UPDATE 2024: Thêm nghiệp vụ xóa danh mục */
    /**
     * Xóa danh mục (chỉ cho phép xóa danh mục đã ngừng hoạt động 
     * và không có sản phẩm nào).
     *
     * @param categoryId ID danh mục cần xóa.
     * @return true nếu thao tác thành công.
     * @throws InvalidCategoryException Nếu ID không hợp lệ.
     * @throws CategoryNotFoundException Nếu không tìm thấy danh mục.
     * @throws IllegalStateException Nếu danh mục đang hoạt động hoặc còn sản phẩm.
     */
    fun deleteCategory(categoryId: Long): Boolean {
        if (categoryId <= 0) {
            throw InvalidCategoryException(
                "ID danh mục phải lớn hơn 0."
            )
        }

        val category = categoryRepository.findById(categoryId)
            ?: throw CategoryNotFoundException()

        // Chỉ cho xóa danh mục đã ngừng hoạt động
        if (category.isActive) {
            throw IllegalStateException(
                "Vui lòng ngừng hoạt động danh mục trước khi xóa."
            )
        }

        // Kiểm tra còn sản phẩm không (cả active và inactive)
        if (categoryRepository.hasProducts(categoryId)) {
            throw IllegalStateException(
                "Không thể xóa danh mục còn sản phẩm."
            )
        }

        val deleted = categoryRepository.delete(categoryId)

        if (!deleted) {
            throw CategoryNotFoundException()
        }

        return true
    }
}
