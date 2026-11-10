
package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class UpdateReservationRequest(
    @SerializedName("reservedQuantity")
    val reservedQuantity: Int
)

data class ReservationResponse(
    @SerializedName("inventoryId")
    val inventoryId: Int,

    @SerializedName("stockQuantity")
    val stockQuantity: Int,

    @SerializedName("reservedQuantity")
    val reservedQuantity: Int,

    @SerializedName("availableQuantity")
    val availableQuantity: Int
)

data class InventoryItemResponse(
    @SerializedName("inventoryId") val inventoryId: Int,
    @SerializedName("sku") val sku: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("warehouseName") val warehouseName: String,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("reservedQuantity") val reservedQuantity: Int,
    @SerializedName("availableQuantity") val availableQuantity: Int,
    @SerializedName("status") val status: String,
    @SerializedName("initials") val initials: String
)
