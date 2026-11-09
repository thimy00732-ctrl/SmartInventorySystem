package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class SupplierOption(
    @SerializedName("supplierId") val supplierId: Long,
    @SerializedName("supplierName") val supplierName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null
)
