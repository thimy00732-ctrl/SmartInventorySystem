package com.sis.backend.entity

import java.time.LocalDateTime

/**
 * Entity đại diện cho nhà cung cấp trong hệ thống.
 *
 * @property supplierId Mã nhà cung cấp.
 * @property supplierName Tên nhà cung cấp.
 * @property phone Số điện thoại.
 * @property email Địa chỉ email.
 * @property address Địa chỉ nhà cung cấp.
 * @property isActive Trạng thái hoạt động.
 * @property createdAt Thời điểm tạo nhà cung cấp.
 * @property updatedAt Thời điểm cập nhật gần nhất.
 */
data class Supplier(
    val supplierId: Long,
    val supplierName: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)