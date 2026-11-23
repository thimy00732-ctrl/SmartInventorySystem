package com.sis.backend.config

import com.sis.backend.dto.ErrorResponse
import com.sis.backend.exception.AppException
import com.sis.backend.exception.CategoryException
import com.sis.backend.exception.CategoryInUseException
import com.sis.backend.exception.CategoryNotFoundException
import com.sis.backend.exception.DataIntegrityException
import com.sis.backend.exception.DuplicateCategoryException
import com.sis.backend.exception.ErrorCode
import com.sis.backend.exception.InvalidCategoryException
import com.sis.backend.exception.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import java.sql.SQLException

/**
 * Cấu hình xử lý ngoại lệ tập trung cho ứng dụng Ktor.
 *
 * Chuyển các ngoại lệ thành HTTP status và response JSON thống nhất.
 * 
 * Sprint 1: giữ nguyên format cũ mapOf("message" to ...) cho CategoryException.
 * Sprint 2: dùng ErrorResponse mới cho AppException và các lỗi khác.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {

        /**
         * Sprint 2: Xử lý AppException (base class của mọi lỗi nghiệp vụ mới).
         * 
         * ĐẶT TRƯỚC handler Throwable để được ưu tiên.
         */
        exception<AppException> { call, cause ->
            val errorResponse = ErrorResponse.from(
                code = cause.errorCode.name,
                message = cause.message ?: "Lỗi không xác định.",
                status = cause.errorCode.statusCode,
                path = call.request.path()
            )
            
            call.respond(cause.errorCode.httpStatus, errorResponse)
        }
        
        /**
         * Sprint 1: Giữ nguyên xử lý CategoryException (không đổi API cũ).
         */
        exception<CategoryException> { call, cause ->
            val status = when (cause) {
                is CategoryNotFoundException ->
                    HttpStatusCode.NotFound

                is DuplicateCategoryException,
                is CategoryInUseException ->
                    HttpStatusCode.Conflict

                is InvalidCategoryException ->
                    HttpStatusCode.BadRequest
            }

            call.respond(
                status,
                mapOf("message" to cause.message)
            )
        }
        
        /**
         * Body JSON sai định dạng (SerializationException).
         * HTTP 400, ErrorCode.VALIDATION_ERROR.
         */
        exception<SerializationException> { call, cause ->
            val errorResponse = ErrorResponse.from(
                code = ErrorCode.VALIDATION_ERROR.name,
                message = "Dữ liệu JSON không hợp lệ: ${cause.message}",
                status = 400,
                path = call.request.path()
            )
            
            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }
        
        /**
         * BadRequestException (Ktor tự throw khi parse param sai).
         * HTTP 400, ErrorCode.VALIDATION_ERROR.
         */
        exception<BadRequestException> { call, cause ->
            val errorResponse = ErrorResponse.from(
                code = ErrorCode.VALIDATION_ERROR.name,
                message = "Tham số không hợp lệ: ${cause.message}",
                status = 400,
                path = call.request.path()
            )
            
            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }
        
        /**
         * SQLException: vi phạm ràng buộc DB (CHECK, FK, unique).
         * 
         * SQLState 23000 = constraint violation.
         * MySQL error code 3819 = CHECK constraint.
         */
        exception<SQLException> { call, cause ->
            val sqlState = cause.sqlState
            val errorCode = cause.errorCode
            
            // Constraint violation
            if (sqlState?.startsWith("23") == true || errorCode == 3819) {
                val errorResponse = ErrorResponse.from(
                    code = ErrorCode.DATA_INTEGRITY.name,
                    message = "Vi phạm ràng buộc dữ liệu: ${extractConstraintMessage(cause)}",
                    status = 400,
                    path = call.request.path()
                )
                
                call.respond(HttpStatusCode.BadRequest, errorResponse)
            } else {
                // SQL error khác -> 500
                val errorResponse = ErrorResponse.from(
                    code = ErrorCode.INTERNAL_ERROR.name,
                    message = "Lỗi cơ sở dữ liệu. Vui lòng thử lại sau.",
                    status = 500,
                    path = call.request.path()
                )
                
                // Log chi tiết lỗi nhưng không trả client
                call.application.environment.log.error("SQLException: ${cause.message}", cause)
                
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
        
        /**
         * IllegalArgumentException: validation lỗi (ví dụ từ require()).
         * HTTP 400, ErrorCode.VALIDATION_ERROR.
         */
        exception<IllegalArgumentException> { call, cause ->
            val errorResponse = ErrorResponse.from(
                code = ErrorCode.VALIDATION_ERROR.name,
                message = cause.message ?: "Dữ liệu không hợp lệ.",
                status = 400,
                path = call.request.path()
            )
            
            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        /**
         * Xử lý các lỗi không được dự kiến trước.
         *
         * Không trả chi tiết exception nội bộ cho client
         * để tránh làm lộ thông tin hệ thống.
         */
        exception<Throwable> { call, cause ->
            // Log chi tiết cho debug
            call.application.environment.log.error("Unhandled exception: ${cause.message}", cause)
            
            val errorResponse = ErrorResponse.from(
                code = ErrorCode.INTERNAL_ERROR.name,
                message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                status = 500,
                path = call.request.path()
            )
            
            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }
}

/**
 * Trích xuất thông báo constraint từ SQLException.
 */
private fun extractConstraintMessage(exception: SQLException): String {
    val message = exception.message ?: return "Unknown constraint violation"
    
    // Extract constraint name từ message MySQL/MariaDB
    return when {
        message.contains("CHECK constraint", ignoreCase = true) -> 
            "Giá trị không thỏa mãn ràng buộc kiểm tra."
        message.contains("foreign key", ignoreCase = true) -> 
            "Tham chiếu đến bản ghi không tồn tại."
        message.contains("Duplicate entry", ignoreCase = true) -> 
            "Giá trị đã tồn tại (trùng lặp)."
        else -> message.take(100) // Giới hạn độ dài message
    }
}
