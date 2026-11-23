package com.sis.backend.repository

import com.sis.backend.config.TestDatabaseConfig
import com.sis.backend.fixture.InventoryFixture
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.test.assertNotNull

/**
 * Test UC31: concurrent updates trên reserved_quantity.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservedQuantityTest {
    
    private lateinit var dataSource: DataSource
    
    @BeforeAll
    fun setupDatabase() {
        dataSource = TestDatabaseConfig.createTestDataSource()
    }
    
    @BeforeEach
    fun setupData() {
        TestDatabaseConfig.cleanupTestData(dataSource)
        InventoryFixture.setupTestData(dataSource)
    }
    
    @Test
    fun `concurrent updates should not cause data corruption`() {
        val repository1 = InventoryRepository(TestDatabaseConfig.createTestDataSource())
        val repository2 = InventoryRepository(TestDatabaseConfig.createTestDataSource())
        
        val inventoryId = 1L
        val latch = CountDownLatch(2)
        val successCount = AtomicInteger(0)
        
        val executor = Executors.newFixedThreadPool(2)
        
        // Thread 1: update to 50
        executor.submit {
            try {
                repository1.updateReserved(inventoryId, 50)
                successCount.incrementAndGet()
            } catch (e: Exception) {
                // Expected: một trong hai có thể fail do lock
            } finally {
                latch.countDown()
            }
        }
        
        // Thread 2: update to 60
        executor.submit {
            try {
                Thread.sleep(10) // Delay nhẹ
                repository2.updateReserved(inventoryId, 60)
                successCount.incrementAndGet()
            } catch (e: Exception) {
                // Expected
            } finally {
                latch.countDown()
            }
        }
        
        latch.await()
        executor.shutdown()
        
        // Verify: ít nhất 1 thành công, data không hỏng
        assertTrue(successCount.get() >= 1, "Ít nhất 1 update thành công")
        
        val finalState = InventoryRepository(dataSource).findById(inventoryId)
        assertNotNull(finalState, "Final state should exist")
        assertTrue(finalState!!.reservedQuantity in listOf(50, 60))
    }
    
    @Test
    fun `zero reserved quantity should be valid`() {
        val repository = InventoryRepository(dataSource)
        val result = repository.updateReserved(1L, 0)
        
        assertNotNull(result, "Should return updated inventory")
        assertEquals(0, result?.reservedQuantity)
    }
    
    @Test
    fun `reserved equal to stock should be valid`() {
        val repository = InventoryRepository(dataSource)
        
        // inventory_id=1 có stock_quantity=200
        val result = repository.updateReserved(1L, 200)
        
        assertNotNull(result, "Should return updated inventory")
        assertEquals(200, result?.reservedQuantity)
        assertEquals(0, result?.availableQuantity)
    }
}
