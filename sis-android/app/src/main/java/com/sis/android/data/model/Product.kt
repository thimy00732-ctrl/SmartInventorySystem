package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("product_id") val productId: Long = 0L,
    @SerializedName("sku") val sku: String = "",
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("product_name") val productName: String = "",
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("category_id") val categoryId: Long? = null,
    @SerializedName("supplier_id") val supplierId: Long? = null,
    @SerializedName("unit") val unit: String = "Cái",
    @SerializedName("cost_price") val costPrice: Double = 0.0,
    @SerializedName("sale_price") val salePrice: Double = 0.0,
    @SerializedName("min_stock") val minStock: Double = 0.0,
    @SerializedName("max_stock") val maxStock: Double = 1000.0,
    @SerializedName("is_active") val isActive: Int = 1,

    // Các trường dữ liệu mở rộng cho giao diện
    val description: String? = null,
    val stockQuantity: Double = 0.0,
    val reservedQuantity: Double = 0.0,
    val availableQuantity: Double = stockQuantity - reservedQuantity,
    val warehouseName: String = "Kho Trung tâm",
    val categoryName: String = "Chưa phân loại"
) {
    val status: String
        get() = if (isActive == 1) "ACTIVE" else "INACTIVE"

    val statusText: String
        get() = if (isActive == 1) "Đang kinh doanh" else "Ngừng kinh doanh"

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
        isActive = if (status == "ACTIVE" || status == "1" || status.lowercase() == "true") 1 else 0,
        description = description,
        stockQuantity = stockQuantity.toDouble(),
        reservedQuantity = reservedQuantity.toDouble(),
        availableQuantity = stockQuantity.toDouble() - reservedQuantity.toDouble(),
        warehouseName = warehouseName,
        categoryName = categoryName
    )
}