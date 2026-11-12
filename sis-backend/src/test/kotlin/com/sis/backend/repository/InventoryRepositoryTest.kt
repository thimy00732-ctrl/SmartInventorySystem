package com.sis.backend.repository

import com.sis.backend.config.TestDatabaseConfig
import com.sis.backend.dto.InventorySearchFilter
import com.sis.backend.dto.StockStatus
import com.sis.backend.fixture.InventoryFixture
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import javax.sql.DataSource
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Integration test InventoryRepository với MariaDB thật.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryRepositoryTest {
    
    private lateinit var dataSource: DataSource
    private lateinit var repository: InventoryRepository
    
    @BeforeAll
    fun setupDatabase() {
        assertTrue(TestDatabaseConfig.testConnection(), "Không kết nối được warehouse_test")
        dataSource = TestDatabaseConfig.createTestDataSource()
        repository = InventoryRepository(dataSource)
    }
    
    @BeforeEach
    fun setupData() {
        TestDatabaseConfig.cleanupTestData(dataSource)
        InventoryFixture.setupTestData(dataSource)
    }
    
    @Test
    fun `findPage should filter by stockStatus IN_STOCK`() {
        val filter = InventorySearchFilter(stockStatus = StockStatus.IN_STOCK)
        val result = repository.findPage(filter, 1, 10)
        
        assertTrue(result.items.all { it.stockQuantity > 0 })
    }
    
    @Test
    fun `findPage should filter by stockStatus OUT_OF_STOCK`() {
        val filter = InventorySearchFilter(stockStatus = StockStatus.OUT_OF_STOCK)
        val result = repository.findPage(filter, 1, 10)
        
        assertTrue(result.items.all { it.stockQuantity == 0 })
        assertTrue(result.totalItems >= 1)
    }
    
    @Test
    fun `findById should return null when not exists`() {
        val result = repository.findById(999L)
        assertNull(result, "Should return null for non-existent ID")
    }
    
    @Test
    fun `findByProductId should only return active warehouses`() {
        val result = repository.findByProductId(1L)
        
        assertTrue(result.all { it.warehouseId != 3L }, "Không được có kho inactive")
    }
    
    @Test
    fun `updateReserved should lock row with FOR UPDATE`() {
        // Test concurrency sẽ được test riêng trong ReservedQuantityTest
        val result = repository.updateReserved(1L, 20)
        
        assertNotNull(result, "Should return updated inventory")
        assertEquals(20, result?.reservedQuantity)
    }
    
    @Test
    fun `updateReserved with negative quantity should throw`() {
        assertThrows<IllegalArgumentException> {
            repository.updateReserved(1L, -5)
        }
    }
    
    @Test
    fun `updateReserved exceeding stock should throw`() {
        val exception = assertThrows<IllegalArgumentException> {
            repository.updateReserved(1L, 99999)
        }
        assertTrue(exception.message!!.contains("vượt quá tồn kho"))
    }
}
