package com.sis.backend.mapper

import com.sis.backend.dto.InventoryDetailResponse
import com.sis.backend.dto.InventoryItemResponse
import com.sis.backend.dto.UpdateReservedResponse
import com.sis.backend.entity.ExpiryStatus
import com.sis.backend.entity.Inventory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Mapper chuyển Inventory entity sang DTO.
 * 
 * Extension functions để dễ dùng: `inventory.toItemResponse(nearExpiryDays)`.
 */

/**
 * Chuyển Inventory sang InventoryItemResponse (cho UC29, UC30 by-product).
 * 
 * @param nearExpiryDays Ngưỡng cận hạn (từ config, mặc định 30).
 */
fun Inventory.toItemResponse(nearExpiryDays: Int = 30): InventoryItemResponse {
    return InventoryItemResponse(
        inventoryId = this.inventoryId,
        productId = this.productId,
        imageUrl = null, // TODO: lấy từ products.image_url nếu cần
        sku = this.sku,
        productName = this.productName,
        unit = this.unit,
        warehouseId = this.warehouseId,
        warehouseName = this.warehouseName,
        batchCode = this.batchCode,
        noBatch = this.batchCode.isEmpty(),
        expiryDate = this.expiryDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        expiryStatus = calculateExpiryStatus(this.expiryDate, nearExpiryDays).name,
        stockQuantity = this.stockQuantity,
        reservedQuantity = this.reservedQuantity,
        availableQuantity = this.availableQuantity
    )
}

/**
 * Chuyển Inventory sang InventoryDetailResponse (cho UC30 chi tiết).
 * 
 * @param nearExpiryDays Ngưỡng cận hạn.
 * @param minStock Ngưỡng tồn tối thiểu (từ products).
 * @param maxStock Ngưỡng tồn tối đa (từ products).
 * @param isActive Trạng thái sản phẩm (từ products).
 */
fun Inventory.toDetailResponse(
    nearExpiryDays: Int = 30,
    minStock: Int,
    maxStock: Int,
    isActive: Boolean
): InventoryDetailResponse {
    return InventoryDetailResponse(
        // Dòng tồn
        inventoryId = this.inventoryId,
        batchCode = this.batchCode,
        noBatch = this.batchCode.isEmpty(),
        manufactureDate = this.manufactureDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        expiryDate = this.expiryDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        expiryStatus = calculateExpiryStatus(this.expiryDate, nearExpiryDays).name,
        stockQuantity = this.stockQuantity,
        reservedQuantity = this.reservedQuantity,
        availableQuantity = this.availableQuantity,
        updatedAt = this.updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        
        // Kho
        warehouseId = this.warehouseId,
        warehouseName = this.warehouseName,
        
        // Sản phẩm
        productId = this.productId,
        sku = this.sku,
        barcode = this.barcode,
        productName = this.productName,
        imageUrl = null, // TODO: lấy từ products nếu cần
        unit = this.unit,
        minStock = minStock,
        maxStock = maxStock,
        isActive = isActive,
        
        // Danh mục và nhà cung cấp
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        supplierId = this.supplierId,
        supplierName = this.supplierName
    )
}

/**
 * Chuyển Inventory sang UpdateReservedResponse (cho UC31).
 */
fun Inventory.toUpdateReservedResponse(): UpdateReservedResponse {
    return UpdateReservedResponse(
        inventoryId = this.inventoryId,
        stockQuantity = this.stockQuantity,
        reservedQuantity = this.reservedQuantity,
        availableQuantity = this.availableQuantity,
        updatedAt = this.updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}

/**
 * Tính ExpiryStatus dựa trên expiry_date so với hôm nay.
 * 
 * Quy tắc (spec UC29):
 * - expiry_date = null → NONE
 * - expiry_date < hôm nay → EXPIRED
 * - expiry_date <= hôm nay + nearExpiryDays → NEAR_EXPIRY
 * - còn lại → OK
 * 
 * @param expiryDate Ngày hết hạn (có thể null).
 * @param nearExpiryDays Ngưỡng cận hạn (mặc định 30 ngày).
 * @return ExpiryStatus tương ứng.
 */
fun calculateExpiryStatus(expiryDate: LocalDate?, nearExpiryDays: Int): ExpiryStatus {
    if (expiryDate == null) {
        return ExpiryStatus.NONE
    }
    
    val today = LocalDate.now()
    val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate)
    
    return when {
        daysUntilExpiry < 0 -> ExpiryStatus.EXPIRED // Đã hết hạn
        daysUntilExpiry <= nearExpiryDays -> ExpiryStatus.NEAR_EXPIRY // Gần hết hạn
        else -> ExpiryStatus.OK // Còn tốt
    }
}
