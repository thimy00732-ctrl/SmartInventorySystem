package com.sis.backend.routes

import com.sis.backend.controller.CategoryController
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.service.CategoryService
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

/**
 * Khai báo các API quản lý danh mục sản phẩm.
 *
 * @param categoryController Controller xử lý các yêu cầu danh mục.
 */
fun Route.categoryRoutes(categoryController: CategoryController) {
    route("/api/categories") {

        /**
         * GET /api/categories
         * Lấy danh sách danh mục, hỗ trợ tìm kiếm và lọc trạng thái.
         */
        get {
            categoryController.getCategories(call)
        }

        /**
         * GET /api/categories/{id}
         * Lấy thông tin chi tiết một danh mục.
         */
        get("/{id}") {
            val categoryId = call.parameters["id"]?.toLongOrNull()

            if (categoryId == null || categoryId <= 0) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ.")
                )
                return@get
            }

            categoryController.getCategoryById(call, categoryId)
        }

        /**
         * POST /api/categories
         * Tạo danh mục mới.
         */
        post {
            categoryController.createCategory(call)
        }

        /**
         * PUT /api/categories/{id}
         * Cập nhật thông tin danh mục.
         */
        put("/{id}") {
            val categoryId = call.parameters["id"]?.toLongOrNull()

            if (categoryId == null || categoryId <= 0) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ.")
                )
                return@put
            }

            categoryController.updateCategory(call, categoryId)
        }

        /**
         * PATCH /api/categories/{id}/deactivate
         * Ngừng hoạt động danh mục mà không xóa vật lý.
         */
        patch("/{id}/deactivate") {
            val categoryId = call.parameters["id"]?.toLongOrNull()

            if (categoryId == null || categoryId <= 0) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ.")
                )
                return@patch
            }

            categoryController.deactivateCategory(call, categoryId)
        }

        /* UPDATE 2024: Thêm route kích hoạt lại danh mục */
        /**
         * PATCH /api/categories/{id}/reactivate
         * Kích hoạt lại danh mục đã ngừng hoạt động.
         */
        patch("/{id}/reactivate") {
            val categoryId = call.parameters["id"]?.toLongOrNull()

            if (categoryId == null || categoryId <= 0) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ.")
                )
                return@patch
            }

            categoryController.reactivateCategory(call, categoryId)
        }

        /* UPDATE 2024: Thêm route xóa danh mục */
        /**
         * DELETE /api/categories/{id}
         * Xóa danh mục (chỉ cho phép xóa danh mục đã ngừng hoạt động
         * và không có sản phẩm).
         */
        delete("/{id}") {
            val categoryId = call.parameters["id"]?.toLongOrNull()

            if (categoryId == null || categoryId <= 0) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("message" to "ID danh mục không hợp lệ.")
                )
                return@delete
            }

            categoryController.deleteCategory(call, categoryId)
        }
    }
}
