package com.sis.backend.exception

/**
 * Exception xảy ra khi không tìm thấy nhà cung cấp.
 */
class SupplierNotFoundException(
    message: String = "Không tìm thấy nhà cung cấp."
) : RuntimeException(message)

/**
 * Exception xảy ra khi tên nhà cung cấp đã tồn tại.
 */
class DuplicateSupplierException(
    message: String = "Tên nhà cung cấp đã tồn tại."
) : RuntimeException(message)

/**
 * Exception xảy ra khi dữ liệu nhà cung cấp không hợp lệ.
 */
class InvalidSupplierException(
    message: String
) : RuntimeException(message)