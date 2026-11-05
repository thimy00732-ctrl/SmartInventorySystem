package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("productId", alternate = ["product_id"]) val productId: Long = 0L,
    @SerializedName("sku") val sku: String = "",
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("productName", alternate = ["product_name"]) val productName: String = "",
    @SerializedName("imageUrl", alternate = ["image_url"]) val imageUrl: String? = null,
    @SerializedName("categoryId", alternate = ["category_id"]) val categoryId: Long? = null,
    @SerializedName("supplierId", alternate = ["supplier_id"]) val supplierId: Long? = null,
    @SerializedName("unit") val unit: String = "Cái",
    @SerializedName("costPrice", alternate = ["cost_price"]) val costPrice: Double = 0.0,
    @SerializedName("salePrice", alternate = ["sale_price"]) val salePrice: Double = 0.0,
    @SerializedName("minStock", alternate = ["min_stock"]) val minStock: Int = 0,
    @SerializedName("maxStock", alternate = ["max_stock"]) val maxStock: Int = 1000,
    @SerializedName("isActive", alternate = ["is_active"]) val isActive: Boolean = true,

    @SerializedName("stockQuantity", alternate = ["stock_quantity"])
    val stockQuantity: Int = 0,

    @SerializedName("reservedQuantity", alternate = ["reserved_quantity"])
    val reservedQuantity: Int = 0,

    val description: String? = null,
    val warehouseName: String = "Kho Trung tâm",
    val categoryName: String = "Chưa phân loại"
) {
    // 🚀 Tự động tính toán số lượng khả dụng = Tồn kho - Giữ chỗ (đảm bảo không bị âm)
    val availableQuantity: Int
        get() = (stockQuantity - reservedQuantity).coerceAtLeast(0)

    val status: String
        get() = if (isActive) "ACTIVE" else "INACTIVE"

    val statusText: String
        get() = if (isActive) "Đang kinh doanh" else "Ngừng kinh doanh"
}

data class ProductPageResponse(
    val items: List<Product>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)