
package com.sis.backend.service

import com.sis.backend.dto.ReservationResponse
import com.sis.backend.repository.InventoryRepository

class InventoryService(
    private val inventoryRepository: InventoryRepository
) {

    fun updateReservation(
        inventoryId: Int,
        newReservedQuantity: Int
    ): ReservationResponse {

        if (newReservedQuantity < 0) {
            throw IllegalArgumentException(
                "Số lượng giữ chỗ không được âm"
            )
        }

        val inventory = inventoryRepository.findById(inventoryId)
            ?: throw NoSuchElementException(
                "Không tìm thấy thông tin tồn kho"
            )

        if (newReservedQuantity > inventory.stockQuantity) {
            throw IllegalArgumentException(
                "Số lượng giữ chỗ không được vượt quá tồn kho. " +
                        "Tối đa cho phép: ${inventory.stockQuantity}"
            )
        }

        val updated = inventoryRepository.updateReservedQuantity(
            inventoryId,
            newReservedQuantity
        )

        if (!updated) {
            throw IllegalStateException(
                "Cập nhật số lượng giữ chỗ thất bại"
            )
        }

        return ReservationResponse(
            inventoryId = inventoryId,
            stockQuantity = inventory.stockQuantity,
            reservedQuantity = newReservedQuantity,
            availableQuantity = inventory.stockQuantity - newReservedQuantity
        )
    }
}
