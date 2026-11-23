package com.sis.backend.service

import com.sis.backend.config.TestDatabaseConfig
import com.sis.backend.dto.UpdateThresholdRequest
import com.sis.backend.exception.InvalidThresholdException
import com.sis.backend.exception.ResourceNotFoundException
import com.sis.backend.fixture.ThresholdFixture
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.ThresholdRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Test service layer cho UC33: Ngưỡng tồn kho.
 * 
 * Kiểm tra:
 * - Validation minStock < 0
 * - Validation maxStock < minStock
 * - Validation minStock = maxStock (hợp lệ)
 * - Cập nhật thành công
 * - Sản phẩm không tồn tại
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThresholdServiceTest {
    
    private lateinit var dataSource: DataSource
    private lateinit var thresholdService: ThresholdService
    private lateinit var productRepository: ProductRepository
    private lateinit var thresholdRepository: ThresholdRepository
    
    @BeforeAll
    fun setupAll() {
        dataSource = TestDatabaseConfig.createTestDataSource()
        productRepository = ProductRepository(dataSource)
        thresholdRepository = ThresholdRepository(dataSource)
        thresholdService = ThresholdService(productRepository, thresholdRepository)
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
    fun `test get products with thresholds - tất cả sản phẩm hoạt động`() = runBlocking {
        val result = thresholdService.getProductsWithThresholds()
        
        // Phải có ít nhất các sản phẩm test (101-107, trừ 108 vì inactive)
        assertTrue(result.size >= 7, "Phải có ít nhất 7 sản phẩm hoạt động")
        
        // Kiểm tra alertLevel được tính đúng
        val lowStock = result.find { it.sku == "LOW-STOCK-01" }
        assertEquals("LOW_STOCK", lowStock?.alertLevel, "Sản phẩm tồn thấp phải có alertLevel=LOW_STOCK")
        
        val okStock = result.find { it.sku == "OK-STOCK-02" }
        assertEquals("OK", okStock?.alertLevel, "Sản phẩm trong ngưỡng phải có alertLevel=OK")
        
        val overStock = result.find { it.sku == "OVER-STOCK-03" }
        assertEquals("OVER_STOCK", overStock?.alertLevel, "Sản phẩm vượt mức phải có alertLevel=OVER_STOCK")
    }
    
    @Test
    fun `test update thresholds - thành công`() = runBlocking {
        val request = UpdateThresholdRequest(
            minStock = 20,
            maxStock = 300
        )
        
        val result = thresholdService.updateThresholds(101, request)
        
        assertEquals(101, result.productId, "Product ID phải khớp")
        assertEquals("LOW-STOCK-01", result.sku, "SKU phải khớp")
        assertEquals(20, result.minStock, "minStock phải được cập nhật")
        assertEquals(300, result.maxStock, "maxStock phải được cập nhật")
        
        // Verify trong DB
        val product = productRepository.findById(101)
        assertEquals(20, product?.minStock, "minStock trong DB phải được cập nhật")
        assertEquals(300, product?.maxStock, "maxStock trong DB phải được cập nhật")
    }
    
    @Test
    fun `test update thresholds - minStock âm phải lỗi`() {
        val request = UpdateThresholdRequest(
            minStock = -1,
            maxStock = 100
        )
        
        val exception = assertFailsWith<InvalidThresholdException> {
            runBlocking {
                thresholdService.updateThresholds(101, request)
            }
        }
        
        assertTrue(exception.message?.contains("không được âm") == true, 
            "Thông báo lỗi phải chứa 'không được âm'")
    }
    
    @Test
    fun `test update thresholds - maxStock nhỏ hơn minStock phải lỗi`() {
        val request = UpdateThresholdRequest(
            minStock = 100,
            maxStock = 50
        )
        
        val exception = assertFailsWith<InvalidThresholdException> {
            runBlocking {
                thresholdService.updateThresholds(101, request)
            }
        }
        
        assertTrue(exception.message?.contains("lớn hơn hoặc bằng") == true,
            "Thông báo lỗi phải chứa 'lớn hơn hoặc bằng'")
    }
    
    @Test
    fun `test update thresholds - minStock bằng maxStock hợp lệ`() = runBlocking {
        val request = UpdateThresholdRequest(
            minStock = 100,
            maxStock = 100
        )
        
        val result = thresholdService.updateThresholds(101, request)
        
        assertEquals(100, result.minStock, "minStock phải là 100")
        assertEquals(100, result.maxStock, "maxStock phải là 100")
    }
    
    @Test
    fun `test update thresholds - sản phẩm không tồn tại`() {
        val request = UpdateThresholdRequest(
            minStock = 10,
            maxStock = 100
        )
        
        val exception = assertFailsWith<ResourceNotFoundException> {
            runBlocking {
                thresholdService.updateThresholds(999999, request)
            }
        }
        
        assertTrue(exception.message?.contains("999999") == true,
            "Thông báo lỗi phải chứa ID sản phẩm")
    }
}
