package com.sis.backend.routes

import com.sis.backend.controller.StockLedgerController
import com.sis.backend.security.RolePolicy
import com.sis.backend.security.requireRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Khai báo các API sổ kho (UC36).
 * 
 * ADMIN và STAFF đều được xem (RolePolicy.STOCK_LEDGER).
 * 
 * @param stockLedgerController Controller xử lý sổ kho.
 */
fun Route.stockLedgerRoutes(
    stockLedgerController: StockLedgerController
) {
    route("/api/stock-ledger") {
        
        /**
         * UC36: Lấy sổ kho với phân trang.
         * 
         * GET /api/stock-ledger?fromDate&toDate&warehouseId&categoryId&keyword&page&size
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.STOCK_LEDGER).
         */
        requireRole(*RolePolicy.STOCK_LEDGER.toTypedArray()) {
            get {
                val fromDateStr = call.request.queryParameters["fromDate"]
                val toDateStr = call.request.queryParameters["toDate"]
                
                val fromDate = try {
                    fromDateStr?.let { LocalDate.parse(it) }
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("fromDate phải có định dạng yyyy-MM-dd")
                }
                
                val toDate = try {
                    toDateStr?.let { LocalDate.parse(it) }
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("toDate phải có định dạng yyyy-MM-dd")
                }
                
                val warehouseId = call.request.queryParameters["warehouseId"]?.toLongOrNull()
                val categoryId = call.request.queryParameters["categoryId"]?.toLongOrNull()
                val keyword = call.request.queryParameters["keyword"]?.takeIf { it.isNotBlank() }
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                
                val result = stockLedgerController.getStockLedger(
                    fromDate, toDate, warehouseId, categoryId, keyword, page, size
                )
                
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
