package com.sis.backend.routes

import com.sis.backend.controller.InventoryController
import com.sis.backend.dto.*
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.repository.WarehouseRepository
import com.sis.backend.security.RolePolicy
import com.sis.backend.security.requireRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route

/**
 * Khai báo các API quản lý tồn kho (UC29-32).
 *
 * MỌI ENDPOINT đều yêu cầu xác thực JWT và phân quyền (requireRole).
 * Không có endpoint public.
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
        
        // UC29: Danh sách tồn kho
        // ADMIN và STAFF đều được xem
        requireRole(*RolePolicy.INVENTORY_LIST.toTypedArray()) {
            get {
                inventoryController.getInventoryList(call)
            }
        }
        
        // UC30: Chi tiết tồn kho theo ID
        // ADMIN và STAFF đều được xem
        requireRole(*RolePolicy.INVENTORY_DETAIL.toTypedArray()) {
            get("/{id}") {
                inventoryController.getInventoryDetail(call)
            }
        }
        
        // UC30: Danh sách tồn kho theo sản phẩm
        // ADMIN và STAFF đều được xem
        requireRole(*RolePolicy.INVENTORY_BY_PRODUCT.toTypedArray()) {
            get("/by-product/{productId}") {
                inventoryController.getInventoryByProduct(call)
            }
        }
        
        // UC31: Cập nhật reserved_quantity
        // ADMIN và STAFF đều được cập nhật (đã chốt với PO)
        requireRole(*RolePolicy.INVENTORY_UPDATE_RESERVED.toTypedArray()) {
            patch("/{id}/reserved") {
                inventoryController.updateReservedQuantity(call)
            }
        }
        
        /**
         * Dropdown options cho filter (không cần auth - helper endpoint).
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
