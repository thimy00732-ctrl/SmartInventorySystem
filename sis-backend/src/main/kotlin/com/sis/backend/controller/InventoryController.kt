
package com.sis.backend.controller

import com.sis.backend.dto.UpdateReservationRequest
import com.sis.backend.service.InventoryService

class InventoryController(
    private val inventoryService: InventoryService
) {

    fun updateReservation(
        inventoryId: Int,
        request: UpdateReservationRequest
    ) = inventoryService.updateReservation(
        inventoryId,
        request.reservedQuantity
    )
}
