package com.sis.backend.controller

import com.sis.backend.dto.CreateCategoryRequest
import com.sis.backend.dto.UpdateCategoryRequest
import com.sis.backend.service.CategoryService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.response.respond

/**
 * Controller xử lý các yêu cầu HTTP liên quan đến danh mục sản phẩm.
 *
 * @property categoryService Service xử lý nghiệp vụ danh mục.
 */
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * Lấy danh sách danh mục và hỗ trợ tìm kiếm theo tên.
     *
     * @param call Đối tượng xử lý request và response HTTP.
     */
    suspend fun getCategories(call: ApplicationCall) {
        try {
            val keyword = call.request.queryParameters["keyword"]
            val activeOnly = call.request.queryParameters["activeOnly"]
                ?.toBooleanStrictOrNull() ?: false

            val categories = categoryService.getCategories(
                keyword = keyword,
                activeOnly = activeOnly
            )

            call.respond(HttpStatusCode.OK, categories)
        } catch (exception: Exception) {
            call.application.log.error(
                "Lỗi khi lấy danh sách danh mục",
                exception
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Không thể lấy danh sách danh mục.")
            )
        }
    }

    /**
     * Lấy thông tin chi tiết danh mục theo ID.
     *
     * @param call Đối tượng xử lý request và response HTTP.
     * @param categoryId ID danh mục cần lấy.
     */
    suspend fun getCategoryById(
        call: ApplicationCall,
        categoryId: Long
    ) {
        try {
            val category = categoryService.getCategoryById(categoryId)
            call.respond(HttpStatusCode.OK, category)
        } catch (exception: NoSuchElementException) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf(
                    "message" to (
                            exception.message ?: "Không tìm thấy danh mục."
                            )
                )
            )
        } catch (exception: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "message" to (
                            exception.message ?: "ID danh mục không hợp lệ."
                            )
                )
            )
        } catch (exception: Exception) {
            call.application.log.error(
                "Lỗi khi lấy thông tin danh mục ID=$categoryId",
                exception
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Không thể lấy thông tin danh mục.")
            )
        }
    }

    /**
     * Tạo danh mục mới.
     *
     * @param call Đối tượng xử lý request và response HTTP.
     */
    suspend fun createCategory(call: ApplicationCall) {
        try {
            val request = call.receive<CreateCategoryRequest>()
            val category = categoryService.createCategory(request)

            call.respond(HttpStatusCode.Created, category)
        } catch (exception: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "message" to (
                            exception.message ?: "Dữ liệu không hợp lệ."
                            )
                )
            )
        } catch (exception: Exception) {
            call.application.log.error(
                "Lỗi khi tạo danh mục sản phẩm",
                exception
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Không thể tạo danh mục.")
            )
        }
    }

    /**
     * Cập nhật thông tin danh mục.
     *
     * @param call Đối tượng xử lý request và response HTTP.
     * @param categoryId ID danh mục cần cập nhật.
     */
    suspend fun updateCategory(
        call: ApplicationCall,
        categoryId: Long
    ) {
        try {
            val request = call.receive<UpdateCategoryRequest>()
            val category = categoryService.updateCategory(categoryId, request)

            call.respond(HttpStatusCode.OK, category)
        } catch (exception: NoSuchElementException) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf(
                    "message" to (
                            exception.message ?: "Không tìm thấy danh mục."
                            )
                )
            )
        } catch (exception: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "message" to (
                            exception.message ?: "Dữ liệu không hợp lệ."
                            )
                )
            )
        } catch (exception: Exception) {
            call.application.log.error(
                "Lỗi khi cập nhật danh mục ID=$categoryId",
                exception
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Không thể cập nhật danh mục.")
            )
        }
    }

    /**
     * Ngừng hoạt động danh mục.
     *
     * @param call Đối tượng xử lý request và response HTTP.
     * @param categoryId ID danh mục cần ngừng hoạt động.
     */
    suspend fun deactivateCategory(
        call: ApplicationCall,
        categoryId: Long
    ) {
        try {
            categoryService.deactivateCategory(categoryId)

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Đã ngừng hoạt động danh mục.")
            )
        } catch (exception: NoSuchElementException) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf(
                    "message" to (
                            exception.message ?: "Không tìm thấy danh mục."
                            )
                )
            )
        } catch (exception: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "message" to (
                            exception.message ?: "ID danh mục không hợp lệ."
                            )
                )
            )
        } catch (exception: IllegalStateException) {
            call.respond(
                HttpStatusCode.Conflict,
                mapOf(
                    "message" to (
                            exception.message
                                ?: "Không thể ngừng hoạt động danh mục."
                            )
                )
            )
        } catch (exception: Exception) {
            call.application.log.error(
                "Lỗi khi ngừng hoạt động danh mục ID=$categoryId",
                exception
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Không thể ngừng hoạt động danh mục.")
            )
        }
    }
}
