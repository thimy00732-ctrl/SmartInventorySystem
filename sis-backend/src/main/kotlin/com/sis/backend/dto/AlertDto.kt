package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * DTO cho UC34: Cảnh báo tồn kho thấp.
 * 
 * Danh sách sản phẩm có tổng tồn < min_stock.
 */
@Serializable
data class LowStockAlertResponse(
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    val supplierId: Long?, // Nhà cung cấp để hỗ trợ đặt hàng bổ sung
    val supplierName: String?,
    val totalStock: Int,
    val minStock: Int,
    val missingQuantity: Int, // minStock - totalStock
    val level: String // "OUT_OF_STOCK" (totalStock = 0) | "LOW" (< minStock)
)

/**
 * DTO cho UC35: Cảnh báo tồn kho vượt mức.
 * 
 * Danh sách sản phẩm có tổng tồn > max_stock.
 */
@Serializable
data class OverStockAlertResponse(
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    val totalStock: Int,
    val maxStock: Int,
    val excessQuantity: Int, // totalStock - maxStock
    val excessPercent: String? // (total/max - 1)*100, null nếu max=0
)

/**
 * DTO cho UC34, UC35: Tổng quan cảnh báo.
 * 
 * Tổng số sản phẩm tồn thấp, hết hàng, vượt mức.
 */
@Serializable
data class AlertSummaryResponse(
    val lowStockCount: Int, // Tổng số sản phẩm dưới ngưỡng (bao gồm hết hàng)
    val outOfStockCount: Int, // Số sản phẩm hết hàng (totalStock = 0)
    val overStockCount: Int // Số sản phẩm vượt mức
)
