package com.sis.backend.routes

import com.sis.backend.controller.ProductController
import com.sis.backend.controller.ThresholdController
import com.sis.backend.dto.*
import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.product.UpdateProductRequest
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.security.RolePolicy
import com.sis.backend.security.requireRole
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

/**
 * Khai báo các API quản lý sản phẩm.
 *
 * @param productController Controller xử lý yêu cầu sản phẩm.
 * @param thresholdController Controller xử lý ngưỡng tồn kho (UC33).
 * @param categoryRepository Repository danh mục (cho dropdown).
 * @param supplierRepository Repository nhà cung cấp (cho dropdown).
 */
fun Route.productRoutes(
    productController: ProductController,
    thresholdController: ThresholdController? = null,
    categoryRepository: CategoryRepository? = null,
    supplierRepository: SupplierRepository? = null
) {
    route("/api/products") {

        /**
         * Lấy danh sách sản phẩm có tìm kiếm, lọc và phân trang.
         *
         * Các tham số:
         * keyword, categoryId, categoryName, supplierId, supplierName, isActive,
         * minPrice, maxPrice, sortBy, sortDirection, page, size.
         */
        get {
            val query = call.request.queryParameters

            val page = query["page"]?.toIntOrNull() ?: 1
            val size = query["size"]?.toIntOrNull() ?: 10

            val filter = ProductSearchFilter(
                keyword = query["keyword"]?.takeIf { it.isNotBlank() },
                warehouseId = query["warehouseId"]?.let {
                    it.toLongOrNull()
                        ?: throw IllegalArgumentException("Mã kho không hợp lệ.")
                },
                categoryId = query["categoryId"]?.let {
                    it.toLongOrNull()
                        ?: throw IllegalArgumentException("Mã danh mục không hợp lệ.")
                },
                categoryName = query["categoryName"]?.takeIf { it.isNotBlank() },
                supplierId = query["supplierId"]?.let {
                    it.toLongOrNull()
                        ?: throw IllegalArgumentException("Mã nhà cung cấp không hợp lệ.")
                },
                supplierName = query["supplierName"]?.takeIf { it.isNotBlank() },
                isActive = query["isActive"]?.let {
                    it.toBooleanStrictOrNull()
                        ?: throw IllegalArgumentException("Trạng thái hoạt động không hợp lệ.")
                },
                minPrice = query["minPrice"]?.let {
                    it.toBigDecimalOrNull()
                        ?: throw IllegalArgumentException("Giá bán tối thiểu không hợp lệ.")
                },
                maxPrice = query["maxPrice"]?.let {
                    it.toBigDecimalOrNull()
                        ?: throw IllegalArgumentException("Giá bán tối đa không hợp lệ.")
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
         * Lấy tất cả dropdown options cho product filter.
         *
         * Response gồm:
         * - categories: Danh sách danh mục
         * - suppliers: Danh sách nhà cung cấp
         * - sortByOptions: Các cột sắp xếp
         * - sortDirectionOptions: Chiều sắp xếp
         */
        get("/filter-options") {
            try {
                val categories = categoryRepository?.findAllActiveForDropdown() ?: emptyList()
                val suppliers = supplierRepository?.findAllActiveForDropdown() ?: emptyList()

                val response = ProductFilterOptions(
                    categories = categories,
                    suppliers = suppliers,
                    sortByOptions = ProductSortOption.all(),
                    sortDirectionOptions = SortDirectionOption.all()
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (exception: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Lỗi khi lấy filter options: ${exception.message}")
                )
            }
        }

        /**
         * Lấy danh sách sản phẩm theo API cũ.
         *
         * Sử dụng khi cần lấy toàn bộ danh sách mà không phân trang.
         */
        get("/all") {
            val keyword = call.request.queryParameters["keyword"]

            val includeInactive = call.request.queryParameters["includeInactive"]
                ?.toBooleanStrictOrNull() ?: false

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
                ?: throw IllegalArgumentException("Thiếu mã sản phẩm.")

            val product = productController.lookupProduct(code)

            call.respond(HttpStatusCode.OK, product)
        }

        /**
         * Lấy thông tin chi tiết sản phẩm.
         */
        get("/{id}") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Mã sản phẩm không hợp lệ.")

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
                ?: throw IllegalArgumentException("Mã sản phẩm không hợp lệ.")

            val request = call.receive<UpdateProductRequest>()

            val product = productController.updateProduct(productId, request)

            call.respond(HttpStatusCode.OK, product)
        }

        /**
         * Ngừng kinh doanh sản phẩm.
         */
        patch("/{id}/deactivate") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Mã sản phẩm không hợp lệ.")

            productController.deactivateProduct(productId)

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Ngừng kinh doanh sản phẩm thành công.")
            )
        }

        /**
         * Kích hoạt lại sản phẩm.
         */
        patch("/{id}/reactivate") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Mã sản phẩm không hợp lệ.")

            productController.reactivateProduct(productId)

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Kích hoạt lại sản phẩm thành công.")
            )
        }

        /**
         * Xóa sản phẩm theo ID.
         */
        delete("/{id}") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Mã sản phẩm không hợp lệ.")

            productController.deleteProduct(productId)

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Xóa sản phẩm thành công.")
            )
        }

        /**
         * UC33 GET: Lấy danh sách sản phẩm có thiết lập ngưỡng tồn kho.
         * 
         * GET /api/products/thresholds?warehouseId={id}
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.PRODUCT_THRESHOLDS_VIEW).
         */
        requireRole(*RolePolicy.PRODUCT_THRESHOLDS_VIEW.toTypedArray()) {
            get("/thresholds") {
                val warehouseId = call.request.queryParameters["warehouseId"]?.toLongOrNull()
                
                val result = thresholdController?.getProductsWithThresholds(warehouseId)
                    ?: emptyList()
                
                call.respond(HttpStatusCode.OK, result)
            }
        }

        /**
         * UC33 PATCH: Cập nhật ngưỡng tồn kho cho sản phẩm.
         * 
         * PATCH /api/products/{id}/thresholds
         * 
         * CHỈ ADMIN được gọi (RolePolicy.PRODUCT_THRESHOLDS_UPDATE).
         */
        requireRole(*RolePolicy.PRODUCT_THRESHOLDS_UPDATE.toTypedArray()) {
            patch("/{id}/thresholds") {
                val productId = call.parameters["id"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("Mã sản phẩm không hợp lệ.")
                
                val request = call.receive<UpdateThresholdRequest>()
                
                val result = thresholdController?.updateThresholds(productId, request)
                    ?: throw IllegalStateException("ThresholdController chưa được khởi tạo.")
                
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
