package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("productId") val productId: Long = 0L,
    @SerializedName("sku") val sku: String = "",
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("productName") val productName: String = "",
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("supplierId") val supplierId: Long? = null,
    @SerializedName("unit") val unit: String = "Cái",
    @SerializedName("costPrice") val costPrice: Double = 0.0,
    @SerializedName("salePrice") val salePrice: Double = 0.0,
    @SerializedName("minStock") val minStock: Double = 0.0,
    @SerializedName("maxStock") val maxStock: Double = 1000.0,
    @SerializedName("isActive") val isActive: Boolean = true,

    // 3 trường tồn kho từ API backend
    @SerializedName("stockQuantity") val stockQuantity: Double = 0.0,
    @SerializedName("reservedQuantity") val reservedQuantity: Double = 0.0,
    @SerializedName("availableQuantity") val availableQuantity: Double = 0.0,

    // Các trường mở rộng (không có trong API)
    val description: String? = null,
    val warehouseName: String = "Kho Trung tâm",
    val categoryName: String = "Chưa phân loại"
) {
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
