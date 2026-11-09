package com.sis.backend.routes

import com.sis.backend.controller.InventoryController
import com.sis.backend.dto.*
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.repository.WarehouseRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Khai báo các API quản lý tồn kho.
 *
 * @param inventoryController Controller xử lý yêu cầu tồn kho.
 * @param warehouseRepository Repository kho hàng (cho dropdown).
 * @param categoryRepository Repository danh mục (cho dropdown).
 * @param supplierRepository Repository nhà cung cấp (cho dropdown).
 */
fun Route.inventoryRoutes(
    inventoryController: InventoryController,
    warehouseRepository: WarehouseRepository,
    categoryRepository: CategoryRepository,
    supplierRepository: SupplierRepository
) {
    route("/api/inventory") {

        /**
         * Lấy danh sách tồn kho có tìm kiếm, lọc và phân trang.
         *
         * Query parameters:
         * - keyword: Tìm theo SKU, barcode, tên sản phẩm
         * - warehouseId: Lọc theo kho (null = tất cả kho)
         * - categoryId: Lọc theo danh mục
         * - supplierId: Lọc theo nhà cung cấp
         * - stockStatus: ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED
         * - minQuantity: Số lượng tồn tối thiểu
         * - maxQuantity: Số lượng tồn tối đa
         * - expiryDateFrom: Ngày hết hạn từ (YYYY-MM-DD)
         * - expiryDateTo: Ngày hết hạn đến (YYYY-MM-DD)
         * - sortBy: stock_quantity, reserved_quantity, available_quantity, 
         *           product_name, expiry_date, updated_at
         * - sortDirection: ASC hoặc DESC
         * - page: Số trang (mặc định 1)
         * - size: Kích thước trang (mặc định 10)
         */
        get {
            inventoryController.getInventoryPage(call)
        }

        /**
         * Lấy chi tiết tồn kho theo ID.
         */
        get("/{id}") {
            inventoryController.getInventoryById(call)
        }

        /**
         * Lấy tất cả dropdown options cho inventory filter.
         *
         * Response gồm:
         * - warehouses: Danh sách kho
         * - categories: Danh sách danh mục
         * - suppliers: Danh sách nhà cung cấp
         * - stockStatusOptions: Các trạng thái tồn
         * - sortByOptions: Các cột sắp xếp
         * - sortDirectionOptions: Chiều sắp xếp
         */
        get("/filter-options") {
            try {
                val warehouses = warehouseRepository.findAllActiveForDropdown()
                val categories = categoryRepository.findAllActiveForDropdown()
                val suppliers = supplierRepository.findAllActiveForDropdown()

                val response = InventoryFilterOptions(
                    warehouses = warehouses,
                    categories = categories,
                    suppliers = suppliers,
                    stockStatusOptions = StockStatusOption.all(),
                    sortByOptions = InventorySortOption.all(),
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
    }
}
