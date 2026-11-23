package com.sis.backend.service

import com.sis.backend.config.TestDatabaseConfig
import com.sis.backend.fixture.ValuationFixture
import com.sis.backend.repository.ValuationRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test service layer cho UC39: Giá trị tồn kho.
 * 
 * Kiểm tra:
 * - BigDecimal không có sai số (0.1 + 0.2 = 0.30)
 * - Dòng tổng không đổi khi chuyển trang
 * - Kho không hoạt động không tính
 * - Sản phẩm stock=0 không xuất hiện
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValuationServiceTest {
    
    private lateinit var dataSource: DataSource
    private lateinit var valuationService: ValuationService
    private lateinit var valuationRepository: ValuationRepository
    
    @BeforeAll
    fun setupAll() {
        dataSource = TestDatabaseConfig.createTestDataSource()
        valuationRepository = ValuationRepository(dataSource)
        valuationService = ValuationService(valuationRepository)
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
    fun `test getSummary - tổng giá trị đúng`() = runBlocking {
        val summary = valuationService.getSummary()
        
        // Product 201: 100 * 10000 = 1,000,000
        // Product 202: 50 * 15000 = 750,000
        // Product 203: 0 * 20000 = 0 (không tính)
        // Product 204: 3 * 0.10 = 0.30
        // Product 205: 200 * 5000 = 1,000,000
        // Total = 2,750,000.30
        
        val expected = BigDecimal("2750000.30")
        assertEquals(expected, summary.totalValue, "Total value phải chính xác")
        
        assertEquals(353, summary.totalQuantity, "Total quantity phải là 353")
        
        // Product có stock > 0: 201, 202, 204, 205 = 4 sản phẩm
        assertEquals(4, summary.productCount, "Product count phải là 4")
    }
    
    @Test
    fun `test getSummary - kho không hoạt động không tính`() = runBlocking {
        val summary = valuationService.getSummary()
        
        // Kho 21 (không hoạt động) có 1000 * 10000 = 10,000,000 KHÔNG được tính
        assertTrue(summary.totalValue < BigDecimal("10000000"), 
            "Kho không hoạt động không được tính vào tổng")
    }
    
    @Test
    fun `test getSummary - BigDecimal không có sai số`() = runBlocking {
        val summary = valuationService.getSummary()
        
        // Product 204: 3 * 0.10 = 0.30
        // Nếu dùng double: 0.1 + 0.2 = 0.30000000000000004
        // BigDecimal phải ra đúng 0.30
        
        val totalStr = summary.totalValue.toString()
        assertTrue(totalStr.endsWith(".30") || totalStr.endsWith(".3"),
            "BigDecimal phải không có sai số floating point")
    }
    
    @Test
    fun `test getDetail - phân trang đúng`() = runBlocking {
        val page1 = valuationService.getDetail(page = 1, size = 2)
        
        assertEquals(2, page1.items.size, "Page 1 phải có 2 items")
        assertEquals(4, page1.totalItems, "Total items phải là 4 (exclude stock=0)")
        assertEquals(2, page1.totalPages, "Total pages phải là 2")
    }
    
    @Test
    fun `test getDetail - dòng tổng không đổi khi chuyển trang`() = runBlocking {
        val page1 = valuationService.getDetail(page = 1, size = 2)
        val page2 = valuationService.getDetail(page = 2, size = 2)
        
        assertEquals(page1.total, page2.total, 
            "Dòng tổng phải giống nhau ở mọi trang")
        
        val expected = BigDecimal("2750000.30")
        assertEquals(expected, page1.total, "Total phải chính xác")
    }
    
    @Test
    fun `test getDetail - sắp xếp theo stockValue DESC`() = runBlocking {
        val result = valuationService.getDetail(page = 1, size = 10)
        
        assertTrue(result.items.isNotEmpty(), "Phải có items")
        
        // Verify sorted by stockValue DESC
        val values = result.items.map { it.stockValue }
        val sortedValues = values.sortedDescending()
        assertEquals(sortedValues, values, "Phải sắp xếp theo stockValue giảm dần")
    }
    
    @Test
    fun `test getDetail - stock=0 không xuất hiện`() = runBlocking {
        val result = valuationService.getDetail(page = 1, size = 10)
        
        // Product 203 có stock=0 KHÔNG được xuất hiện
        val product203 = result.items.find { it.sku == "VAL-003" }
        assertEquals(null, product203, "Sản phẩm stock=0 không được hiển thị")
    }
    
    @Test
    fun `test getDetail - lọc theo categoryId`() = runBlocking {
        val result = valuationService.getDetail(categoryId = 20, page = 1, size = 10)
        
        // Chỉ có sản phẩm trong category 20 (exclude 205)
        val product205 = result.items.find { it.sku == "VAL-005" }
        assertEquals(null, product205, "Sản phẩm danh mục khác không hiển thị")
    }
    
    @Test
    fun `test getDetail - note hiển thị đúng`() = runBlocking {
        val result = valuationService.getDetail()
        
        assertTrue(result.note.contains("giá vốn hiện tại"),
            "Note phải có cảnh báo về giá vốn")
    }
}
