package com.sis.backend.service

import com.sis.backend.dto.*
import com.sis.backend.entity.Inventory
import com.sis.backend.entity.Product
import com.sis.backend.exception.ResourceNotFoundException
import com.sis.backend.repository.InventoryRepository
import com.sis.backend.repository.ProductRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Test InventoryService (Unit test với MockK).
 */
class InventoryServiceTest {
    
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var inventoryService: InventoryService
    
    @BeforeEach
    fun setup() {
        inventoryRepository = mockk()
        productRepository = mockk()
        inventoryService = InventoryService(inventoryRepository, productRepository)
    }
    
    @Test
    fun `getInventoryPage should return mapped items`() = runBlocking {
        val mockInventory = Inventory(
            inventoryId = 1L,
            warehouseId = 1L,
            warehouseName = "Kho A",
            productId = 1L,
            productName = "Sản phẩm A",
            sku = "SKU-001",
            barcode = "123456",
            categoryId = 1L,
            categoryName = "Danh mục A",
            supplierId = 1L,
            supplierName = "NCC A",
            unit = "Cái",
            batchCode = "LOT-001",
            manufactureDate = LocalDate.now().minusMonths(6),
            expiryDate = LocalDate.now().plusMonths(6),
            stockQuantity = 100,
            reservedQuantity = 10,
            availableQuantity = 90,
            updatedAt = LocalDateTime.now()
        )
        
        val pageResult = PageResult(
            items = listOf(mockInventory),
            page = 1,
            size = 10,
            totalItems = 1L,
            totalPages = 1
        )
        
        every { inventoryRepository.findPage(any(), any(), any()) } returns pageResult
        
        val result = inventoryService.getInventoryPage()
        
        assertEquals(1, result.items.size)
        assertEquals("SKU-001", result.items[0].sku)
        assertFalse(result.items[0].noBatch)
    }
    
    @Test
    fun `getInventoryDetail should throw when not found`() {
        every { inventoryRepository.findById(999L) } returns null
        
        assertThrows(ResourceNotFoundException::class.java) {
            runBlocking {
                inventoryService.getInventoryDetail(999L)
            }
        }
    }
    
    @Test
    fun `calculateExpiryStatus for expired product`() {
        val expiredDate = LocalDate.now().minusDays(1)
        val mockInventory = Inventory(
            inventoryId = 1L, warehouseId = 1L, warehouseName = "Kho",
            productId = 1L, productName = "SP", sku = "SKU", barcode = null,
            categoryId = null, categoryName = null, supplierId = null, supplierName = null,
            unit = "Cái", batchCode = "LOT", manufactureDate = null,
            expiryDate = expiredDate,
            stockQuantity = 10, reservedQuantity = 0, availableQuantity = 10,
            updatedAt = LocalDateTime.now()
        )
        
        val product = Product(
            productId = 1L, sku = "SKU", barcode = null, productName = "SP",
            imageUrl = null, categoryId = null, categoryName = null,
            supplierId = null, supplierName = null, unit = "Cái",
            costPrice = BigDecimal.ZERO, salePrice = BigDecimal.ZERO,
            minStock = 10, maxStock = 100, isActive = true,
            createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now(),
            stockQuantity = 10, reservedQuantity = 0, availableQuantity = 10
        )
        
        every { inventoryRepository.findById(1L) } returns mockInventory
        every { productRepository.findById(1L) } returns product
        
        val result = runBlocking { inventoryService.getInventoryDetail(1L) }
        
        assertEquals("EXPIRED", result.expiryStatus)
    }
}
