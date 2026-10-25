package com.sis.backend.exception

/**
 * Lớp cơ sở (base class) cho tất cả các ngoại lệ liên quan đến danh mục trong hệ thống.
 *
 * @property message Thông điệp chi tiết về lỗi.
 */
sealed class CategoryException(
    override val message: String
) : RuntimeException(message)

/**
 * Ngoại lệ được ném ra khi không tìm thấy danh mục yêu cầu trong hệ thống.
 */
class CategoryNotFoundException :
    CategoryException("Không tìm thấy danh mục")

/**
 * Ngoại lệ được ném ra khi tên danh mục mới bị trùng lặp với danh mục đã tồn tại.
 */
class DuplicateCategoryException :
    CategoryException("Tên danh mục đã tồn tại")

/**
 * Ngoại lệ được ném ra khi cố gắng xóa một danh mục vẫn đang được sản phẩm sử dụng.
 */
class CategoryInUseException :
    CategoryException("Không thể xóa danh mục đang được sản phẩm sử dụng")

/**
 * Ngoại lệ được ném ra khi dữ liệu hoặc thông tin cấu hình danh mục không hợp lệ.
 *
 * @property message Thông điệp chi tiết mô tả lỗi không hợp lệ.
 */
class InvalidCategoryException(message: String) :
    CategoryException(message)