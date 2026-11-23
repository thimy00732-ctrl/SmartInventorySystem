package com.sis.android.ui.inventory

/*
 * Vai trò và quyền hiển thị giao diện tồn kho theo đặc tả UI.
 * API vẫn phải tự kiểm tra quyền; đây chỉ là chính sách hiển thị phía Android.
 */
enum class InventoryUserRole {
    ADMIN,
    WAREHOUSE_STAFF
}

internal object InventoryRolePolicy {
    // Backend RolePolicy hiện cấp quyền giữ chỗ cho cả ADMIN và WAREHOUSE_STAFF.
    fun canUpdateReserved(role: InventoryUserRole): Boolean =
        role == InventoryUserRole.ADMIN || role == InventoryUserRole.WAREHOUSE_STAFF

    fun canManageThresholds(role: InventoryUserRole): Boolean =
        role == InventoryUserRole.ADMIN

    fun canViewValuation(role: InventoryUserRole): Boolean =
        role == InventoryUserRole.ADMIN
}
