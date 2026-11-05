package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    @SerializedName("sku")
    val sku: String? = null,

    @SerializedName("barcode")
    val barcode: String? = null,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("categoryId")
    val categoryId: Long? = null,

    @SerializedName("supplierId")
    val supplierId: Long? = null,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("costPrice")
    val costPrice: String = "0.0",

    @SerializedName("salePrice")
    val salePrice: String = "0.0",

    @SerializedName("minStock")
    val minStock: Int = 0,

    @SerializedName("maxStock")
    val maxStock: Int = 1000
)