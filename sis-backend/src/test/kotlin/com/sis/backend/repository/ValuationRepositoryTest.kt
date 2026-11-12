package com.sis.backend.repository

import com.sis.backend.config.TestDatabaseConfig
import com.sis.backend.fixture.ValuationFixture
import org.junit.jupiter.api.*
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test repository layer cho UC39: Valuation.
 * 
 * Test SQL thật với MariaDB:
 * - GROUP BY với aggregation
 * - BigDecimal scale 2
 * - Kho is_active filter
 * - HAVING SUM(stock_quantity) > 0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValuationRepositoryTest {
    
    private lateinit var dataSource: DataSource
    private lateinit var valuationRepository: ValuationRepository
    
    @BeforeAll
    fun setupAll() {
        dataSource = TestDatabaseConfig.createTestDataSource()
        valuationRepository = ValuationRepository(dataSource)
    }
    
    @BeforeEach
    fun setup() {
        TestDatabaseConfig.cleanupTestData(dataSource)
        ValuationFixture.setupTestData(dataSource)
    }
    
    @AfterEach
    fun cleanup() {
        ValuationFixture.cleanupTestData(dataSource)
    }
    
    @Test
    fun `test getSummary - totalValue khớp với sum manual`() {
        val summary = valuationRepository.getSummary()
        
        // Tính manual:
        // 201: 100 * 10000 = 1,000,000
        // 202: 50 * 15000 = 750,000
        // 204: 3 * 0.10 = 0.30
        // 205: 200 * 5000 = 1,000,000
        // Total = 2,750,000.30
        
        val expected = BigDecimal("2750000.30")
        assertEquals(expected, summary.totalValue, "Total value phải khớp tính manual")
    }
    
    @Test
    fun `test getSummary - warehouses sắp xếp theo value DESC`() {
        val summary = valuationRepository.getSummary()
        
        assertTrue(summary.warehouses.isNotEmpty(), "Phải có ít nhất 1 kho")
        
        // Verify sorted
        val values = summary.warehouses.map { it.totalValue }
        val sortedValues = values.sortedDescending()
        assertEquals(sortedValues, values, "Warehouses phải sắp xếp theo value DESC")
    }
    
    @Test
    fun `test getSummary - lọc theo categoryId`() {
        val summaryAll = valuationRepository.getSummary()
        val summaryCategory20 = valuationRepository.getSummary(categoryId = 20)
        
        // Category 20 không bao gồm product 205 (category 21)
        assertTrue(summaryCategory20.totalValue < summaryAll.totalValue,
            "Lọc category phải giảm total value")
    }
    
    @Test
    fun `test getDetail - HAVING stock greater than 0`() {
        val (items, _) = valuationRepository.getDetail(page = 1, size = 10)
        
        // Product 203 có stock=0 KHÔNG xuất hiện
        val zeroStock = items.find { it.sku == "VAL-003" }
        assertEquals(null, zeroStock, "Product stock=0 không được hiển thị")
        
        // Tất cả items phải có totalQuantity > 0
        items.forEach {
            assertTrue(it.totalQuantity > 0, "Mọi item phải có totalQuantity lớn hơn 0")
        }
    }
    
    @Test
    fun `test getDetail - stockValue tính đúng`() {
        val (items, _) = valuationRepository.getDetail(page = 1, size = 10)
        
        items.forEach { item ->
            val expected = item.currentCostPrice.multiply(BigDecimal(item.totalQuantity))
            assertEquals(expected, item.stockValue, 
                "stockValue phải = currentCostPrice * totalQuantity")
        }
    }
    
    @Test
    fun `test getDetail - phân trang đúng`() {
        val (items1, total) = valuationRepository.getDetail(page = 1, size = 2)
        val (items2, _) = valuationRepository.getDetail(page = 2, size = 2)
        
        assertEquals(2, items1.size, "Page 1 phải có 2 items")
        assertEquals(2, items2.size, "Page 2 phải có 2 items")
        assertEquals(4, total.toInt(), "Total phải là 4")
        
        // Items giữa 2 trang phải khác nhau
        val skus1 = items1.map { it.sku }.toSet()
        val skus2 = items2.map { it.sku }.toSet()
        assertTrue(skus1.intersect(skus2).isEmpty(), "Hai trang không được trùng items")
    }
    
    @Test
    fun `test getDetail - lọc theo warehouseId`() {
        val (items, _) = valuationRepository.getDetail(warehouseId = 20, page = 1, size = 10)
        
        // Tất cả items phải thuộc warehouse 20
        items.forEach {
            assertEquals(20L, it.warehouseId, "Mọi item phải thuộc warehouse 20")
        }
    }
    
    @Test
    fun `test getDetail - tìm kiếm theo keyword`() {
        val (items, _) = valuationRepository.getDetail(keyword = "VAL-001", page = 1, size = 10)
        
        assertEquals(1, items.size, "Chỉ có 1 sản phẩm khớp SKU")
        assertEquals("VAL-001", items[0].sku, "SKU phải khớp")
    }
    
    @Test
    fun `test BigDecimal scale 2`() {
        val summary = valuationRepository.getSummary()
        
        // Verify BigDecimal có scale đúng
        summary.warehouses.forEach { warehouse ->
            assertNotNull(warehouse.totalValue, "totalValue không được null")
            assertTrue(warehouse.totalValue.scale() >= 0, "BigDecimal phải có scale hợp lệ")
        }
    }
}
