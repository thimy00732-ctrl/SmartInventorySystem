package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class SortDirectionOption(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String
)
