package com.sis.backend.routes

import com.sis.backend.controller.SupplierController
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Khai báo các API quản lý nhà cung cấp.
 */
fun Route.supplierRoutes(
    supplierController: SupplierController
) {
    route("/api/suppliers") {

        /**
         * Thêm nhà cung cấp.
         */
        post {
            supplierController.createSupplier(call)
        }

        /**
         * Lấy danh sách nhà cung cấp.
         */
        get {
            supplierController.getSuppliers(call)
        }

        /**
         * Tìm kiếm nhà cung cấp.
         *
         * GET /api/suppliers/search?keyword=...
         */
        get("/search") {
            supplierController.searchSuppliers(call)
        }

        /**
         * Xem chi tiết nhà cung cấp.
         */
        get("/{id}") {
            supplierController.getSupplierDetail(call)
        }

        /**
         * Cập nhật nhà cung cấp.
         */
        put("/{id}") {
            supplierController.updateSupplier(call)
        }

        /**
         * Ngừng hoạt động nhà cung cấp.
         */
        patch("/{id}/deactivate") {
            supplierController.deactivateSupplier(call)
        }

        /**
         *  Hợp tác lại nhà cung cấp.
         */
        patch("/{id}/reactivate") {
            supplierController.reactivateSupplier(call)
        }
    }
}