package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class WarehouseOption(
    @SerializedName("warehouseId") val warehouseId: Long,
    @SerializedName("warehouseName") val warehouseName: String,
    @SerializedName("location") val location: String? = null
)
