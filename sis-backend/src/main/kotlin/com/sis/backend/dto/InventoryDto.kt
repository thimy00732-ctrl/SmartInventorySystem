
package com.sis.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReservationRequest(
    val reservedQuantity: Int
)

@Serializable
data class ReservationResponse(
    val inventoryId: Int,
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int
)

@Serializable
data class InventoryItemResponse(
    val inventoryId: Int,
    val sku: String,
    val productName: String,
    val warehouseName: String,
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val status: String,
    val initials: String
)
