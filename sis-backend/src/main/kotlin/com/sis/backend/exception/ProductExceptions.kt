package com.sis.backend.exception

/**
 * Lớp cơ sở cho các ngoại lệ nghiệp vụ liên quan đến sản phẩm.
 *
 * @param message Mô tả lỗi nghiệp vụ.
 */
sealed class ProductException(
    override val message: String
) : RuntimeException(message)

/**
 * Được ném khi không tìm thấy sản phẩm.
 */
class ProductNotFoundException :
    ProductException("Không tìm thấy sản phẩm")

/**
 * Được ném khi SKU đã tồn tại.
 */
class DuplicateSkuException :
    ProductException("SKU đã tồn tại")

/**
 * Được ném khi Barcode đã thuộc về sản phẩm khác.
 */
class DuplicateBarcodeException :
    ProductException("Barcode đã tồn tại")

/**
 * Được ném khi danh mục không tồn tại hoặc không hoạt động.
 */
class InvalidProductCategoryException :
    ProductException("Danh mục không tồn tại hoặc không hoạt động")

/**
 * Được ném khi dữ liệu sản phẩm không hợp lệ.
 *
 * @param message Mô tả dữ liệu không hợp lệ.
 */
class InvalidProductException(message: String) :
    ProductException(message)

/**
 * Được ném khi người dùng không có quyền xóa sản phẩm.
 */
class ProductForbiddenException :
    ProductException("Bạn không có quyền thực hiện thao tác này")