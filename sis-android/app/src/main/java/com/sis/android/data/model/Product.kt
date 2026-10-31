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
    @SerializedName("isActive") val isActive: Boolean = true,  // <-- Đổi từ Int sang Boolean

    // Các trường mở rộng giữ nguyên
    val description: String? = null,
    val stockQuantity: Double = 0.0,
    val reservedQuantity: Double = 0.0,
    val availableQuantity: Double = stockQuantity - reservedQuantity,
    val warehouseName: String = "Kho Trung tâm",
    val categoryName: String = "Chưa phân loại"
) {
    val status: String
        get() = if (isActive) "ACTIVE" else "INACTIVE"

    val statusText: String
        get() = if (isActive) "Đang kinh doanh" else "Ngừng kinh doanh"



// Constructor phụ hỗ trợ khởi tạo linh hoạt từ Form Screen
    constructor(
        productId: Long = 0L,
        sku: String = "",
        barcode: String? = null,
        productName: String = "",
        imageUrl: String? = null,
        categoryId: Any? = null,
        supplierId: Long? = null,
        unit: String = "Cái",
        costPrice: Number = 0.0,
        salePrice: Number = 0.0,
        minStock: Number = 0.0,
        maxStock: Number = 1000.0,
        status: String = "ACTIVE",
        description: String? = null,
        stockQuantity: Number = 0.0,
        reservedQuantity: Number = 0.0,
        warehouseName: String = "Kho Trung tâm",
        categoryName: String = "Chưa phân loại"
    ) : this(
        productId = productId,
        sku = sku,
        barcode = barcode,
        productName = productName,
        imageUrl = imageUrl,
        categoryId = when (categoryId) {
            is Int -> categoryId.toLong()
            is Long -> categoryId
            is String -> categoryId.toLongOrNull()
            else -> null
        },
        supplierId = supplierId,
        unit = unit,
        costPrice = costPrice.toDouble(),
        salePrice = salePrice.toDouble(),
        minStock = minStock.toDouble(),
        maxStock = maxStock.toDouble(),
        isActive = (status == "ACTIVE" || status == "1" || status.lowercase() == "true"),

        description = description,
        stockQuantity = stockQuantity.toDouble(),
        reservedQuantity = reservedQuantity.toDouble(),
        availableQuantity = stockQuantity.toDouble() - reservedQuantity.toDouble(),
        warehouseName = warehouseName,
        categoryName = categoryName
    )


}

data class ProductPageResponse(
    val items: List<Product>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)