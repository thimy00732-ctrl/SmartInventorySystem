package com.sis.backend.security

import com.sis.backend.entity.UserRole

/**
 * Policy phân quyền tập trung cho tất cả API.
 * 
 * GOM MỌI QUYỀN Ở MỘT CHỖ DUY NHẤT để dễ thay đổi.
 * KHÔNG hardcode role trong route, controller hay service.
 * 
 * Khi PO đổi quyền, CHỈ SỬA FILE NÀY, không cần sửa nhiều file.
 * 
 * ===== QUYẾT ĐỊNH ĐÃ CHỐT (PO) =====
 * 
 * A. WAREHOUSE_STAFF ĐƯỢC:
 * - Xem danh sách tồn kho, chi tiết, by-product (UC29, UC30)
 * - PATCH /api/inventory/{id}/reserved (giữ chỗ, UC31)
 * - Xem ngưỡng sản phẩm (UC33 GET)
 * - Xem cảnh báo tồn thấp, vượt mức, tổng quan (UC34, UC35)
 * - Xem sổ kho (UC36)
 * - Xem lịch sử biến động (UC37)
 * - Xem truy vết giao dịch (UC38)
 * 
 * B. WAREHOUSE_STAFF KHÔNG ĐƯỢC:
 * - PATCH /api/products/{id}/thresholds (thiết lập ngưỡng, chỉ ADMIN)
 * - GET /api/inventory/valuation (giá trị tồn kho, chỉ ADMIN, UC39)
 * 
 * C. ADMIN:
 * - Toàn quyền (tất cả quyền của STAFF + quyền riêng ADMIN)
 */
object RolePolicy {
    
    // ===== UC29: Danh sách tồn kho =====
    val INVENTORY_LIST = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC30: Chi tiết tồn kho =====
    val INVENTORY_DETAIL = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    val INVENTORY_BY_PRODUCT = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC31: Giữ chỗ =====
    val INVENTORY_UPDATE_RESERVED = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC33: Ngưỡng tồn kho =====
    val PRODUCT_THRESHOLDS_VIEW = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    val PRODUCT_THRESHOLDS_UPDATE = setOf(UserRole.ADMIN) // CHỈ ADMIN
    
    // ===== UC34, UC35: Cảnh báo =====
    val ALERTS_LOW_STOCK = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    val ALERTS_OVER_STOCK = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    val ALERTS_SUMMARY = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC36: Sổ kho =====
    val STOCK_LEDGER = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC37: Lịch sử biến động =====
    val TRANSACTION_MOVEMENTS = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC38: Truy vết giao dịch =====
    val TRANSACTION_DETAIL = setOf(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF)
    
    // ===== UC39: Giá trị tồn kho =====
    val INVENTORY_VALUATION = setOf(UserRole.ADMIN) // CHỈ ADMIN
    val INVENTORY_VALUATION_SUMMARY = setOf(UserRole.ADMIN) // CHỈ ADMIN
    
    /**
     * Kiểm tra user có quyền thực hiện action không.
     * 
     * @param userRole Role của user hiện tại
     * @param allowedRoles Set role được phép
     * @return true nếu có quyền, false nếu không
     */
    fun hasPermission(userRole: UserRole, allowedRoles: Set<UserRole>): Boolean {
        return userRole in allowedRoles
    }
    
    /**
     * Kiểm tra user có phải ADMIN không.
     */
    fun isAdmin(userRole: UserRole): Boolean = userRole == UserRole.ADMIN
    
    /**
     * Kiểm tra user có phải STAFF không.
     */
    fun isStaff(userRole: UserRole): Boolean = userRole == UserRole.WAREHOUSE_STAFF
}
