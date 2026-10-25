package com.sis.android.data.model

data class Product(
    val productId: Long,
    val sku: String,
    val barcode: String?,
    val productName: String,
    val description: String? = null,
    val unit: String = "piece",
    val minStock: Double = 0.0,
    val categoryId: Long? = null,
    val status: String = "ACTIVE",
    // Các trường phụ trợ để hiển thị UI danh sách mượt mà không bị thiếu dữ liệu
    val stockQuantity: Double = 0.0,
    val categoryName: String = "Chưa phân loại"
)