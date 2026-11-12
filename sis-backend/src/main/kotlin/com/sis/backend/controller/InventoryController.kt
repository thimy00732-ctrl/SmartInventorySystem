package com.sis.backend.controller

import com.sis.backend.dto.InventorySearchFilter
import com.sis.backend.dto.StockStatus
import com.sis.backend.dto.UpdateReservedRequest
import com.sis.backend.exception.ValidationException
import com.sis.backend.service.InventoryService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

/**
 * Controller xử lý HTTP request/response cho inventory (UC29-32).
 * 
 * Nhiệm vụ:
 * - Parse và validate request params
 * - Gọi service
 * - Trả response với status code phù hợp
 */
class InventoryController(
    private val inventoryService: InventoryService
) {
    /**
     * UC29: GET /api/inventory
     * Danh sách tồn kho với lọc và phân trang.
     */
    suspend fun getInventoryList(call: ApplicationCall) {
        val query = call.request.queryParameters
        
        // Parse pagination
        val page = query["page"]?.toIntOrNull() ?: 1
        val size = query["size"]?.toIntOrNull() ?: 10
        
        // Parse filters
        val filter = try {
            InventorySearchFilter(
                keyword = query["keyword"]?.trim()?.takeIf { it.isNotBlank() },
                warehouseId = query["warehouseId"]?.toLongOrNull(),
                categoryId = query["categoryId"]?.toLongOrNull(),
                supplierId = query["supplierId"]?.toLongOrNull(),
                stockStatus = query["stockStatus"]?.let { 
                    try {
                        StockStatus.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        throw ValidationException("Stock status không hợp lệ: $it. Chấp nhận: ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED")
                    }
                } ?: StockStatus.ALL,
                sortBy = query["sortBy"] ?: "product_name",
                sortDirection = query["sortDirection"] ?: "ASC"
            )
        } catch (e: NumberFormatException) {
            throw ValidationException("Tham số số không hợp lệ: ${e.message}")
        }
        
        val result = inventoryService.getInventoryPage(filter, page, size)
        call.respond(HttpStatusCode.OK, result)
    }
    
    /**
     * UC30: GET /api/inventory/{id}
     * Chi tiết tồn kho theo ID.
     */
    suspend fun getInventoryDetail(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: throw ValidationException("ID không hợp lệ.")
        
        val result = inventoryService.getInventoryDetail(id)
        call.respond(HttpStatusCode.OK, result)
    }
    
    /**
     * UC30: GET /api/inventory/by-product/{productId}
     * Danh sách tồn kho theo sản phẩm.
     */
    suspend fun getInventoryByProduct(call: ApplicationCall) {
        val productId = call.parameters["productId"]?.toLongOrNull()
            ?: throw ValidationException("Product ID không hợp lệ.")
        
        val result = inventoryService.getInventoryByProduct(productId)
        call.respond(HttpStatusCode.OK, result)
    }
    
    /**
     * UC31: PATCH /api/inventory/{id}/reserved
     * Cập nhật reserved_quantity.
     */
    suspend fun updateReservedQuantity(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: throw ValidationException("ID không hợp lệ.")
        
        val request = try {
            call.receive<UpdateReservedRequest>()
        } catch (e: Exception) {
            throw ValidationException("Request body không hợp lệ: ${e.message}")
        }
        
        val result = inventoryService.updateReservedQuantity(id, request.reservedQuantity)
        call.respond(HttpStatusCode.OK, result)
    }
}
