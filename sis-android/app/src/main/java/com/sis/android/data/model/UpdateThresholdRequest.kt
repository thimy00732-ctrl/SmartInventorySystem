package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class UpdateThresholdRequest(
    @SerializedName("minThreshold")
    val minThreshold: Int,

    @SerializedName("maxThreshold")
    val maxThreshold: Int
)