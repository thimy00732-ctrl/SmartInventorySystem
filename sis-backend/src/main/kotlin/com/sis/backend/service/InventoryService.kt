package com.sis.backend.service

import com.sis.backend.dto.*
import com.sis.backend.exception.ResourceNotFoundException
import com.sis.backend.exception.InvalidReservedQuantityException
import com.sis.backend.mapper.toDetailResponse
import com.sis.backend.mapper.toItemResponse
import com.sis.backend.mapper.toUpdateReservedResponse
import com.sis.backend.repository.InventoryRepository
import com.sis.backend.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service xử lý nghiệp vụ tồn kho (UC29-32).
 * 
 * Chịu trách nhiệm validation, tính toán tổng, gọi repository.
 */
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository
) {
    // Ngưỡng cận hạn (từ config, hardcode tạm)
    // TODO: Đọc từ application.conf bằng environment.config
    private val nearExpiryDays = 30
    
    /**
     * UC29: Lấy danh sách tồn kho có lọc và phân trang.
     * 
     * @param filter Điều kiện lọc
     * @param page Trang hiện tại (bắt đầu từ 1)
     * @param size Số item tối đa/trang
     * @return PageResult<InventoryItemResponse>
     */
    suspend fun getInventoryPage(
        filter: InventorySearchFilter = InventorySearchFilter(),
        page: Int = 1,
        size: Int = 10
    ): PageResult<InventoryItemResponse> = withContext(Dispatchers.IO) {
        val result = inventoryRepository.findPage(filter, page, size)
        
        PageResult(
            items = result.items.map { it.toItemResponse(nearExpiryDays) },
            page = result.page,
            size = result.size,
            totalItems = result.totalItems,
            totalPages = result.totalPages
        )
    }
    
    /**
     * UC30: Lấy chi tiết tồn kho theo ID.
     * 
     * @param inventoryId ID dòng tồn kho
     * @return InventoryDetailResponse
     * @throws ResourceNotFoundException nếu không tồn tại
     */
    suspend fun getInventoryDetail(inventoryId: Long): InventoryDetailResponse = withContext(Dispatchers.IO) {
        val inventory = inventoryRepository.findById(inventoryId)
            ?: throw ResourceNotFoundException("Dòng tồn kho", inventoryId)
        
        // Lấy thông tin sản phẩm (min_stock, max_stock, is_active)
        val product = productRepository.findById(inventory.productId)
            ?: throw ResourceNotFoundException("Sản phẩm", inventory.productId)
        
        inventory.toDetailResponse(
            nearExpiryDays = nearExpiryDays,
            minStock = product.minStock,
            maxStock = product.maxStock,
            isActive = product.isActive
        )
    }
    
    /**
     * UC30: Lấy danh sách tồn kho theo sản phẩm.
     * 
     * Chỉ lấy kho đang hoạt động, tính tổng ở service.
     * 
     * @param productId ID sản phẩm
     * @return InventoryByProductResponse với items và dòng tổng
     * @throws ResourceNotFoundException nếu sản phẩm không tồn tại
     */
    suspend fun getInventoryByProduct(productId: Long): InventoryByProductResponse = withContext(Dispatchers.IO) {
        // Kiểm tra sản phẩm có tồn tại không
        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Sản phẩm", productId)
        
        // Lấy các dòng inventory
        val inventoryList = inventoryRepository.findByProductId(productId)
        
        // Tính tổng (không dùng sum trên trang, tính trên toàn bộ kết quả)
        val totalStock = inventoryList.sumOf { it.stockQuantity }
        val totalReserved = inventoryList.sumOf { it.reservedQuantity }
        val totalAvailable = inventoryList.sumOf { it.availableQuantity }
        
        InventoryByProductResponse(
            items = inventoryList.map { it.toItemResponse(nearExpiryDays) },
            totalStock = totalStock,
            totalReserved = totalReserved,
            totalAvailable = totalAvailable
        )
    }
    
    /**
     * UC31: Cập nhật reserved_quantity.
     * 
     * Validation:
     * - 0 <= n <= stock_quantity
     * - Trong transaction với FOR UPDATE lock
     * 
     * @param inventoryId ID dòng tồn kho
     * @param newReservedQuantity Số lượng giữ chỗ mới
     * @return UpdateReservedResponse
     * @throws ResourceNotFoundException nếu không tồn tại
     * @throws InvalidReservedQuantityException nếu vi phạm ràng buộc
     */
    suspend fun updateReservedQuantity(
        inventoryId: Long,
        newReservedQuantity: Int
    ): UpdateReservedResponse = withContext(Dispatchers.IO) {
        try {
            // Repository đã xử lý transaction và validation
            val updatedInventory = inventoryRepository.updateReserved(inventoryId, newReservedQuantity)
                ?: throw ResourceNotFoundException("Dòng tồn kho", inventoryId)
            
            updatedInventory.toUpdateReservedResponse()
            
        } catch (e: IllegalArgumentException) {
            // Ném lại với InvalidReservedQuantityException
            throw InvalidReservedQuantityException(e.message ?: "Số lượng giữ chỗ không hợp lệ.")
        }
    }
}
