package com.sis.backend.routes

import com.sis.backend.controller.ValuationController
import com.sis.backend.security.RolePolicy
import com.sis.backend.security.requireRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Khai báo các API giá trị tồn kho (UC39).
 * 
 * CHỈ ADMIN được gọi (RolePolicy.INVENTORY_VALUATION).
 * 
 * @param valuationController Controller xử lý valuation.
 */
fun Route.valuationRoutes(
    valuationController: ValuationController
) {
    route("/api/inventory/valuation") {
        
        /**
         * UC39: Lấy tổng quan giá trị tồn kho.
         * 
         * GET /api/inventory/valuation/summary?warehouseId&categoryId
         * 
         * CHỈ ADMIN (RolePolicy.INVENTORY_VALUATION_SUMMARY).
         * 
         * Note: warehouseId không dùng cho summary, chỉ lọc categoryId.
         */
        requireRole(*RolePolicy.INVENTORY_VALUATION_SUMMARY.toTypedArray()) {
            get("/summary") {
                val warehouseId = call.request.queryParameters["warehouseId"]?.toLongOrNull()
                val categoryId = call.request.queryParameters["categoryId"]?.toLongOrNull()
                
                val result = valuationController.getSummary(warehouseId, categoryId)
                call.respond(HttpStatusCode.OK, result)
            }
        }
        
        /**
         * UC39: Lấy chi tiết giá trị tồn kho với phân trang.
         * 
         * GET /api/inventory/valuation?warehouseId&categoryId&keyword&page&size
         * 
         * CHỈ ADMIN (RolePolicy.INVENTORY_VALUATION).
         */
        requireRole(*RolePolicy.INVENTORY_VALUATION.toTypedArray()) {
            get {
                val warehouseId = call.request.queryParameters["warehouseId"]?.toLongOrNull()
                val categoryId = call.request.queryParameters["categoryId"]?.toLongOrNull()
                val keyword = call.request.queryParameters["keyword"]?.takeIf { it.isNotBlank() }
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                
                val result = valuationController.getDetail(warehouseId, categoryId, keyword, page, size)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
