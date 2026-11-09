package com.sis.backend.service

import com.sis.backend.dto.InventorySearchFilter
import com.sis.backend.dto.PageResult
import com.sis.backend.entity.Inventory
import com.sis.backend.repository.InventoryRepository

/**
 * Service xử lý business logic cho tồn kho.
 *
 * @property inventoryRepository Repository tồn kho.
 */
class InventoryService(
    private val inventoryRepository: InventoryRepository
) {

    /**
     * Lấy danh sách tồn kho với filter và phân trang.
     *
     * @param filter Điều kiện lọc.
     * @param page Số trang.
     * @param size Kích thước trang.
     * @return PageResult chứa danh sách inventory.
     */
    fun getInventoryPage(
        filter: InventorySearchFilter,
        page: Int,
        size: Int
    ): PageResult<Inventory> {
        return inventoryRepository.findPage(filter, page, size)
    }

    /**
     * Lấy chi tiết tồn kho theo ID.
     *
     * @param inventoryId ID tồn kho.
     * @return Inventory nếu tìm thấy.
     * @throws IllegalArgumentException nếu không tìm thấy.
     */
    fun getInventoryById(inventoryId: Long): Inventory {
        return inventoryRepository.findById(inventoryId)
            ?: throw IllegalArgumentException("Không tìm thấy tồn kho với ID: $inventoryId")
    }
}
