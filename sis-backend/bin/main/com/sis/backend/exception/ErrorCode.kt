package com.sis.backend.exception

import io.ktor.http.HttpStatusCode

/**
 * Enum mã lỗi nghiệp vụ (Sprint 2).
 * 
 * Mỗi mã kèm HTTP status code tương ứng.
 * Dùng cho ErrorResponse.code và xác định status trả về.
 */
enum class ErrorCode(val httpStatus: HttpStatusCode) {
    /**
     * Resource không tồn tại (inventory, product, warehouse, ...).
     * HTTP 404 Not Found.
     */
    RESOURCE_NOT_FOUND(HttpStatusCode.NotFound),
    
    /**
     * reserved_quantity không hợp lệ (âm, vượt stock_quantity).
     * HTTP 400 Bad Request.
     */
    INVALID_RESERVED_QUANTITY(HttpStatusCode.BadRequest),
    
    /**
     * Ngưỡng tồn kho không hợp lệ (min < 0, max < min).
     * HTTP 400 Bad Request.
     */
    INVALID_THRESHOLD(HttpStatusCode.BadRequest),
    
    /**
     * Khoảng ngày không hợp lệ (fromDate > toDate, quá 366 ngày).
     * HTTP 400 Bad Request.
     */
    INVALID_DATE_RANGE(HttpStatusCode.BadRequest),
    
    /**
     * Lỗi validation chung (request body, query params).
     * HTTP 400 Bad Request.
     */
    VALIDATION_ERROR(HttpStatusCode.BadRequest),
    
    /**
     * Vi phạm ràng buộc dữ liệu (CHECK constraint, foreign key, unique).
     * HTTP 400 Bad Request.
     */
    DATA_INTEGRITY(HttpStatusCode.BadRequest),
    
    /**
     * Chưa đăng nhập (không có token hoặc token hết hạn).
     * HTTP 401 Unauthorized.
     */
    UNAUTHORIZED(HttpStatusCode.Unauthorized),
    
    /**
     * Không đủ quyền (role không phù hợp).
     * HTTP 403 Forbidden.
     */
    FORBIDDEN(HttpStatusCode.Forbidden),
    
    /**
     * Lỗi server không dự kiến.
     * HTTP 500 Internal Server Error.
     */
    INTERNAL_ERROR(HttpStatusCode.InternalServerError);
    
    val statusCode: Int
        get() = httpStatus.value
}
