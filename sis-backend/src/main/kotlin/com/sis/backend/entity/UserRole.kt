package com.sis.backend.entity

/**
 * Vai trò người dùng trong hệ thống.
 * 
 * Khớp với enum 'role' trong bảng users của schema:
 * ENUM('ADMIN', 'WAREHOUSE_STAFF')
 * 
 * Dùng cho phân quyền API (RoleAuthorization) và JWT claim "role".
 */
enum class UserRole {
    /**
     * Quản trị viên - toàn quyền.
     * 
     * Được phép:
     * - Tất cả quyền của WAREHOUSE_STAFF
     * - PATCH /api/products/{id}/thresholds (thiết lập ngưỡng)
     * - GET /api/inventory/valuation (xem giá trị tồn kho)
     */
    ADMIN,
    
    /**
     * Nhân viên kho - quyền hạn chế.
     * 
     * Được phép:
     * - Xem danh sách tồn kho, chi tiết, cảnh báo
     * - PATCH /api/inventory/{id}/reserved (giữ chỗ)
     * - Xem ngưỡng sản phẩm (không sửa)
     * - Xem sổ kho, lịch sử biến động, truy vết giao dịch
     * 
     * KHÔNG được phép:
     * - Thiết lập ngưỡng min_stock/max_stock
     * - Xem giá trị tồn kho (valuation)
     */
    WAREHOUSE_STAFF;
    
    companion object {
        /**
         * Parse string từ JWT claim thành UserRole.
         * 
         * @param value Chuỗi role từ JWT (ví dụ: "ADMIN", "WAREHOUSE_STAFF")
         * @return UserRole tương ứng
         * @throws IllegalArgumentException nếu role không hợp lệ
         */
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Role không hợp lệ: $value. Chỉ chấp nhận ADMIN hoặc WAREHOUSE_STAFF.")
            }
        }
    }
}
