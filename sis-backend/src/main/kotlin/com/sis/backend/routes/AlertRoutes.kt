package com.sis.backend.routes

import com.sis.backend.controller.AlertController
import com.sis.backend.security.RolePolicy
import com.sis.backend.security.requireRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Khai báo các API cảnh báo tồn kho (UC34, UC35).
 * 
 * @param alertController Controller xử lý cảnh báo.
 */
fun Route.alertRoutes(
    alertController: AlertController
) {
    route("/api/alerts") {
        
        /**
         * UC34: Lấy danh sách sản phẩm có tồn kho thấp.
         * 
         * GET /api/alerts/low-stock
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.ALERTS_LOW_STOCK).
         */
        requireRole(*RolePolicy.ALERTS_LOW_STOCK.toTypedArray()) {
            get("/low-stock") {
                val result = alertController.getLowStockAlerts()
                call.respond(HttpStatusCode.OK, result)
            }
        }
        
        /**
         * UC35: Lấy danh sách sản phẩm có tồn kho vượt mức.
         * 
         * GET /api/alerts/over-stock
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.ALERTS_OVER_STOCK).
         */
        requireRole(*RolePolicy.ALERTS_OVER_STOCK.toTypedArray()) {
            get("/over-stock") {
                val result = alertController.getOverStockAlerts()
                call.respond(HttpStatusCode.OK, result)
            }
        }
        
        /**
         * UC35: Lấy tổng quan cảnh báo.
         * 
         * GET /api/alerts/summary
         * 
         * ADMIN và STAFF đều được xem (RolePolicy.ALERTS_SUMMARY).
         */
        requireRole(*RolePolicy.ALERTS_SUMMARY.toTypedArray()) {
            get("/summary") {
                val result = alertController.getAlertSummary()
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
