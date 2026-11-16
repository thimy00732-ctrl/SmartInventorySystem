package com.sis.backend.routes

import com.sis.backend.controller.CategoryController
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.service.CategoryService
import com.sis.backend.security.requireAdmin
import com.sis.backend.security.requireStaffOrAdmin
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
 * Theo tài liệu phân quyền UC18-UC22:
 * - UC18: Thêm danh mục - ADMIN only
 * - UC19: Xem danh sách danh mục - Cả hai role
 * - UC20: Cập nhật danh mục - ADMIN only
 * - UC21: Ngừng sử dụng danh mục - ADMIN only
 * - UC22: Tìm kiếm danh mục - Cả hai role
 *
 * @param categoryController Controller xử lý các yêu cầu danh mục.
 */
fun Route.categoryRoutes(categoryController: CategoryController) {
    route("/api/categories") {

        // ========== ENDPOINTS CHO CẢ ADMIN VÀ WAREHOUSE_STAFF ==========
        // UC19, UC22: Xem và tìm kiếm danh mục
        
        requireStaffOrAdmin {
            /**
             * UC19, UC22: Lấy danh sách danh mục, hỗ trợ tìm kiếm và lọc trạng thái.
             */
            get {
                categoryController.getCategories(call)
            }

            /**
             * UC19: Lấy thông tin chi tiết một danh mục.
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
        }

        // ========== ENDPOINTS CHỈ CHO ADMIN ==========
        // UC18, UC20, UC21: Thêm, cập nhật, ngừng sử dụng danh mục
        
        requireAdmin {
            /**
             * UC18: Tạo danh mục mới - CHỈ ADMIN.
             */
            post {
                categoryController.createCategory(call)
            }

            /**
             * UC20: Cập nhật thông tin danh mục - CHỈ ADMIN.
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
             * UC21: Ngừng hoạt động danh mục - CHỈ ADMIN.
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

            /**
             * UC21: Kích hoạt lại danh mục - CHỈ ADMIN.
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

            /**
             * Xóa danh mục - CHỈ ADMIN.
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
}
