package com.sis.backend.routes

import com.sis.backend.controller.ProductController
import com.sis.backend.dto.ProductRequest
import com.sis.backend.exception.DuplicateBarcodeException
import com.sis.backend.exception.DuplicateSkuException
import com.sis.backend.exception.InvalidProductCategoryException
import com.sis.backend.exception.InvalidProductException
import com.sis.backend.exception.ProductException
import com.sis.backend.exception.ProductForbiddenException
import com.sis.backend.exception.ProductNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.sql.SQLException

/**
 * Đăng ký các endpoint CRUD sản phẩm.
 *
 * @param controller Controller xử lý yêu cầu sản phẩm.
 */
fun Route.productRoutes(controller: ProductController) {
    route("/api/products") {

        // GET /api/products?q={keyword}
        get {
            val keyword = call.request.queryParameters["q"].orEmpty()

            try {
                call.respond(
                    HttpStatusCode.OK,
                    controller.searchProducts(keyword)
                )
            } catch (exception: Exception) {
                call.application.environment.log.error(
                    "Lỗi khi tìm kiếm sản phẩm.",
                    exception
                )
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Không thể tìm kiếm sản phẩm.")
                )
            }
        }

// GET /api/products/barcode/{barcode}
        get("/barcode/{barcode}") {
            val barcode = call.parameters["barcode"]

            if (barcode.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Barcode không được để trống.")
                )
                return@get
            }

            try {
                call.respond(
                    HttpStatusCode.OK,
                    controller.getProductByBarcode(barcode)
                )
            } catch (exception: ProductNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "Không tìm thấy sản phẩm với Barcode đã cung cấp.")
                )
            } catch (exception: InvalidProductException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to (exception.message ?: "Barcode không hợp lệ."))
                )
            } catch (exception: Exception) {
                call.application.environment.log.error(
                    "Lỗi khi tra cứu sản phẩm bằng Barcode.",
                    exception
                )
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Không thể tra cứu sản phẩm.")
                )
            }
        }

        /**
         * GET /api/products
         * Trả về danh sách sản phẩm.
         */
        get {
            handleProductErrors {
                call.respond(HttpStatusCode.OK, controller.getAll())
            }
        }

        /**
         * GET /api/products/{id}
         * Trả về chi tiết sản phẩm.
         */
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID sản phẩm không hợp lệ")
                )
                return@get
            }

            handleProductErrors {
                call.respond(HttpStatusCode.OK, controller.getById(id))
            }
        }

        /**
         * POST /api/products
         * Tạo sản phẩm mới.
         */
        post {
            handleProductErrors {
                val request = call.receive<ProductRequest>()
                val createdProduct = controller.create(request)
                call.respond(HttpStatusCode.Created, createdProduct)
            }
        }

        /**
         * PUT /api/products/{id}
         * Cập nhật sản phẩm.
         */
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID sản phẩm không hợp lệ")
                )
                return@put
            }

            handleProductErrors {
                val request = call.receive<ProductRequest>()
                call.respond(HttpStatusCode.OK, controller.update(id, request))
            }
        }

        /**
         * DELETE /api/products/{id}
         * Chỉ người dùng có JWT hợp lệ và role ADMIN được xóa sản phẩm.
         */
        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()

            if (principal == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Yêu cầu đăng nhập hợp lệ")
                )
                return@delete
            }

            val role = principal.payload.getClaim("role").asString()

            if (role != "ADMIN") {
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Chỉ Admin được phép xóa sản phẩm")
                )
                return@delete
            }

            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID sản phẩm không hợp lệ")
                )
                return@delete
            }

            handleProductErrors {
                controller.delete(id)
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Xóa sản phẩm thành công")
                )
            }
        }
    }
}

/**
 * Chuyển ngoại lệ nghiệp vụ thành phản hồi HTTP.
 *
 * @param block Khối xử lý route cần thực hiện.
 */
private suspend fun io.ktor.server.routing.RoutingContext.handleProductErrors(
    block: suspend () -> Unit
) {
    try {
        block()
    } catch (exception: ProductNotFoundException) {
        call.respond(HttpStatusCode.NotFound, mapOf("error" to exception.message))
    } catch (exception: DuplicateSkuException) {
        call.respond(HttpStatusCode.Conflict, mapOf("error" to exception.message))
    } catch (exception: DuplicateBarcodeException) {
        call.respond(HttpStatusCode.Conflict, mapOf("error" to exception.message))
    } catch (exception: InvalidProductCategoryException) {
        call.respond(HttpStatusCode.BadRequest, mapOf("error" to exception.message))
    } catch (exception: InvalidProductException) {
        call.respond(HttpStatusCode.BadRequest, mapOf("error" to exception.message))
    } catch (exception: ProductForbiddenException) {
        call.respond(HttpStatusCode.Forbidden, mapOf("error" to exception.message))
    } catch (exception: ProductException) {
        call.respond(HttpStatusCode.BadRequest, mapOf("error" to exception.message))
    } catch (exception: IllegalArgumentException) {
        call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Dữ liệu yêu cầu không hợp lệ")
        )
    } catch (exception: SQLException) {
        call.respond(
            HttpStatusCode.InternalServerError,
            mapOf("error" to "Lỗi truy cập cơ sở dữ liệu")
        )
    } catch (exception: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            mapOf("error" to "Đã xảy ra lỗi máy chủ")
        )
    }



}
