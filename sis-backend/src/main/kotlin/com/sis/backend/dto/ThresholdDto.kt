package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * DTO cho UC33: Thông tin ngưỡng tồn kho của sản phẩm.
 * 
 * Hiển thị mức tồn hiện tại, ngưỡng, và mức cảnh báo.
 */
@Serializable
data class ProductThresholdResponse(
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    val totalStock: Int,
    val totalReserved: Int,
    val totalAvailable: Int,
    val minStock: Int,
    val maxStock: Int,
    val alertLevel: String // LOW_STOCK, OVER_STOCK, OK
)

/**
 * Request cho UC33: Cập nhật ngưỡng min_stock/max_stock.
 */
@Serializable
data class UpdateThresholdRequest(
    val minStock: Int,
    val maxStock: Int
)

/**
 * Response cho UC33: Sau khi cập nhật ngưỡng.
 */
@Serializable
data class UpdateThresholdResponse(
    val productId: Long,
    val sku: String,
    val productName: String,
    val minStock: Int,
    val maxStock: Int,
    val updatedAt: String // yyyy-MM-dd'T'HH:mm:ss
)
