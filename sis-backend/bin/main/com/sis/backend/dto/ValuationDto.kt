package com.sis.backend.dto

import com.sis.backend.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTO cho UC39: Giá trị tồn kho theo kho.
 */
@Serializable
data class WarehouseValuationResponse(
    val warehouseId: Long,
    val warehouseName: String,
    @Serializable(with = BigDecimalSerializer::class)
    val totalValue: BigDecimal,
    val totalQuantity: Int
)

/**
 * DTO cho UC39: Tổng quan giá trị tồn kho.
 */
@Serializable
data class ValuationSummaryResponse(
    @Serializable(with = BigDecimalSerializer::class)
    val totalValue: BigDecimal, // Tổng giá trị tất cả kho
    val totalQuantity: Int, // Tổng số lượng
    val productCount: Int, // Số sản phẩm DISTINCT có stock > 0
    val warehouses: List<WarehouseValuationResponse> // Giá trị từng kho, sort DESC
)

/**
 * DTO cho UC39: Chi tiết giá trị tồn kho từng dòng.
 */
@Serializable
data class ValuationItemResponse(
    val warehouseId: Long,
    val warehouseName: String,
    val productId: Long,
    val sku: String,
    val productName: String,
    val totalQuantity: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val currentCostPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val stockValue: BigDecimal // totalQuantity * currentCostPrice
)

/**
 * Response chi tiết valuation với phân trang.
 */
@Serializable
data class ValuationDetailResponse(
    val items: List<ValuationItemResponse>,
    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal, // Từ summary, không đổi khi chuyển trang
    val note: String = "Tính theo giá vốn hiện tại, không phản ánh giá vốn lịch sử",
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)
