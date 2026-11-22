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
                
                // Validation: fromDate and toDate are required
                if (fromDateStr.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "fromDate là bắt buộc (định dạng yyyy-MM-dd)"
                    ))
                    return@get
                }
                
                if (toDateStr.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "toDate là bắt buộc (định dạng yyyy-MM-dd)"
                    ))
                    return@get
                }
                
                val fromDate = try {
                    LocalDate.parse(fromDateStr)
                } catch (e: DateTimeParseException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "fromDate phải có định dạng yyyy-MM-dd"
                    ))
                    return@get
                    null
                }
                
                val toDate = try {
                    LocalDate.parse(toDateStr)
                } catch (e: DateTimeParseException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "toDate phải có định dạng yyyy-MM-dd"
                    ))
                    return@get
                    null
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
