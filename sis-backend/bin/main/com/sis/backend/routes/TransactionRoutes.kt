package com.sis.backend.routes

import com.sis.backend.controller.TransactionController
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
 * Khai báo các API transactions (UC37, UC38).
 * 
 * ADMIN và STAFF đều được xem.
 * 
 * @param transactionController Controller xử lý transactions.
 */
fun Route.transactionRoutes(
    transactionController: TransactionController
) {
    route("/api/transactions") {
        
        /**
         * UC37: Lấy lịch sử biến động với phân trang.
         * 
         * GET /api/transactions/movements?productId&warehouseId&type&status&userId&fromDate&toDate&sortBy&sortDir&page&size
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.TRANSACTION_MOVEMENTS).
         */
        requireRole(*RolePolicy.TRANSACTION_MOVEMENTS.toTypedArray()) {
            get("/movements") {
                val productId = call.request.queryParameters["productId"]?.toLongOrNull()
                val warehouseId = call.request.queryParameters["warehouseId"]?.toLongOrNull()
                val type = call.request.queryParameters["type"]?.takeIf { it.isNotBlank() }
                val status = call.request.queryParameters["status"]?.takeIf { it.isNotBlank() }
                val userId = call.request.queryParameters["userId"]?.toLongOrNull()
                
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
                
                val sortBy = call.request.queryParameters["sortBy"] ?: "txTime"
                val sortDir = call.request.queryParameters["sortDir"] ?: "DESC"
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                
                val result = transactionController.getTransactionMovements(
                    productId, warehouseId, type, status, userId,
                    fromDate, toDate, sortBy, sortDir, page, size
                )
                
                call.respond(HttpStatusCode.OK, result)
            }
        }
        
        /**
         * UC38: Lấy chi tiết giao dịch theo ID.
         * 
         * GET /api/transactions/{id}
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.TRANSACTION_DETAIL).
         */
        requireRole(*RolePolicy.TRANSACTION_DETAIL.toTypedArray()) {
            get("/{id}") {
                val transactionId = call.parameters["id"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("Transaction ID không hợp lệ")
                
                val result = transactionController.getTransactionDetail(transactionId)
                
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
