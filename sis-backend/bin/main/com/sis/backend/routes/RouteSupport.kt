package com.sis.backend.routes

import com.sis.backend.dto.ApiErrorResponse
import com.sis.backend.exception.AuthException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respond

suspend fun ApplicationCall.respondAuthOperation(operation: suspend ApplicationCall.() -> Unit) {
    try {
        operation()
    } catch (e: AuthException) {
        respond(e.status, ApiErrorResponse(message = e.message, code = e.code))
    } catch (_: BadRequestException) {
        respond(
            HttpStatusCode.BadRequest,
            ApiErrorResponse(message = "Dữ liệu gửi lên không hợp lệ", code = "INVALID_REQUEST")
        )
    } catch (e: Exception) {
        application.environment.log.error("Lỗi xử lý yêu cầu tài khoản", e)
        respond(
            HttpStatusCode.InternalServerError,
            ApiErrorResponse(
                message = "Đã xảy ra lỗi khi xử lý yêu cầu",
                code = "INTERNAL_SERVER_ERROR"
            )
        )
    }
}
