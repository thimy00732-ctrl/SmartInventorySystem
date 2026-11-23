package com.sis.backend.entity

import java.time.LocalDateTime

/**
 * Entity đại diện cho kho hàng/chi nhánh.
 *
 * @property warehouseId ID kho (primary key).
 * @property warehouseName Tên kho.
 * @property location Địa điểm/địa chỉ kho.
 * @property isActive Trạng thái hoạt động.
 * @property createdAt Thời điểm tạo.
 * @property updatedAt Thời điểm cập nhật cuối cùng.
 */
data class Warehouse(
    val warehouseId: Long,
    val warehouseName: String,
    val location: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
