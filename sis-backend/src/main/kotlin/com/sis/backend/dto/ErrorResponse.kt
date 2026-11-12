package com.sis.backend.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Response chuẩn cho lỗi API (Sprint 2 trở đi).
 * 
 * Theo spec mục 6: timestamp, status, code, message, path, details.
 * Sprint 1 API giữ nguyên format cũ mapOf("message" to ...).
 */
@Serializable
data class ErrorResponse(
    /**
     * Thời điểm xảy ra lỗi (ISO 8601: yyyy-MM-dd'T'HH:mm:ss).
     */
    val timestamp: String,
    
    /**
     * HTTP status code (400, 401, 403, 404, 500, ...).
     */
    val status: Int,
    
    /**
     * Mã lỗi nghiệp vụ (ErrorCode enum name).
     * Ví dụ: "RESOURCE_NOT_FOUND", "INVALID_RESERVED_QUANTITY".
     */
    val code: String,
    
    /**
     * Thông báo lỗi bằng tiếng Việt, rõ ràng cho client.
     */
    val message: String,
    
    /**
     * Đường dẫn API bị lỗi (ví dụ: "/api/inventory/123").
     */
    val path: String,
    
    /**
     * Chi tiết bổ sung (optional).
     * Ví dụ: field validation errors, constraint violations.
     */
    val details: Map<String, String>? = null
) {
    companion object {
        /**
         * Tạo ErrorResponse từ exception và request path.
         */
        fun from(
            code: String,
            message: String,
            status: Int,
            path: String,
            details: Map<String, String>? = null
        ): ErrorResponse {
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            
            return ErrorResponse(
                timestamp = timestamp,
                status = status,
                code = code,
                message = message,
                path = path,
                details = details
            )
        }
    }
}
