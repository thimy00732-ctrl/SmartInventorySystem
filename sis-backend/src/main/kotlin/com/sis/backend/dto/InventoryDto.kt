package com.sis.backend.dto

import com.sis.backend.entity.ExpiryStatus
import com.sis.backend.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTO cho UC29: Danh sách tồn kho (từng dòng inventory).
 * 
 * Mỗi dòng là một tổ hợp kho/sản phẩm/lô.
 */
@Serializable
data class InventoryItemResponse(
    val inventoryId: Long,
    val productId: Long,
    val imageUrl: String?,
    val sku: String,
    val productName: String,
    val unit: String,
    val warehouseId: Long,
    val warehouseName: String,
    val batchCode: String,
    val noBatch: Boolean, // true nếu batch_code == ""
    val expiryDate: String?, // yyyy-MM-dd hoặc null
    val expiryStatus: String, // ExpiryStatus.name: NONE, EXPIRED, NEAR_EXPIRY, OK
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int
)

/**
 * DTO cho UC30: Chi tiết tồn kho theo ID.
 * 
 * Bao gồm đầy đủ thông tin kho, sản phẩm, danh mục, nhà cung cấp.
 */
@Serializable
data class InventoryDetailResponse(
    // Thông tin dòng tồn
    val inventoryId: Long,
    val batchCode: String,
    val noBatch: Boolean,
    val manufactureDate: String?, // yyyy-MM-dd
    val expiryDate: String?,
    val expiryStatus: String,
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val updatedAt: String, // yyyy-MM-dd'T'HH:mm:ss
    
    // Thông tin kho
    val warehouseId: Long,
    val warehouseName: String,
    
    // Thông tin sản phẩm
    val productId: Long,
    val sku: String,
    val barcode: String?,
    val productName: String,
    val imageUrl: String?,
    val unit: String,
    val minStock: Int,
    val maxStock: Int,
    val isActive: Boolean,
    
    // Thông tin danh mục và nhà cung cấp (có thể null)
    val categoryId: Long?,
    val categoryName: String?,
    val supplierId: Long?,
    val supplierName: String?
)

/**
 * DTO cho UC30: Danh sách tồn kho theo sản phẩm.
 * 
 * Bao gồm các dòng inventory và dòng tổng.
 */
@Serializable
data class InventoryByProductResponse(
    val items: List<InventoryItemResponse>,
    val totalStock: Int,
    val totalReserved: Int,
    val totalAvailable: Int
)

/**
 * Request cho UC31: Cập nhật reserved_quantity.
 */
@Serializable
data class UpdateReservedRequest(
    val reservedQuantity: Int
)

/**
 * Response cho UC31: Sau khi cập nhật reserved_quantity.
 */
@Serializable
data class UpdateReservedResponse(
    val inventoryId: Long,
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val updatedAt: String // yyyy-MM-dd'T'HH:mm:ss
)
