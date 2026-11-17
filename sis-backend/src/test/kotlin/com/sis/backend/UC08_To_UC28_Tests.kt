package com.sis.backend

import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Services
import com.sis.backend.service.ProductService
import com.sis.backend.service.CategoryService
import com.sis.backend.service.SupplierService

// Repositories
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.SupplierRepository

// DTOs
import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.product.UpdateProductRequest
import com.sis.backend.dto.product.ProductPageResponse
import com.sis.backend.dto.product.ProductResponse
import com.sis.backend.dto.product.ProductDetailResponse
import com.sis.backend.dto.ProductSearchFilter
import com.sis.backend.dto.CreateCategoryRequest
import com.sis.backend.dto.UpdateCategoryRequest
import com.sis.backend.dto.CreateSupplierRequest
import com.sis.backend.dto.UpdateSupplierRequest
import com.sis.backend.dto.SupplierResponse
import com.sis.backend.dto.PageResult

// Entities
import com.sis.backend.entity.Product
import com.sis.backend.entity.Category
import com.sis.backend.entity.Supplier

// Exceptions
import com.sis.backend.exception.*

/**
 * Bộ test toàn diện cho 21 UCs (UC-08 đến UC-28)
 * 
 * UC-08 đến UC-17: Quản lý Sản Phẩm (10 UCs)
 * UC-18 đến UC-22: Quản lý Danh Mục (5 UCs)
 * UC-23 đến UC-28: Quản lý Nhà Cung Cấp (6 UCs)
 * 
 * Tổng cộng: 21 test cases cho 21 UCs
 */

@DisplayName("UC-08 đến UC-28: Comprehensive Test Suite")
class UC08_To_UC28_Tests {

    // ==================== PRODUCT SERVICE TESTS (UC-08 to UC-17) ====================
    
    @Nested
    @DisplayName("UC-08 to UC-17: Quản Lý Sản Phẩm")
    inner class ProductManagementTests {
        private lateinit var productService: ProductService
        private val productRepository = mockk<ProductRepository>()
        private val categoryRepository = mockk<CategoryRepository>()
        private val supplierRepository = mockk<SupplierRepository>()

        @BeforeEach
        fun setup() {
            productService = ProductService(productRepository, categoryRepository, supplierRepository)
        }

        private fun createTestProduct(
            productId: Long = 1L,
            sku: String = "SP000001",
            isActive: Boolean = true
        ) = Product(
            productId = productId,
            sku = sku,
            barcode = "2000000000001",
            productName = "Sản Phẩm Test",
            imageUrl = null,
            categoryId = 1L,
            categoryName = "Điện Tử",
            supplierId = 1L,
            supplierName = "Công Ty A",
            unit = "cái",
            costPrice = BigDecimal("100.00"),
            salePrice = BigDecimal("150.00"),
            minStock = 10,
            maxStock = 100,
            isActive = isActive,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            stockQuantity = 50,
            reservedQuantity = 10,
            availableQuantity = 40
        )

        // ========== UC-08: Thêm sản phẩm ==========
        @Test
        @DisplayName("UC-08: Thêm sản phẩm - tạo sản phẩm hợp lệ")
        fun uc08_createValidProduct() {
            every { categoryRepository.existsActiveById(1L) } returns true
            every { supplierRepository.existsActiveById(1L) } returns true
            every { productRepository.existsSku("SP000001") } returns false
            every { productRepository.existsBarcode("2000000000001") } returns false
            every { productRepository.create(any()) } returns 1L
            every { productRepository.findById(1L) } returns createTestProduct()

            val request = CreateProductRequest(
                productName = "Sản Phẩm Mới",
                unit = "cái",
                costPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("150.00"),
                minStock = 10,
                maxStock = 100,
                categoryId = 1L,
                supplierId = 1L,
                sku = "SP000001",
                barcode = "2000000000001"
            )

            val product = productService.createProduct(request)
            
            assertNotNull(product)
            assertEquals(1L, product.productId)
            assertTrue(product.isActive)
        }

        @Test
        @DisplayName("UC-08: Thêm sản phẩm - lỗi giá bán < giá vốn")
        fun uc08_rejectInvalidPrice() {
            every { categoryRepository.existsActiveById(1L) } returns true
            every { supplierRepository.existsActiveById(1L) } returns true

            val request = CreateProductRequest(
                productName = "Sản Phẩm",
                unit = "cái",
                costPrice = BigDecimal("150.00"),
                salePrice = BigDecimal("100.00"),
                minStock = 10,
                maxStock = 100,
                categoryId = 1L,
                supplierId = 1L
            )

            assertThrows<InvalidProductException> {
                productService.createProduct(request)
            }
        }

        // ========== UC-09: Xem danh sách sản phẩm ==========
        @Test
        @DisplayName("UC-09: Xem danh sách sản phẩm - lấy toàn bộ")
        fun uc09_getAllProducts() {
            val mockProduct1 = createTestProduct(1L, "SP000001")
            val mockProduct2 = createTestProduct(2L, "SP000002")
            
            every { productRepository.findAll(null, false) } returns listOf(mockProduct1, mockProduct2)

            val products = productService.getAllProducts(keyword = null, includeInactive = false)

            assertEquals(2, products.size)
        }

        @Test
        @DisplayName("UC-09: Xem danh sách sản phẩm - tìm kiếm theo keyword")
        fun uc09_searchProducts() {
            every { productRepository.findAll("Điện", false) } returns listOf(
                createTestProduct(1L)
            )

            val products = productService.getAllProducts(keyword = "Điện", includeInactive = false)

            assertEquals(1, products.size)
        }

        // ========== UC-10: Xem chi tiết sản phẩm ==========
        @Test
        @DisplayName("UC-10: Xem chi tiết sản phẩm - lấy thông tin đầy đủ")
        fun uc10_getProductDetail() {
            every { productRepository.findById(1L) } returns createTestProduct()
            every { productRepository.findInventories(1L) } returns emptyList()

            val detail = productService.getProductDetail(1L)

            assertNotNull(detail)
            assertEquals(1L, detail.product.productId)
        }

        @Test
        @DisplayName("UC-10: Xem chi tiết sản phẩm - sản phẩm không tồn tại")
        fun uc10_productNotFound() {
            every { productRepository.findById(999L) } returns null

            assertThrows<ProductNotFoundException> {
                productService.getProductDetail(999L)
            }
        }

        // ========== UC-11: Cập nhật sản phẩm ==========
        @Test
        @DisplayName("UC-11: Cập nhật sản phẩm - thay đổi thông tin")
        fun uc11_updateProduct() {
            val testProduct = createTestProduct()
            every { productRepository.findById(1L) } returns testProduct
            every { categoryRepository.existsActiveById(1L) } returns true
            every { supplierRepository.existsActiveById(1L) } returns true
            every { productRepository.existsSku("SP000001", 1L) } returns false
            every { productRepository.existsBarcode(any(), any()) } returns false
            every { productRepository.update(any()) } returns true
            every { productRepository.findById(1L) } returns testProduct

            val request = UpdateProductRequest(
                sku = "SP000001",
                barcode = "2000000000001",
                productName = "Sản Phẩm Cập Nhật",
                unit = "chiếc",
                costPrice = BigDecimal("120.00"),
                salePrice = BigDecimal("180.00"),
                minStock = 15,
                maxStock = 120,
                categoryId = 1L,
                supplierId = 1L,
                imageUrl = null
            )

            val updated = productService.updateProduct(1L, request)

            assertNotNull(updated)
            assertEquals(1L, updated.productId)
        }

        // ========== UC-12: Ngừng kinh doanh sản phẩm ==========
        @Test
        @DisplayName("UC-12: Ngừng kinh doanh sản phẩm - deactivate thành công")
        fun uc12_deactivateProduct() {
            every { productRepository.findById(1L) } returns createTestProduct(isActive = true)
            every { productRepository.deactivate(1L) } returns true

            productService.deactivateProduct(1L)

            verify { productRepository.deactivate(1L) }
        }

        @Test
        @DisplayName("UC-12: Ngừng kinh doanh sản phẩm - sản phẩm không tồn tại")
        fun uc12_deactivateNotFound() {
            every { productRepository.findById(999L) } returns null

            assertThrows<ProductNotFoundException> {
                productService.deactivateProduct(999L)
            }
        }

        // ========== UC-13: Tìm kiếm sản phẩm ==========
        @Test
        @DisplayName("UC-13: Tìm kiếm sản phẩm - search by keyword")
        fun uc13_searchByKeyword() {
            every { productRepository.findAll("Test", false) } returns listOf(
                createTestProduct(1L)
            )

            val products = productService.getAllProducts(keyword = "Test", includeInactive = false)

            assertEquals(1, products.size)
        }

        // ========== UC-14: Lọc và sắp xếp sản phẩm ==========
        @Test
        @DisplayName("UC-14: Lọc và sắp xếp sản phẩm - filter by category")
        fun uc14_filterByCategory() {
            val filter = ProductSearchFilter(categoryId = 1L)
            val mockPageResult = mockPageResult(
                listOf(createTestProduct(1L))
            )
            every { productRepository.findPage(filter, 1, 10) } returns mockPageResult

            val result = productService.getProductsPage(filter, 1, 10)

            assertEquals(1, result.items.size)
        }

        // ========== UC-15: Kiểm tra trùng sản phẩm ==========
        @Test
        @DisplayName("UC-15: Kiểm tra trùng sản phẩm - SKU trùng")
        fun uc15_checkDuplicateSku() {
            every { categoryRepository.existsActiveById(1L) } returns true
            every { supplierRepository.existsActiveById(1L) } returns true
            every { productRepository.existsSku("SP000001") } returns true

            assertThrows<DuplicateSkuException> {
                val request = CreateProductRequest(
                    productName = "Sản Phẩm",
                    unit = "cái",
                    costPrice = BigDecimal("100.00"),
                    salePrice = BigDecimal("150.00"),
                    minStock = 10,
                    maxStock = 100,
                    categoryId = 1L,
                    supplierId = 1L,
                    sku = "SP000001"
                )
                productService.createProduct(request)
            }
        }

        // ========== UC-16: Tạo mã vạch/QR ==========
        @Test
        @DisplayName("UC-16: Tạo mã vạch - automatic barcode generation")
        fun uc16_generateBarcodeAutomatic() {
            every { categoryRepository.existsActiveById(1L) } returns true
            every { supplierRepository.existsActiveById(1L) } returns true
            every { productRepository.existsSku(any()) } returns false
            every { productRepository.existsBarcode(any()) } returns false
            every { productRepository.create(any()) } returns 1L
            every { productRepository.findById(1L) } returns createTestProduct()

            val request = CreateProductRequest(
                productName = "Sản Phẩm",
                unit = "cái",
                costPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("150.00"),
                minStock = 10,
                maxStock = 100,
                categoryId = 1L,
                supplierId = 1L
                // No SKU/barcode provided - should be auto-generated
            )

            val product = productService.createProduct(request)

            assertNotNull(product.barcode)
            assertTrue(product.barcode.isNotEmpty())
        }

        // ========== UC-17: Tra cứu sản phẩm bằng SKU/Barcode ==========
        @Test
        @DisplayName("UC-17: Tra cứu sản phẩm bằng SKU")
        fun uc17_lookupBySkuOrBarcode() {
            every { productRepository.findBySku("SP000001") } returns createTestProduct()

            val product = productService.lookupProduct("SP000001")

            assertNotNull(product)
            assertEquals("SP000001", product.sku)
        }

        private fun mockPageResult(items: List<Product>): com.sis.backend.dto.PageResult<Product> {
            return mockk<com.sis.backend.dto.PageResult<Product>> {
                every { this@mockk.items } returns items
                every { page } returns 1
                every { size } returns 10
                every { totalItems } returns items.size.toLong()
                every { totalPages } returns 1
            }
        }
    }

    // ==================== CATEGORY SERVICE TESTS (UC-18 to UC-22) ====================
    
    @Nested
    @DisplayName("UC-18 to UC-22: Quản Lý Danh Mục")
    inner class CategoryManagementTests {
        private lateinit var categoryService: CategoryService
        private val categoryRepository = mockk<CategoryRepository>()

        @BeforeEach
        fun setup() {
            categoryService = CategoryService(categoryRepository)
        }

        private fun createTestCategory(
            categoryId: Long = 1L,
            categoryName: String = "Điện Tử",
            isActive: Boolean = true
        ) = Category(
            categoryId = categoryId,
            categoryName = categoryName,
            description = "Các sản phẩm điện tử",
            isActive = isActive,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        // ========== UC-18: Thêm danh mục ==========
        @Test
        @DisplayName("UC-18: Thêm danh mục - tạo danh mục hợp lệ")
        fun uc18_createValidCategory() {
            every { categoryRepository.existsByName("Điện Tử") } returns false
            every { categoryRepository.create(any()) } returns createTestCategory()

            val request = CreateCategoryRequest(
                categoryName = "Điện Tử",
                description = "Các sản phẩm điện tử"
            )

            val category = categoryService.createCategory(request)

            assertNotNull(category)
            assertEquals("Điện Tử", category.categoryName)
            assertTrue(category.isActive)
        }

        @Test
        @DisplayName("UC-18: Thêm danh mục - tên trống")
        fun uc18_rejectEmptyName() {
            val request = CreateCategoryRequest(categoryName = "")

            assertThrows<InvalidCategoryException> {
                categoryService.createCategory(request)
            }
        }

        @Test
        @DisplayName("UC-18: Thêm danh mục - tên trùng")
        fun uc18_rejectDuplicateName() {
            every { categoryRepository.existsByName("Điện Tử") } returns true

            val request = CreateCategoryRequest(categoryName = "Điện Tử")

            assertThrows<DuplicateCategoryException> {
                categoryService.createCategory(request)
            }
        }

        // ========== UC-19: Xem danh sách danh mục ==========
        @Test
        @DisplayName("UC-19: Xem danh sách danh mục - lấy toàn bộ")
        fun uc19_getAllCategories() {
            every { categoryRepository.findAll(null, false) } returns listOf(
                createTestCategory(1L, "Điện Tử"),
                createTestCategory(2L, "Đồ Gia Dụng")
            )

            val categories = categoryService.getCategories()

            assertEquals(2, categories.size)
        }

        @Test
        @DisplayName("UC-19: Xem danh sách danh mục - tìm kiếm")
        fun uc19_searchCategories() {
            every { categoryRepository.findAll("Điện", false) } returns listOf(
                createTestCategory(1L, "Điện Tử")
            )

            val categories = categoryService.getCategories(keyword = "Điện")

            assertEquals(1, categories.size)
        }

        // ========== UC-20: Cập nhật danh mục ==========
        @Test
        @DisplayName("UC-20: Cập nhật danh mục - thay đổi thông tin")
        fun uc20_updateCategory() {
            every { categoryRepository.findById(1L) } returns createTestCategory(1L)
            every { categoryRepository.existsByName("Điện Tử Mới", 1L) } returns false
            every { categoryRepository.update(1L, any(), any()) } returns 
                createTestCategory(1L, "Điện Tử Mới")

            val request = UpdateCategoryRequest(
                categoryName = "Điện Tử Mới",
                description = "Sản phẩm điện tử cập nhật"
            )

            val updated = categoryService.updateCategory(1L, request)

            assertNotNull(updated)
            assertEquals("Điện Tử Mới", updated.categoryName)
        }

        // ========== UC-21: Ngừng sử dụng danh mục ==========
        @Test
        @DisplayName("UC-21: Ngừng sử dụng danh mục - deactivate thành công")
        fun uc21_deactivateCategory() {
            every { categoryRepository.findById(1L) } returns createTestCategory(isActive = true)
            every { categoryRepository.hasActiveProducts(1L) } returns false
            every { categoryRepository.deactivate(1L) } returns true

            val result = categoryService.deactivateCategory(1L)

            assertTrue(result)
        }

        @Test
        @DisplayName("UC-21: Ngừng sử dụng danh mục - còn sản phẩm đang dùng")
        fun uc21_cannotDeactivateIfProductsExist() {
            every { categoryRepository.findById(1L) } returns createTestCategory(isActive = true)
            every { categoryRepository.hasActiveProducts(1L) } returns true

            assertThrows<CategoryInUseException> {
                categoryService.deactivateCategory(1L)
            }
        }

        // ========== UC-22: Tìm kiếm danh mục ==========
        @Test
        @DisplayName("UC-22: Tìm kiếm danh mục - search by keyword")
        fun uc22_searchCategoriesByKeyword() {
            every { categoryRepository.findAll("Điện", false) } returns listOf(
                createTestCategory(1L, "Điện Tử")
            )

            val categories = categoryService.getCategories(keyword = "Điện")

            assertEquals(1, categories.size)
            assertEquals("Điện Tử", categories[0].categoryName)
        }
    }

    // ==================== SUPPLIER SERVICE TESTS (UC-23 to UC-28) ====================
    
    @Nested
    @DisplayName("UC-23 to UC-28: Quản Lý Nhà Cung Cấp")
    inner class SupplierManagementTests {
        private lateinit var supplierService: SupplierService
        private val supplierRepository = mockk<SupplierRepository>()

        @BeforeEach
        fun setup() {
            supplierService = SupplierService(supplierRepository)
        }

        private fun createTestSupplier(
            supplierId: Long = 1L,
            supplierName: String = "Công Ty A",
            isActive: Boolean = true
        ) = Supplier(
            supplierId = supplierId,
            supplierName = supplierName,
            phone = "0123456789",
            email = "contact@company.com",
            address = "123 Đường Chính, TP HCM",
            isActive = isActive,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // ========== UC-23: Thêm nhà cung cấp ==========
        @Test
        @DisplayName("UC-23: Thêm nhà cung cấp - tạo thành công")
        fun uc23_createValidSupplier() {
            every { supplierRepository.existsByName("Công Ty A") } returns false
            every { supplierRepository.create(any(), any(), any(), any()) } returns 1L
            every { supplierRepository.findById(1L) } returns createTestSupplier()

            val request = CreateSupplierRequest(
                supplierName = "Công Ty A",
                phone = "0123456789",
                email = "contact@company.com",
                address = "123 Đường Chính"
            )

            val supplier = supplierService.createSupplier(request)

            assertNotNull(supplier)
            assertEquals("Công Ty A", supplier.supplierName)
        }

        @Test
        @DisplayName("UC-23: Thêm nhà cung cấp - tên trống")
        fun uc23_rejectEmptyName() {
            val request = CreateSupplierRequest(supplierName = "")

            assertThrows<InvalidSupplierException> {
                supplierService.createSupplier(request)
            }
        }

        @Test
        @DisplayName("UC-23: Thêm nhà cung cấp - tên trùng")
        fun uc23_rejectDuplicateName() {
            every { supplierRepository.existsByName("Công Ty A") } returns true

            val request = CreateSupplierRequest(supplierName = "Công Ty A")

            assertThrows<DuplicateSupplierException> {
                supplierService.createSupplier(request)
            }
        }

        // ========== UC-24: Xem danh sách nhà cung cấp ==========
        @Test
        @DisplayName("UC-24: Xem danh sách nhà cung cấp - lấy toàn bộ")
        fun uc24_getAllSuppliers() {
            val mockSupplier = mockk<Supplier>()
            every { supplierRepository.findAll(null, false) } returns listOf(mockSupplier)

            val suppliers = supplierService.getSuppliers(keyword = null)

            assertEquals(1, suppliers.size)
        }

        // ========== UC-25: Xem chi tiết nhà cung cấp ==========
        @Test
        @DisplayName("UC-25: Xem chi tiết nhà cung cấp - lấy thông tin đầy đủ")
        fun uc25_getSupplierDetail() {
            every { supplierRepository.findById(1L) } returns createTestSupplier()
            every { supplierRepository.findProductsBySupplierId(1L) } returns emptyList()
            every { supplierRepository.findImportTransactionsBySupplierId(1L) } returns emptyList()

            val supplier = supplierService.getSupplierDetail(1L)

            assertNotNull(supplier)
            assertEquals(1L, supplier.supplierId)
            assertEquals("Công Ty A", supplier.supplierName)
        }

        @Test
        @DisplayName("UC-25: Xem chi tiết nhà cung cấp - không tồn tại")
        fun uc25_supplierNotFound() {
            every { supplierRepository.findById(999L) } returns null

            assertThrows<SupplierNotFoundException> {
                supplierService.getSupplierDetail(999L)
            }
        }

        // ========== UC-26: Cập nhật nhà cung cấp ==========
        @Test
        @DisplayName("UC-26: Cập nhật nhà cung cấp - thay đổi thông tin")
        fun uc26_updateSupplier() {
            val testSupplier = createTestSupplier()
            every { supplierRepository.findById(1L) } returns testSupplier
            every { supplierRepository.existsByName("Công Ty A Mới", 1L) } returns false
            every { supplierRepository.findById(1L) } returns createTestSupplier(1L, "Công Ty A Mới")

            val request = UpdateSupplierRequest(
                supplierName = "Công Ty A Mới",
                phone = "0987654321",
                email = "new@company.com",
                address = "456 Đường Mới"
            )

            val updated = supplierService.updateSupplier(1L, request)

            assertNotNull(updated)
            assertEquals("Công Ty A Mới", updated.supplierName)
        }

        // ========== UC-27: Ngừng hợp tác nhà cung cấp ==========
        @Test
        @DisplayName("UC-27: Ngừng hợp tác nhà cung cấp - deactivate thành công")
        fun uc27_deactivateSupplier() {
            every { supplierRepository.findById(1L) } returns createTestSupplier(isActive = true)

            supplierService.deactivateSupplier(1L)

            verify { supplierRepository.deactivate(1L) }
        }

        @Test
        @DisplayName("UC-27: Ngừng hợp tác nhà cung cấp - sản phẩm không tồn tại")
        fun uc27_deactivateNotFound() {
            every { supplierRepository.findById(999L) } returns null

            assertThrows<SupplierNotFoundException> {
                supplierService.deactivateSupplier(999L)
            }
        }

        // ========== UC-28: Tìm kiếm nhà cung cấp ==========
        @Test
        @DisplayName("UC-28: Tìm kiếm nhà cung cấp - search by keyword")
        fun uc28_searchSuppliersByKeyword() {
            val mockSupplier = mockk<Supplier>()
            every { supplierRepository.findAll("Công Ty", false) } returns listOf(mockSupplier)

            val suppliers = supplierService.getSuppliers(keyword = "Công Ty")

            assertEquals(1, suppliers.size)
        }

        @Test
        @DisplayName("UC-28: Tìm kiếm nhà cung cấp - không tìm thấy")
        fun uc28_searchNoResults() {
            every { supplierRepository.findAll("NotExist", false) } returns emptyList()

            val suppliers = supplierService.getSuppliers(keyword = "NotExist")

            assertEquals(0, suppliers.size)
        }
    }
}
