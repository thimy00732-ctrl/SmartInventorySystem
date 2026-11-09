package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class ProductSortOption(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String
)
