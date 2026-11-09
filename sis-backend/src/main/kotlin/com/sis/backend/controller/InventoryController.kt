package com.sis.backend.controller

import com.sis.backend.dto.InventorySearchFilter
import com.sis.backend.dto.PageResult
import com.sis.backend.dto.StockStatus
import com.sis.backend.entity.Inventory
import com.sis.backend.service.InventoryService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

/**
 * Controller xử lý các request liên quan đến tồn kho.
 *
 * @property inventoryService Service xử lý logic tồn kho.
 */
class InventoryController(
    private val inventoryService: InventoryService
) {

    /**
     * Lấy danh sách tồn kho với filter và phân trang.
     *
     * Query parameters:
     * - keyword: Tìm theo SKU, barcode, tên sản phẩm
     * - warehouseId: Lọc theo kho
     * - categoryId: Lọc theo danh mục
     * - supplierId: Lọc theo nhà cung cấp
     * - stockStatus: ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED
     * - minQuantity: Số lượng tồn tối thiểu
     * - maxQuantity: Số lượng tồn tối đa
     * - expiryDateFrom: Ngày hết hạn từ (YYYY-MM-DD)
     * - expiryDateTo: Ngày hết hạn đến (YYYY-MM-DD)
     * - sortBy: Cột sắp xếp
     * - sortDirection: ASC hoặc DESC
     * - page: Số trang
     * - size: Kích thước trang
     */
    suspend fun getInventoryPage(call: ApplicationCall) {
        try {
            val query = call.request.queryParameters

            val page = query["page"]?.toIntOrNull() ?: 1
            val size = query["size"]?.toIntOrNull() ?: 10

            val stockStatus = query["stockStatus"]?.let {
                try {
                    StockStatus.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Trạng thái tồn kho không hợp lệ. Chỉ chấp nhận: ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED"
                    )
                }
            } ?: StockStatus.ALL

            val filter = InventorySearchFilter(
                keyword = query["keyword"]?.takeIf { it.isNotBlank() },
                warehouseId = query["warehouseId"]?.let {
                    it.toLongOrNull()
                        ?: throw IllegalArgumentException("Mã kho không hợp lệ.")
                },
                warehouseName = query["warehouseName"]?.takeIf { it.isNotBlank() },
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
                stockStatus = stockStatus,
                minQuantity = query["minQuantity"]?.let {
                    it.toIntOrNull()
                        ?: throw IllegalArgumentException("Số lượng tối thiểu không hợp lệ.")
                },
                maxQuantity = query["maxQuantity"]?.let {
                    it.toIntOrNull()
                        ?: throw IllegalArgumentException("Số lượng tối đa không hợp lệ.")
                },
                expiryDateFrom = query["expiryDateFrom"],
                expiryDateTo = query["expiryDateTo"],
                sortBy = query["sortBy"] ?: "stock_quantity",
                sortDirection = query["sortDirection"] ?: "DESC"
            )

            val result: PageResult<Inventory> = inventoryService.getInventoryPage(
                filter = filter,
                page = page,
                size = size
            )

            call.respond(HttpStatusCode.OK, result)
        } catch (exception: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to exception.message)
            )
        } catch (exception: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Lỗi server: ${exception.message}")
            )
        }
    }

    /**
     * Lấy chi tiết tồn kho theo ID.
     */
    suspend fun getInventoryById(call: ApplicationCall) {
        try {
            val inventoryId = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException("ID tồn kho không hợp lệ.")

            val inventory = inventoryService.getInventoryById(inventoryId)

            call.respond(HttpStatusCode.OK, inventory)
        } catch (exception: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to exception.message)
            )
        } catch (exception: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Lỗi server: ${exception.message}")
            )
        }
    }
}
