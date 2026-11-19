package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class Inventory(
    @SerializedName("inventoryId")
    val inventoryId: Int,

    @SerializedName("productId")
    val productId: Int,

    @SerializedName("sku")
    val sku: String,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("stockQuantity")
    val stockQuantity: Int,

    @SerializedName("minThreshold")
    val minThreshold: Int,

    @SerializedName("maxThreshold")
    val maxThreshold: Int,

    @SerializedName("alertLevel")
    val alertLevel: String
)