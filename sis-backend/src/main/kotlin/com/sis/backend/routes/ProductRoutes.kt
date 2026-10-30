package com.sis.backend.routes

import com.sis.backend.controller.ProductController
import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.product.UpdateProductRequest
import com.sis.backend.repository.ProductSearchFilter
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.math.BigDecimal

/**
 * Khai báo các API quản lý sản phẩm.
 *
 * @param productController Controller xử lý yêu cầu sản phẩm.
 */
fun Route.productRoutes(
    productController: ProductController
) {
    route("/api/products") {

        /**
         * Lấy danh sách sản phẩm có tìm kiếm, lọc và phân trang.
         *
         * Các tham số:
         * keyword, categoryId, supplierId, isActive,
         * minPrice, maxPrice, sortBy, sortDirection,
         * page, size.
         */
        get {
            val query = call.request.queryParameters

            val page = query["page"]?.toIntOrNull() ?: 1
            val size = query["size"]?.toIntOrNull() ?: 10

            val filter = ProductSearchFilter(
                keyword = query["keyword"]?.takeIf {
                    it.isNotBlank()
                },
                categoryId = query["categoryId"]?.let {
                    it.toLongOrNull()
                        ?: throw IllegalArgumentException(
                            "Mã danh mục không hợp lệ."
                        )
                },
                supplierId = query["supplierId"]?.let {
                    it.toLongOrNull()
                        ?: throw IllegalArgumentException(
                            "Mã nhà cung cấp không hợp lệ."
                        )
                },
                isActive = query["isActive"]?.let {
                    it.toBooleanStrictOrNull()
                        ?: throw IllegalArgumentException(
                            "Trạng thái hoạt động không hợp lệ."
                        )
                },
                minPrice = query["minPrice"]?.let {
                    it.toBigDecimalOrNull()
                        ?: throw IllegalArgumentException(
                            "Giá bán tối thiểu không hợp lệ."
                        )
                },
                maxPrice = query["maxPrice"]?.let {
                    it.toBigDecimalOrNull()
                        ?: throw IllegalArgumentException(
                            "Giá bán tối đa không hợp lệ."
                        )
                },
                sortBy = query["sortBy"] ?: "created_at",
                sortDirection = query["sortDirection"] ?: "DESC"
            )

            val products = productController.getProductsPage(
                filter = filter,
                page = page,
                size = size
            )

            call.respond(HttpStatusCode.OK, products)
        }

        /**
         * Lấy danh sách sản phẩm theo API cũ.
         *
         * Sử dụng khi cần lấy toàn bộ danh sách mà không phân trang.
         */
        get("/all") {
            val keyword = call.request.queryParameters["keyword"]

            val includeInactive = call.request.queryParameters[
                "includeInactive"
            ]?.toBooleanStrictOrNull() ?: false

            val products = productController.getAllProducts(
                keyword = keyword,
                includeInactive = includeInactive
            )

            call.respond(HttpStatusCode.OK, products)
        }

        /**
         * Tra cứu nhanh sản phẩm bằng SKU hoặc barcode.
         */
        get("/lookup/{code}") {
            val code = call.parameters["code"]
                ?: throw IllegalArgumentException(
                    "Thiếu mã sản phẩm."
                )

            val product = productController.lookupProduct(code)

            call.respond(HttpStatusCode.OK, product)
        }

        /**
         * Lấy thông tin chi tiết sản phẩm.
         */
        get("/{id}") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ."
                )

            val product = productController.getProductDetail(productId)

            call.respond(HttpStatusCode.OK, product)
        }

        /**
         * Thêm sản phẩm mới.
         */
        post {
            val request = call.receive<CreateProductRequest>()
            val product = productController.createProduct(request)

            call.respond(HttpStatusCode.Created, product)
        }

        /**
         * Cập nhật thông tin sản phẩm.
         */
        put("/{id}") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ."
                )

            val request = call.receive<UpdateProductRequest>()

            val product = productController.updateProduct(
                productId,
                request
            )

            call.respond(HttpStatusCode.OK, product)
        }

        /**
         * Ngừng kinh doanh sản phẩm.
         */
        patch("/{id}/deactivate") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ."
                )

            productController.deactivateProduct(productId)

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "message" to "Ngừng kinh doanh sản phẩm thành công."
                )
            )
        }

        /**
         * Kích hoạt lại sản phẩm.
         */
        patch("/{id}/reactivate") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ."
                )

            productController.reactivateProduct(productId)

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "message" to "Kích hoạt lại sản phẩm thành công."
                )
            )
        }
    }
}
