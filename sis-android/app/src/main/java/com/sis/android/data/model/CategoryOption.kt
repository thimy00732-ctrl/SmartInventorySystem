package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class CategoryOption(
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("description") val description: String? = null
)
