package com.sis.backend.routes

import com.sis.backend.controller.SupplierController
import com.sis.backend.security.requireAdmin
import com.sis.backend.security.requireStaffOrAdmin
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Khai báo các API quản lý nhà cung cấp.
 *
 * Theo tài liệu phân quyền UC23-UC28:
 * - UC23: Thêm nhà cung cấp - ADMIN only
 * - UC24: Xem danh sách nhà cung cấp - Cả hai role
 * - UC25: Xem chi tiết nhà cung cấp - Cả hai role
 * - UC26: Cập nhật nhà cung cấp - ADMIN only
 * - UC27: Ngừng hợp tác nhà cung cấp - ADMIN only
 * - UC28: Tìm kiếm nhà cung cấp - Cả hai role
 */
fun Route.supplierRoutes(
    supplierController: SupplierController
) {
    route("/api/suppliers") {

        // ========== ENDPOINTS CHO CẢ ADMIN VÀ WAREHOUSE_STAFF ==========
        // UC24, UC25, UC28: Xem và tìm kiếm nhà cung cấp
        
        requireStaffOrAdmin {
            /**
             * UC24: Lấy danh sách nhà cung cấp.
             */
            get {
                supplierController.getSuppliers(call)
            }

            /**
             * UC28: Tìm kiếm nhà cung cấp.
             */
            get("/search") {
                supplierController.searchSuppliers(call)
            }

            /**
             * UC25: Xem chi tiết nhà cung cấp.
             */
            get("/{id}") {
                supplierController.getSupplierDetail(call)
            }
        }

        // ========== ENDPOINTS CHỈ CHO ADMIN ==========
        // UC23, UC26, UC27: Thêm, cập nhật, ngừng hợp tác nhà cung cấp
        
        requireAdmin {
            /**
             * UC23: Thêm nhà cung cấp - CHỈ ADMIN.
             */
            post {
                supplierController.createSupplier(call)
            }

            /**
             * UC26: Cập nhật nhà cung cấp - CHỈ ADMIN.
             */
            put("/{id}") {
                supplierController.updateSupplier(call)
            }

            /**
             * UC27: Ngừng hoạt động nhà cung cấp - CHỈ ADMIN.
             */
            patch("/{id}/deactivate") {
                supplierController.deactivateSupplier(call)
            }

            /**
             * UC27: Hợp tác lại nhà cung cấp - CHỈ ADMIN.
             */
            patch("/{id}/reactivate") {
                supplierController.reactivateSupplier(call)
            }

            /**
             * Xóa nhà cung cấp - CHỈ ADMIN.
             */
            delete("/{id}") {
                supplierController.deleteSupplier(call)
            }
        }
    }
}