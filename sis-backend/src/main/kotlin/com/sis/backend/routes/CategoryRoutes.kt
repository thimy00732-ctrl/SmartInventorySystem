package com.sis.backend.routes

import com.sis.backend.controller.CategoryController
import com.sis.backend.dto.CategoryRequest
import com.sis.backend.exception.CategoryException
import com.sis.backend.exception.CategoryInUseException
import com.sis.backend.exception.CategoryNotFoundException
import com.sis.backend.exception.DuplicateCategoryException
import com.sis.backend.exception.InvalidCategoryException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

/**
 * Định nghĩa các tuyến đường định tuyến (routes) cho các thao tác quản lý danh mục.
 *
 * @param controller Bộ điều khiển [CategoryController] xử lý logic nghiệp vụ của danh mục.
 */
fun Route.categoryRoutes(controller: CategoryController) {
    route("/api/categories") {

        // GET /api/categories
        get {
            try {
                call.respond(controller.getAll())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Không thể lấy danh sách danh mục")
                )
            }
        }

        // GET /api/categories/{id}
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ")
                )
                return@get
            }

            try {
                call.respond(controller.getById(id))
            } catch (e: Exception) {
                call.respondCategoryError(e)
            }
        }

        // POST /api/categories
        post {
            try {
                val request = call.receive<CategoryRequest>()
                call.respond(
                    HttpStatusCode.Created,
                    controller.create(request)
                )
            } catch (e: Exception) {
                call.respondCategoryError(e)
            }
        }

        // PUT /api/categories/{id}
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ")
                )
                return@put
            }

            try {
                val request = call.receive<CategoryRequest>()
                call.respond(controller.update(id, request))
            } catch (e: Exception) {
                call.respondCategoryError(e)
            }
        }

        // DELETE /api/categories/{id}
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ")
                )
                return@delete
            }

            try {
                controller.delete(id)
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Xóa danh mục thành công")
                )
            } catch (e: Exception) {
                call.respondCategoryError(e)
            }
        }
    }
}

/**
 * Xử lý ngoại lệ và ánh xạ sang mã phản hồi HTTP tương ứng cho các yêu cầu liên quan đến danh mục.
 *
 * @param e Ngoại lệ [Exception] phát sinh trong quá trình xử lý yêu cầu.
 */
private suspend fun io.ktor.server.application.ApplicationCall
        .respondCategoryError(e: Exception) {
    when (e) {
        is CategoryNotFoundException ->
            respond(
                HttpStatusCode.NotFound,
                mapOf("message" to (e.message ?: "Không tìm thấy danh mục"))
            )

        is DuplicateCategoryException ->
            respond(
                HttpStatusCode.Conflict,
                mapOf("message" to (e.message ?: "Tên danh mục đã tồn tại"))
            )

        is CategoryInUseException ->
            respond(
                HttpStatusCode.Conflict,
                mapOf("message" to (e.message ?: "Danh mục đang được sử dụng"))
            )

        is InvalidCategoryException ->
            respond(
                HttpStatusCode.BadRequest,
                mapOf("message" to (e.message ?: "Dữ liệu không hợp lệ"))
            )

        is CategoryException ->
            respond(
                HttpStatusCode.BadRequest,
                mapOf("message" to e.message)
            )

        else ->
            respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Đã xảy ra lỗi khi xử lý danh mục")
            )
    }
}