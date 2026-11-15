package com.sis.android.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.RoundingMode

/*
 * File này định nghĩa model dữ liệu cho module quản lý tồn kho.
 * Mục tiêu: chuẩn hóa trạng thái tồn kho, trạng thái hết hạn và thông tin lô hàng
 * để màn hình Tồn kho trong giai đoạn đầu có thể render dữ liệu nhất quán.
 */
enum class StockStatusFilter {
    ALL,
    IN_STOCK,
    OUT,
    RESERVED
}

enum class ExpiryStatus {
    NONE,
    EXPIRED,
    NEAR_EXPIRY,
    OK
}

data class InventoryItem(
    @SerializedName("inventoryId", alternate = ["inventory_id"]) val inventoryId: Long = 0L,
    @SerializedName("productId", alternate = ["product_id"]) val productId: Long = 0L,
    @SerializedName("imageUrl", alternate = ["image_url"]) val imageUrl: String? = null,
    @SerializedName("sku") val sku: String = "",
    @SerializedName("productName", alternate = ["product_name"]) val productName: String = "",
    @SerializedName("unit") val unit: String = "Cái",
    @SerializedName("warehouseId", alternate = ["warehouse_id"]) val warehouseId: Long = 0L,
    @SerializedName("warehouseName", alternate = ["warehouse_name"]) val warehouseName: String = "",
    @SerializedName("batchCode", alternate = ["batch_code"]) val batchCode: String? = null,
    @SerializedName("noBatch", alternate = ["no_batch"]) val noBatch: Boolean = false,
    @SerializedName("expiryDate", alternate = ["expiry_date"]) val expiryDate: String? = null,
    @SerializedName("manufactureDate", alternate = ["manufacture_date"]) val manufactureDate: String? = null,
    @SerializedName("updatedAt", alternate = ["updated_at"]) val updatedAt: String? = null,
    @SerializedName("expiryStatus", alternate = ["expiry_status"]) val expiryStatus: ExpiryStatus = ExpiryStatus.OK,
    @SerializedName("stockQuantity", alternate = ["stock_quantity"]) val stockQuantity: Int = 0,
    @SerializedName("reservedQuantity", alternate = ["reserved_quantity"]) val reservedQuantity: Int = 0,
    @SerializedName("availableQuantity", alternate = ["available_quantity"]) val availableQuantity: Int = stockQuantity - reservedQuantity
) {
    val displayBatch: String
        get() = if (batchCode.isNullOrBlank()) "Không theo lô" else batchCode
}

/*
 * Thông tin thuộc sản phẩm, được hiển thị cùng dòng tồn trong màn hình chi tiết.
 * Dữ liệu có thể null khi API không cung cấp barcode, danh mục hoặc nhà cung cấp.
 */
data class InventoryProductDetails(
    val barcode: String? = null,
    val categoryName: String? = null,
    val supplierName: String? = null,
    val isActive: Boolean = true,
    val minStock: Int = 0,
    val maxStock: Int = 0,
    val costPrice: BigDecimal = BigDecimal.ZERO
)

/*
 * Dòng tổng hợp theo sản phẩm dùng cho thiết lập ngưỡng và cảnh báo tồn kho.
 */
data class InventoryThresholdProduct(
    val productId: Long,
    val sku: String,
    val productName: String,
    val unit: String,
    val categoryName: String,
    val supplierName: String,
    val totalStock: Int,
    val minStock: Int,
    val maxStock: Int,
    val isActive: Boolean = true
)

/*
 * Dòng giá trị tồn kho tổng hợp theo từng kho và sản phẩm.
 */
data class InventoryValuationRow(
    val warehouseId: Long,
    val warehouseName: String,
    val productId: Long,
    val sku: String,
    val productName: String,
    val categoryName: String,
    val totalQuantity: Int,
    val currentCostPrice: BigDecimal
) {
    val stockValue: BigDecimal
        get() = currentCostPrice
            .multiply(BigDecimal.valueOf(totalQuantity.toLong()))
            .setScale(2, RoundingMode.HALF_UP)
}

enum class InventorySortBy {
    PRODUCT_NAME,
    STOCK_QUANTITY,
    EXPIRY_DATE
}
