package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("productId") val productId: Long,
    @SerializedName("sku") val sku: String,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("productName") val productName: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("categoryId") val categoryId: Long?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("supplierId") val supplierId: Long?,
    @SerializedName("unit") val unit: String,
    @SerializedName("costPrice") val costPrice: String,
    @SerializedName("salePrice") val salePrice: String,
    @SerializedName("minStock") val minStock: Int,
    @SerializedName("maxStock") val maxStock: Int,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("reservedQuantity") val reservedQuantity: Int,
    @SerializedName("availableQuantity") val availableQuantity: Int
)