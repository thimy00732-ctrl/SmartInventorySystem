package com.sis.backend.config

import com.sis.backend.exception.CategoryException
import com.sis.backend.exception.CategoryInUseException
import com.sis.backend.exception.CategoryNotFoundException
import com.sis.backend.exception.DuplicateCategoryException
import com.sis.backend.exception.InvalidCategoryException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

/**
 * Cấu hình xử lý ngoại lệ tập trung cho ứng dụng Ktor.
 *
 * Chuyển các ngoại lệ thành HTTP status và response JSON
 * thống nhất cho client.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {

        /**
         * Xử lý các ngoại lệ liên quan đến danh mục.
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
         * Xử lý các lỗi không được dự kiến trước.
         *
         * Không trả chi tiết exception nội bộ cho client
         * để tránh làm lộ thông tin hệ thống.
         */
        exception<Throwable> { call, _ ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(
                    "message" to "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."
                )
            )
        }
    }
}
