package com.sis.backend.entity

data class Inventory(
    val inventoryId: Int,
    val warehouseId: Int,
    val productId: Int,
    val batchCode: String = "",
    val stockQuantity: Int = 0,
    val reservedQuantity: Int = 0,
    val availableQuantity: Int = stockQuantity - reservedQuantity
)
