
package com.sis.backend.service

import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.product.UpdateProductRequest
import com.sis.backend.entity.Product
import com.sis.backend.exception.DuplicateBarcodeException
import com.sis.backend.exception.DuplicateSkuException
import com.sis.backend.exception.InvalidProductCategoryException
import com.sis.backend.exception.InvalidProductException
import com.sis.backend.exception.InvalidProductSupplierException
import com.sis.backend.exception.ProductNotFoundException
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.ProductPageResult
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.ProductSearchFilter
import com.sis.backend.repository.SupplierRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Kiểm thử các nghiệp vụ quản lý sản phẩm.
 *
 * Sử dụng MockK để giả lập các Repository,
 * không cần kết nối trực tiếp đến cơ sở dữ liệu.
 */
class ProductServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var supplierRepository: SupplierRepository
    private lateinit var productService: ProductService

    private val productId = 1L

    /**
     * Khởi tạo các Repository giả lập trước mỗi test.
     */
    @BeforeEach
    fun setUp() {
        productRepository = mockk()
        categoryRepository = mockk()
        supplierRepository = mockk()

        productService = ProductService(
            productRepository = productRepository,
            categoryRepository = categoryRepository,
            supplierRepository = supplierRepository
        )
    }

    /**
     * Tạo dữ liệu sản phẩm mẫu phục vụ kiểm thử.
     */
    private fun createSampleProduct(
        id: Long = productId,
        sku: String = "SP000001",
        barcode: String? = "2001234567893",
        name: String = "Bàn phím cơ",
        isActive: Boolean = true
    ): Product {
        val now = LocalDateTime.now()

        return Product(
            productId = id,
            sku = sku,
            barcode = barcode,
            productName = name,
            imageUrl = null,
            categoryId = null,
            supplierId = null,
            unit = "Cái",
            costPrice = BigDecimal("250000.00"),
            salePrice = BigDecimal("350000.00"),
            minStock = 5,
            maxStock = 100,
            isActive = isActive,
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Tạo request thêm sản phẩm mẫu.
     */
    private fun createRequest(
        sku: String? = "SPTEST0001",
        barcode: String? = "2001234567893",
        productName: String = "Bàn phím cơ Test",
        costPrice: BigDecimal = BigDecimal("250000.00"),
        salePrice: BigDecimal = BigDecimal("350000.00"),
        minStock: Int = 5,
        maxStock: Int = 100,
        categoryId: Long? = null,
        supplierId: Long? = null
    ) = CreateProductRequest(
        sku = sku,
        barcode = barcode,
        productName = productName,
        imageUrl = null,
        categoryId = categoryId,
        supplierId = supplierId,
        unit = "Cái",
        costPrice = costPrice,
        salePrice = salePrice,
        minStock = minStock,
        maxStock = maxStock
    )

    /**
     * Tạo request cập nhật sản phẩm mẫu.
     */
    private fun updateRequest(
        sku: String = "SP000001",
        barcode: String? = "2001234567893",
        productName: String = "Bàn phím cơ Updated"
    ) = UpdateProductRequest(
        sku = sku,
        barcode = barcode,
        productName = productName,
        imageUrl = null,
        categoryId = null,
        supplierId = null,
        unit = "Cái",
        costPrice = BigDecimal("270000.00"),
        salePrice = BigDecimal("380000.00"),
        minStock = 5,
        maxStock = 100
    )

    // =====================================================
    // 1. KIỂM THỬ LẤY DANH SÁCH SẢN PHẨM
    // =====================================================

    @Test
    fun `getAllProducts should return product list`() {
        val products = listOf(createSampleProduct())

        every {
            productRepository.findAll(null, false)
        } returns products

        val result = productService.getAllProducts(
            keyword = null,
            includeInactive = false
        )

        assertEquals(1, result.size)
        assertEquals("SP000001", result[0].sku)
        assertEquals("Bàn phím cơ", result[0].productName)
    }

    // =====================================================
    // 2. KIỂM THỬ PHÂN TRANG
    // =====================================================

    @Test
    fun `getProductsPage should return paginated products`() {
        val products = listOf(createSampleProduct())

        val pageResult = ProductPageResult(
            items = products,
            page = 1,
            size = 10,
            totalItems = 1,
            totalPages = 1
        )

        every {
            productRepository.findPage(
                filter = any(),
                page = 1,
                size = 10
            )
        } returns pageResult

        val result = productService.getProductsPage(
            filter = ProductSearchFilter(),
            page = 1,
            size = 10
        )

        assertEquals(1, result.items.size)
        assertEquals(1, result.page)
        assertEquals(10, result.size)
        assertEquals(1L, result.totalItems)
        assertEquals(1, result.totalPages)
    }

    // =====================================================
    // 3. KIỂM THỬ TẠO SẢN PHẨM
    // =====================================================

    @Test
    fun `createProduct should create product successfully`() {
        val request = createRequest()
        val savedProduct = slot<Product>()
        val createdProduct = createSampleProduct(
            sku = "SPTEST0001",
            barcode = "2001234567893"
        )

        every {
            productRepository.existsSku("SPTEST0001")
        } returns false

        every {
            productRepository.existsBarcode("2001234567893")
        } returns false

        every {
            productRepository.create(capture(savedProduct))
        } returns productId

        every {
            productRepository.findById(productId)
        } returns createdProduct

        val result = productService.createProduct(request)

        assertEquals("SPTEST0001", result.sku)
        assertEquals("2001234567893", result.barcode)
        assertEquals("Bàn phím cơ Test", savedProduct.captured.productName)
        assertTrue(savedProduct.captured.isActive)

        verify(exactly = 1) {
            productRepository.create(any())
        }
    }

    /**
     * Kiểm tra SKU được tự động sinh khi không cung cấp.
     */
    @Test
    fun `createProduct should generate SKU when SKU is missing`() {
        val request = createRequest(sku = null)
        val savedProduct = slot<Product>()

        every {
            productRepository.existsSku(any())
        } returns false

        every {
            productRepository.existsBarcode("2001234567893")
        } returns false

        every {
            productRepository.create(capture(savedProduct))
        } returns productId

        every {
            productRepository.findById(productId)
        } answers {
            savedProduct.captured.copy(productId = productId)
        }

        val result = productService.createProduct(request)

        assertTrue(result.sku.matches(Regex("SP\\d{6}")))
    }

    /**
     * Kiểm tra từ chối tạo sản phẩm khi SKU bị trùng.
     */
    @Test
    fun `createProduct should throw when SKU already exists`() {
        val request = createRequest()

        every {
            productRepository.existsSku("SPTEST0001")
        } returns true

        assertThrows(DuplicateSkuException::class.java) {
            productService.createProduct(request)
        }

        verify(exactly = 0) {
            productRepository.create(any())
        }
    }

    /**
     * Kiểm tra từ chối tạo sản phẩm khi barcode bị trùng.
     */
    @Test
    fun `createProduct should throw when barcode already exists`() {
        val request = createRequest()

        every {
            productRepository.existsSku("SPTEST0001")
        } returns false

        every {
            productRepository.existsBarcode("2001234567893")
        } returns true

        assertThrows(DuplicateBarcodeException::class.java) {
            productService.createProduct(request)
        }
    }

    // =====================================================
    // 4. KIỂM THỬ DỮ LIỆU KHÔNG HỢP LỆ
    // =====================================================

    /**
     * Không cho phép tạo sản phẩm có tên rỗng.
     */
    @Test
    fun `createProduct should reject empty product name`() {
        val request = createRequest(productName = "")

        assertThrows(InvalidProductException::class.java) {
            productService.createProduct(request)
        }
    }

    /**
     * Không cho phép giá vốn âm.
     */
    @Test
    fun `createProduct should reject negative cost price`() {
        val request = createRequest(
            costPrice = BigDecimal("-1000")
        )

        assertThrows(InvalidProductException::class.java) {
            productService.createProduct(request)
        }
    }

    /**
     * Không cho phép tồn kho tối đa nhỏ hơn tồn kho tối thiểu.
     */
    @Test
    fun `createProduct should reject invalid stock range`() {
        val request = createRequest(
            minStock = 100,
            maxStock = 10
        )

        assertThrows(InvalidProductException::class.java) {
            productService.createProduct(request)
        }
    }

    // =====================================================
    // 5. KIỂM THỬ DANH MỤC VÀ NHÀ CUNG CẤP
    // =====================================================

    /**
     * Không cho phép sử dụng danh mục không hoạt động hoặc không tồn tại.
     */
    @Test
    fun `createProduct should reject invalid category`() {
        val request = createRequest(categoryId = 999L)

        every {
            categoryRepository.existsActiveById(999L)
        } returns false

        assertThrows(InvalidProductCategoryException::class.java) {
            productService.createProduct(request)
        }
    }

    /**
     * Không cho phép sử dụng nhà cung cấp không hoạt động hoặc không tồn tại.
     */
    @Test
    fun `createProduct should reject invalid supplier`() {
        val request = createRequest(supplierId = 999L)

        every {
            supplierRepository.existsActiveById(999L)
        } returns false

        assertThrows(InvalidProductSupplierException::class.java) {
            productService.createProduct(request)
        }
    }

    // =====================================================
    // 6. KIỂM THỬ XEM CHI TIẾT SẢN PHẨM
    // =====================================================

    @Test
    fun `getProductDetail should return product details`() {
        val product = createSampleProduct()

        every {
            productRepository.findById(productId)
        } returns product

        every {
            productRepository.findInventories(productId)
        } returns emptyList()

        val result = productService.getProductDetail(productId)

        assertEquals(productId, result.product.productId)
        assertEquals("Bàn phím cơ", result.product.productName)
        assertTrue(result.inventories.isEmpty())
    }

    @Test
    fun `getProductDetail should throw when product does not exist`() {
        every {
            productRepository.findById(999L)
        } returns null

        assertThrows(ProductNotFoundException::class.java) {
            productService.getProductDetail(999L)
        }
    }

    // =====================================================
    // 7. KIỂM THỬ CẬP NHẬT SẢN PHẨM
    // =====================================================

    @Test
    fun `updateProduct should update product successfully`() {
        val existingProduct = createSampleProduct()
        val updatedProduct = existingProduct.copy(
            productName = "Bàn phím cơ Updated",
            costPrice = BigDecimal("270000.00"),
            salePrice = BigDecimal("380000.00")
        )

        every {
            productRepository.findById(productId)
        } returnsMany listOf(existingProduct, updatedProduct)

        every {
            productRepository.existsSku("SP000001", productId)
        } returns false

        every {
            productRepository.existsBarcode(
                "2001234567893",
                productId
            )
        } returns false

        every {
            productRepository.update(any())
        } returns true

        val result = productService.updateProduct(
            productId,
            updateRequest()
        )

        assertEquals("Bàn phím cơ Updated", result.productName)
        assertEquals(
            BigDecimal("380000.00"),
            result.salePrice
        )

        verify(exactly = 1) {
            productRepository.update(any())
        }
    }

    @Test
    fun `updateProduct should throw when product does not exist`() {
        every {
            productRepository.findById(999L)
        } returns null

        assertThrows(ProductNotFoundException::class.java) {
            productService.updateProduct(
                999L,
                updateRequest()
            )
        }
    }

    // =====================================================
    // 8. KIỂM THỬ TRẠNG THÁI SẢN PHẨM
    // =====================================================

    @Test
    fun `deactivateProduct should deactivate active product`() {
        every {
            productRepository.findById(productId)
        } returns createSampleProduct(isActive = true)

        every {
            productRepository.deactivate(productId)
        } returns true

        productService.deactivateProduct(productId)

        verify(exactly = 1) {
            productRepository.deactivate(productId)
        }
    }

    @Test
    fun `deactivateProduct should reject inactive product`() {
        every {
            productRepository.findById(productId)
        } returns createSampleProduct(isActive = false)

        assertThrows(InvalidProductException::class.java) {
            productService.deactivateProduct(productId)
        }
    }

    @Test
    fun `reactivateProduct should reactivate inactive product`() {
        every {
            productRepository.findById(productId)
        } returns createSampleProduct(isActive = false)

        every {
            productRepository.reactivate(productId)
        } returns true

        productService.reactivateProduct(productId)

        verify(exactly = 1) {
            productRepository.reactivate(productId)
        }
    }

    // =====================================================
    // 9. KIỂM THỬ TRA CỨU SẢN PHẨM
    // =====================================================

    @Test
    fun `lookupProduct should find product by SKU`() {
        val product = createSampleProduct()

        every {
            productRepository.findBySku("SP000001")
        } returns product

        val result = productService.lookupProduct("SP000001")

        assertEquals("SP000001", result.sku)
    }

    @Test
    fun `lookupProduct should find product by barcode`() {
        val product = createSampleProduct()

        every {
            productRepository.findBySku("2001234567893")
        } returns null

        every {
            productRepository.findByBarcode("2001234567893")
        } returns product

        val result = productService.lookupProduct("2001234567893")

        assertEquals("2001234567893", result.barcode)
    }

    @Test
    fun `lookupProduct should throw when code does not exist`() {
        every {
            productRepository.findBySku("UNKNOWN")
        } returns null

        every {
            productRepository.findByBarcode("UNKNOWN")
        } returns null

        assertThrows(ProductNotFoundException::class.java) {
            productService.lookupProduct("UNKNOWN")
        }
    }

    @Test
    fun `lookupProduct should reject inactive product`() {
        every {
            productRepository.findBySku("SP000001")
        } returns createSampleProduct(isActive = false)

        assertThrows(InvalidProductException::class.java) {
            productService.lookupProduct("SP000001")
        }
    }
}
