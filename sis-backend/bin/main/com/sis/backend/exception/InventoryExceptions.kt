package com.sis.backend.exception

/**
 * Base exception cho tất cả lỗi nghiệp vụ (Sprint 2).
 * 
 * Kế thừa từ RuntimeException để không bắt buộc try-catch.
 * Mỗi subclass map với một ErrorCode cụ thể.
 */
abstract class AppException(
    override val message: String,
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Resource không tồn tại (inventory, product, warehouse, user, ...).
 * 
 * Dùng chung cho mọi loại resource, không tạo riêng từng class.
 * HTTP 404, ErrorCode.RESOURCE_NOT_FOUND.
 */
class ResourceNotFoundException(
    resourceType: String,
    resourceId: Any
) : AppException(
    message = "$resourceType với ID $resourceId không tồn tại.",
    errorCode = ErrorCode.RESOURCE_NOT_FOUND
)

/**
 * reserved_quantity không hợp lệ (UC31).
 * 
 * HTTP 400, ErrorCode.INVALID_RESERVED_QUANTITY.
 */
class InvalidReservedQuantityException(
    message: String
) : AppException(
    message = message,
    errorCode = ErrorCode.INVALID_RESERVED_QUANTITY
)

/**
 * Ngưỡng tồn kho không hợp lệ (UC33).
 * 
 * HTTP 400, ErrorCode.INVALID_THRESHOLD.
 */
class InvalidThresholdException(
    message: String
) : AppException(
    message = message,
    errorCode = ErrorCode.INVALID_THRESHOLD
)

/**
 * Khoảng ngày không hợp lệ (UC36).
 * 
 * HTTP 400, ErrorCode.INVALID_DATE_RANGE.
 */
class InvalidDateRangeException(
    message: String
) : AppException(
    message = message,
    errorCode = ErrorCode.INVALID_DATE_RANGE
)

/**
 * Lỗi validation chung (query params, request body).
 * 
 * HTTP 400, ErrorCode.VALIDATION_ERROR.
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : AppException(
    message = message,
    errorCode = ErrorCode.VALIDATION_ERROR,
    cause = cause
)

/**
 * Vi phạm ràng buộc dữ liệu (CHECK, FK, unique).
 * 
 * HTTP 400, ErrorCode.DATA_INTEGRITY.
 */
class DataIntegrityException(
    message: String,
    cause: Throwable? = null
) : AppException(
    message = message,
    errorCode = ErrorCode.DATA_INTEGRITY,
    cause = cause
)

/**
 * Chưa đăng nhập.
 * 
 * HTTP 401, ErrorCode.UNAUTHORIZED.
 */
class UnauthorizedException(
    message: String = "Vui lòng đăng nhập để tiếp tục."
) : AppException(
    message = message,
    errorCode = ErrorCode.UNAUTHORIZED
)

/**
 * Không đủ quyền.
 * 
 * HTTP 403, ErrorCode.FORBIDDEN.
 */
class ForbiddenException(
    message: String = "Bạn không có quyền thực hiện thao tác này."
) : AppException(
    message = message,
    errorCode = ErrorCode.FORBIDDEN
)
