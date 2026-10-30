
package com.sis.backend.exception

/**
 * Lớp cơ sở cho tất cả các ngoại lệ liên quan đến danh mục
 * trong hệ thống quản lý kho.
 *
 * Sử dụng sealed class để giới hạn các loại ngoại lệ danh mục
 * được định nghĩa trong cùng file.
 *
 * @property message Thông điệp chi tiết mô tả lỗi.
 */
sealed class CategoryException(
    override val message: String
) : RuntimeException(message)

/**
 * Ngoại lệ được ném ra khi không tìm thấy danh mục
 * theo ID được yêu cầu.
 *
 * @property message Thông điệp lỗi mặc định.
 */
class CategoryNotFoundException :
    CategoryException("Không tìm thấy danh mục.")

/**
 * Ngoại lệ được ném ra khi tên danh mục mới bị trùng
 * với tên của một danh mục đã tồn tại.
 *
 * @property message Thông điệp lỗi mặc định.
 */
class DuplicateCategoryException :
    CategoryException("Tên danh mục đã tồn tại.")

/**
 * Ngoại lệ được ném ra khi danh mục vẫn đang được
 * sản phẩm hoạt động sử dụng nên không thể ngừng hoạt động.
 *
 * @property message Thông điệp mô tả lý do không thể ngừng hoạt động.
 */
class CategoryInUseException(
    override val message: String =
        "Không thể ngừng hoạt động danh mục vì vẫn còn sản phẩm đang hoạt động."
) : CategoryException(message)

/**
 * Ngoại lệ được ném ra khi dữ liệu danh mục không hợp lệ,
 * chẳng hạn tên danh mục để trống hoặc vượt quá độ dài cho phép.
 *
 * @property message Thông điệp chi tiết mô tả dữ liệu không hợp lệ.
 */
class InvalidCategoryException(
    override val message: String
) : CategoryException(message)
