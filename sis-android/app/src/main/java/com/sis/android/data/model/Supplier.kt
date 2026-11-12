package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class Supplier(
    @SerializedName("supplierId")
    val supplierId: Long? = null,

    @SerializedName("supplierName")
    val supplierName: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
