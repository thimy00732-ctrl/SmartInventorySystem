package com.sis.backend.repository

import com.sis.backend.config.TestDatabaseConfig
import com.sis.backend.fixture.ThresholdFixture
import org.junit.jupiter.api.*
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test repository layer cho UC34-35: Cảnh báo tồn kho.
 * 
 * Test SQL thật với MariaDB:
 * - GROUP BY, HAVING
 * - Lọc kho hoạt động (w.is_active=TRUE)
 * - Sắp xếp
 * - CTE trong alert summary
 * - max_stock=0 không lỗi chia 0
 * - Sản phẩm bằng ngưỡng không xuất hiện
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlertRepositoryTest {
    
    private lateinit var dataSource: DataSource
    private lateinit var alertRepository: AlertRepository
    
    @BeforeAll
    fun setupAll() {
        dataSource = TestDatabaseConfig.createTestDataSource()
        alertRepository = AlertRepository(dataSource)
    }
    
    @BeforeEach
    fun setup() {
        TestDatabaseConfig.cleanupTestData(dataSource)
        ThresholdFixture.setupTestData(dataSource)
    }
    
    @AfterEach
    fun cleanup() {
        ThresholdFixture.cleanupTestData(dataSource)
    }
    
    @Test
    fun `test findLowStockAlerts - chỉ sản phẩm tồn thấp`() {
        val result = alertRepository.findLowStockAlerts()
        
        // Chỉ có sản phẩm 101 (total=30 < min=50)
        val lowStockProduct = result.find { it.sku == "LOW-STOCK-01" }
        assertNotNull(lowStockProduct, "Phải có sản phẩm LOW-STOCK-01")
        assertEquals(30, lowStockProduct.totalStock, "Total stock phải là 30")
        assertEquals(50, lowStockProduct.minStock, "Min stock phải là 50")
        assertEquals(20, lowStockProduct.missingQuantity, "Missing quantity phải là 20")
        assertEquals("LOW", lowStockProduct.level, "Level phải là LOW")
        // Supplier có thể null hoặc có giá trị
        
        // Sản phẩm 102 (OK) không được xuất hiện
        val okProduct = result.find { it.sku == "OK-STOCK-02" }
        assertNull(okProduct, "Sản phẩm trong ngưỡng không được xuất hiện trong low stock")
        
        // Sản phẩm 103 (OVER) không được xuất hiện
        val overProduct = result.find { it.sku == "OVER-STOCK-03" }
        assertNull(overProduct, "Sản phẩm vượt mức không được xuất hiện trong low stock")
        
        // Sản phẩm 105 (không có inventory, total=0 < min=10) phải xuất hiện
        val noInvProduct = result.find { it.sku == "NO-INV-05" }
        assertNotNull(noInvProduct, "Sản phẩm không có inventory phải xuất hiện")
        assertEquals(0, noInvProduct.totalStock, "Total stock phải là 0")
        assertEquals(10, noInvProduct.minStock, "Min stock phải là 10")
        assertEquals("OUT_OF_STOCK", noInvProduct.level, "Level phải là OUT_OF_STOCK khi total=0")
    }
    
    @Test
    fun `test findLowStockAlerts - sản phẩm bằng ngưỡng không xuất hiện`() {
        val result = alertRepository.findLowStockAlerts()
        
        // Sản phẩm 106: total=100 = min_stock=100 → KHÔNG xuất hiện
        val equalMinProduct = result.find { it.sku == "EQUAL-MIN-06" }
        assertNull(equalMinProduct, "Sản phẩm total=min không được xuất hiện trong low stock")
    }
    
    @Test
    fun `test findLowStockAlerts - tồn ở kho không hoạt động không tính`() {
        val result = alertRepository.findLowStockAlerts()
        
        // Sản phẩm 103 có 1500 ở kho hoạt động + 5000 ở kho không hoạt động
        // Chỉ tính 1500 → vượt mức → không xuất hiện trong low stock
        val product = result.find { it.sku == "OVER-STOCK-03" }
        assertNull(product, "Tồn ở kho không hoạt động không được tính")
    }
    
    @Test
    fun `test findLowStockAlerts - sắp xếp theo missingQuantity giảm dần`() {
        val result = alertRepository.findLowStockAlerts()
        
        // Kiểm tra thứ tự
        assertTrue(result.isNotEmpty(), "Phải có ít nhất 1 sản phẩm tồn thấp")
        
        // Verify sorted
        val missings = result.map { it.missingQuantity }
        val sortedMissings = missings.sortedDescending()
        assertEquals(sortedMissings, missings, "Phải sắp xếp theo missingQuantity giảm dần")
    }
    
    @Test
    fun `test findOverStockAlerts - chỉ sản phẩm vượt mức`() {
        val result = alertRepository.findOverStockAlerts()
        
        // Chỉ có sản phẩm 103 (total=1500 > max=1000)
        val overStockProduct = result.find { it.sku == "OVER-STOCK-03" }
        assertNotNull(overStockProduct, "Phải có sản phẩm OVER-STOCK-03")
        assertEquals(1500, overStockProduct.totalStock, "Total stock phải là 1500")
        assertEquals(1000, overStockProduct.maxStock, "Max stock phải là 1000")
        assertEquals(500, overStockProduct.excessQuantity, "Excess quantity phải là 500")
        assertNotNull(overStockProduct.excessPercent, "ExcessPercent không được null")
        
        // Sản phẩm 102 (OK) không xuất hiện
        val okProduct = result.find { it.sku == "OK-STOCK-02" }
        assertNull(okProduct, "Sản phẩm trong ngưỡng không được xuất hiện")
        
        // Sản phẩm 101 (LOW) không xuất hiện
        val lowProduct = result.find { it.sku == "LOW-STOCK-01" }
        assertNull(lowProduct, "Sản phẩm tồn thấp không được xuất hiện trong over stock")
    }
    
    @Test
    fun `test findOverStockAlerts - max_stock bằng 0 không lỗi chia 0`() {
        val result = alertRepository.findOverStockAlerts()
        
        // Sản phẩm 104: max=0, total=10 > 0 → phải xuất hiện
        val maxZeroProduct = result.find { it.sku == "MAX-ZERO-04" }
        assertNotNull(maxZeroProduct, "Sản phẩm max=0 phải xuất hiện nếu total>0")
        assertEquals(0, maxZeroProduct.maxStock, "Max stock phải là 0")
        assertEquals(10, maxZeroProduct.totalStock, "Total stock phải là 10")
        assertNull(maxZeroProduct.excessPercent, "ExcessPercent phải null khi max=0")
    }
    
    @Test
    fun `test findOverStockAlerts - sản phẩm bằng ngưỡng không xuất hiện`() {
        val result = alertRepository.findOverStockAlerts()
        
        // Sản phẩm 107: total=100 = max_stock=100 → KHÔNG xuất hiện
        val equalMaxProduct = result.find { it.sku == "EQUAL-MAX-07" }
        assertNull(equalMaxProduct, "Sản phẩm total=max không được xuất hiện trong over stock")
    }
    
    @Test
    fun `test getAlertSummary - số liệu khớp với danh sách`() {
        val summary = alertRepository.getAlertSummary()
        val lowStockList = alertRepository.findLowStockAlerts()
        val overStockList = alertRepository.findOverStockAlerts()
        
        assertEquals(lowStockList.size, summary.lowStockCount, 
            "lowStockCount phải khớp với số dòng findLowStockAlerts")
        assertEquals(overStockList.size, summary.overStockCount,
            "overStockCount phải khớp với số dòng findOverStockAlerts")
        
        // outOfStockCount phải <= lowStockCount
        assertTrue(summary.outOfStockCount <= summary.lowStockCount,
            "outOfStockCount phải <= lowStockCount")
        
        // Đếm số sản phẩm OUT_OF_STOCK từ lowStockList
        val outOfStockFromList = lowStockList.count { it.level == "OUT_OF_STOCK" }
        assertEquals(outOfStockFromList, summary.outOfStockCount,
            "outOfStockCount phải khớp với số sản phẩm level=OUT_OF_STOCK")
    }
    
    @Test
    fun `test getAlertSummary - CTE query không lỗi`() {
        // Test CTE syntax với MariaDB
        val summary = alertRepository.getAlertSummary()
        
        assertNotNull(summary, "Alert summary không được null")
        assertTrue(summary.lowStockCount >= 0, "lowStockCount phải >= 0")
        assertTrue(summary.outOfStockCount >= 0, "outOfStockCount phải >= 0")
        assertTrue(summary.overStockCount >= 0, "overStockCount phải >= 0")
    }
}
