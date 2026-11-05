package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProductRequest(
    @SerializedName("sku")
    val sku: String,

    @SerializedName("barcode")
    val barcode: String?,

    @SerializedName("productName", alternate = ["product_name"])
    val productName: String,

    @SerializedName("imageUrl", alternate = ["image_url"])
    val imageUrl: String?,

    @SerializedName("categoryId", alternate = ["category_id"])
    val categoryId: Long?,

    @SerializedName("supplierId", alternate = ["supplier_id"])
    val supplierId: Long?,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("costPrice", alternate = ["cost_price"])
    val costPrice: String, // 👈 Đổi thành String giống CreateProductRequest

    @SerializedName("salePrice", alternate = ["sale_price"])
    val salePrice: String, // 👈 Đổi thành String giống CreateProductRequest

    @SerializedName("minStock", alternate = ["min_stock"])
    val minStock: Int,

    @SerializedName("maxStock", alternate = ["max_stock"])
    val maxStock: Int
)