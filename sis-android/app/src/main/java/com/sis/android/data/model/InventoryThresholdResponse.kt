package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class InventoryThresholdResponse(
    @SerializedName("items")
    val items: List<Inventory>,

    @SerializedName("page")
    val page: Int,

    @SerializedName("size")
    val size: Int,

    @SerializedName("totalElements")
    val totalElements: Int,

    @SerializedName("totalPages")
    val totalPages: Int
)