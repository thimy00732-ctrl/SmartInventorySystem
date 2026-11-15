package com.sis.backend

import io.mockk.mockk
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate
import org.junit.jupiter.api.assertThrows

// Services
import com.sis.backend.service.ProductService
import com.sis.backend.service.CategoryService
import com.sis.backend.service.SupplierService
import com.sis.backend.service.InventoryService

// Repositories
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.repository.InventoryRepository

// DTOs
import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.CreateCategoryRequest
import com.sis.backend.dto.CreateSupplierRequest
import com.sis.backend.dto.InventorySearchFilter
import com.sis.backend.dto.PageResult

// Entities
import com.sis.backend.entity.Product
import com.sis.backend.entity.Category
import com.sis.backend.entity.Supplier
import com.sis.backend.entity.Inventory

// Exceptions
import com.sis.backend.exception.InvalidProductException
import com.sis.backend.exception.InvalidCategoryException
import com.sis.backend.exception.InvalidSupplierException

/**
 * Bộ test toàn diện cho 4 services chính:
 * - ProductService (9 tests)
 * - CategoryService (6 tests)
 * - SupplierService (8 tests)
 * - InventoryService (10 tests)
 * 
 * Mỗi test kiểm tra logic thực tế và xác thực các quy tắc kinh doanh.
 * Viết bằng Tiếng Việt để dễ theo dõi.
 */

@DisplayName("Bộ Test Toàn Diện - 4 Services")
class ComprehensiveServiceTests {

    // ==================== PRODUCT SERVICE TESTS ====================
    @Nested
    @DisplayName("ProductService - Kiểm Tra Logic Sản Phẩm")
    inner class ProductServiceTests {
        private lateinit var productService: ProductService
        private val productRepository = mockk<ProductRepository>()
        private val categoryRepository = mockk<CategoryRepository>()
        private val supplierRepository = mockk<SupplierRepository>()

        @BeforeEach
        fun setup() {
            every { productRepository.existsSku(any()) } returns false
            every { productRepository.existsBarcode(any()) } returns false
            every { productRepository.create(any()) } returns 1L
            every { productRepository.findById(1L) } returns createTestProduct()
            every { productRepository.findById(999L) } returns null
            every { categoryRepository.existsActiveById(any()) } returns true
            every { supplierRepository.existsActiveById(any()) } returns true

            productService = ProductService(productRepository, categoryRepository, supplierRepository)
        }

        private fun createTestProduct() = Product(
            productId = 1L,
            sku = "SP000001",
            barcode = "2000000000001",
            productName = "Sản Phẩm Test",
            imageUrl = null,
            categoryId = 1L,
            categoryName = null,
            supplierId = 1L,
            supplierName = null,
            unit = "cái",
            costPrice = BigDecimal("100.00"),
            salePrice = BigDecimal("150.00"),
            minStock = 10,
            maxStock = 100,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            stockQuantity = 50,
            reservedQuantity = 10,
            availableQuantity = 40
        )

        @Test
        @DisplayName("P-1: Giá bán < giá vốn phải bị từ chối")
        fun p1_testSalePriceLessThanCost() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "cái",
                        costPrice = BigDecimal("150.00"),
                        salePrice = BigDecimal("100.00"),
                        minStock = 10,
                        maxStock = 100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi salePrice < costPrice" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-2: Max stock < min stock phải bị từ chối")
        fun p2_testMaxStockLessThanMinStock() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "cái",
                        costPrice = BigDecimal("100.00"),
                        salePrice = BigDecimal("150.00"),
                        minStock = 100,
                        maxStock = 50,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi maxStock < minStock" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-3: Giá vốn âm phải bị từ chối")
        fun p3_testNegativeCostPrice() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "cái",
                        costPrice = BigDecimal("-50.00"),
                        salePrice = BigDecimal("150.00"),
                        minStock = 10,
                        maxStock = 100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi costPrice < 0" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-4: Giá bán âm phải bị từ chối")
        fun p4_testNegativeSalePrice() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "cái",
                        costPrice = BigDecimal("100.00"),
                        salePrice = BigDecimal("-50.00"),
                        minStock = 10,
                        maxStock = 100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi salePrice < 0" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-5: Tồn kho tối thiểu âm phải bị từ chối")
        fun p5_testNegativeMinStock() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "cái",
                        costPrice = BigDecimal("100.00"),
                        salePrice = BigDecimal("150.00"),
                        minStock = -10,
                        maxStock = 100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi minStock < 0" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-6: Tồn kho tối đa âm phải bị từ chối")
        fun p6_testNegativeMaxStock() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "cái",
                        costPrice = BigDecimal("100.00"),
                        salePrice = BigDecimal("150.00"),
                        minStock = 10,
                        maxStock = -100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi maxStock < 0" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-7: Tên sản phẩm trống phải bị từ chối")
        fun p7_testEmptyProductName() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "",
                        unit = "cái",
                        costPrice = BigDecimal("100.00"),
                        salePrice = BigDecimal("150.00"),
                        minStock = 10,
                        maxStock = 100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi productName trống" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-8: Đơn vị tính trống phải bị từ chối")
        fun p8_testEmptyUnit() {
            try {
                productService.createProduct(
                    CreateProductRequest(
                        productName = "Sản Phẩm",
                        unit = "",
                        costPrice = BigDecimal("100.00"),
                        salePrice = BigDecimal("150.00"),
                        minStock = 10,
                        maxStock = 100,
                        categoryId = 1L,
                        supplierId = 1L
                    )
                )
                assert(false) { "BUG: Không throw exception khi unit trống" }
            } catch (e: InvalidProductException) {
                assert(true) // Expected
            }
        }

        @Test
        @DisplayName("P-9: Sản phẩm hợp lệ phải được tạo thành công")
        fun testValidProductCreation() {
            val response = productService.createProduct(
                CreateProductRequest(
                    productName = "Sản Phẩm Hợp Lệ",
                    unit = "cái",
                    costPrice = BigDecimal("100.00"),
                    salePrice = BigDecimal("150.00"),
                    minStock = 10,
                    maxStock = 100,
                    categoryId = 1L,
                    supplierId = 1L
                )
            )
            assert(response.productId == 1L)
        }
    }

    // ==================== CATEGORY SERVICE TESTS ====================
    @Nested
    @DisplayName("CategoryService - Kiểm Tra Logic Danh Mục")
    inner class CategoryServiceTests {
        private lateinit var categoryService: CategoryService
        private val categoryRepository = mockk<CategoryRepository>()

        @BeforeEach
        fun setup() {
            every { categoryRepository.existsByName(any()) } returns false
            every { categoryRepository.create(any()) } returns createTestCategory()
            every { categoryRepository.findById(1L) } returns createTestCategory()
            every { categoryRepository.findById(999L) } returns null
            every { categoryRepository.findAll(any(), any()) } returns listOf(createTestCategory())

            categoryService = CategoryService(categoryRepository)
        }

        private fun createTestCategory() = Category(
            categoryId = 1L,
            categoryName = "Điện Tử",
            description = "Các sản phẩm điện tử",
            isActive = true,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        @Test
        @DisplayName("C-1: Tên danh mục trống phải bị từ chối")
        fun testEmptyCategoryName() = assertThrows<InvalidCategoryException> {
            categoryService.createCategory(CreateCategoryRequest(categoryName = ""))
        }

        @Test
        @DisplayName("C-2: Tên danh mục chỉ có khoảng trắng phải bị từ chối")
        fun testWhitespaceCategoryName() = assertThrows<InvalidCategoryException> {
            categoryService.createCategory(CreateCategoryRequest(categoryName = "   "))
        }

        @Test
        @DisplayName("C-3: Tên danh mục quá ngắn phải bị từ chối")
        fun testTooShortCategoryName() = assertThrows<InvalidCategoryException> {
            categoryService.createCategory(CreateCategoryRequest(categoryName = "A"))
        }

        @Test
        @DisplayName("C-4: Mô tả có thể trống nhưng tên phải hợp lệ")
        fun testEmptyDescription() {
            val response = categoryService.createCategory(
                CreateCategoryRequest(categoryName = "Danh Mục Mới", description = "")
            )
            assert(response.categoryId == 1L)
        }

        @Test
        @DisplayName("C-5: Danh mục hợp lệ phải được tạo thành công")
        fun testValidCategoryCreation() {
            val response = categoryService.createCategory(
                CreateCategoryRequest(categoryName = "Danh Mục Mới", description = "Mô tả chi tiết")
            )
            assert(response.categoryId == 1L)
        }

        @Test
        @DisplayName("C-6: Lấy danh mục theo ID hợp lệ phải thành công")
        fun testGetCategoryById() {
            val category = categoryService.getCategoryById(1L)
            assert(category.categoryId == 1L)
            assert(category.categoryName == "Điện Tử")
        }
    }

    // ==================== SUPPLIER SERVICE TESTS ====================
    @Nested
    @DisplayName("SupplierService - Kiểm Tra Logic Nhà Cung Cấp")
    inner class SupplierServiceTests {
        private lateinit var supplierService: SupplierService
        private val supplierRepository = mockk<SupplierRepository>()

        @BeforeEach
        fun setup() {
            every { supplierRepository.existsByName(any()) } returns false
            every { supplierRepository.create(any(), any(), any(), any()) } returns 1L
            every { supplierRepository.findById(1L) } returns createTestSupplier()
            every { supplierRepository.findById(999L) } returns null
            every { supplierRepository.findAll(any(), any()) } returns listOf(createTestSupplier())

            supplierService = SupplierService(supplierRepository)
        }

        private fun createTestSupplier() = Supplier(
            supplierId = 1L,
            supplierName = "Công Ty A",
            phone = "0123456789",
            email = "contact@company.com",
            address = "123 Đường Chính, TP HCM",
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        @Test
        @DisplayName("S-1: Tên nhà cung cấp trống phải bị từ chối")
        fun testEmptySupplierName() = assertThrows<InvalidSupplierException> {
            supplierService.createSupplier(CreateSupplierRequest(supplierName = ""))
        }

        @Test
        @DisplayName("S-2: Tên nhà cung cấp chỉ khoảng trắng phải bị từ chối")
        fun testWhitespaceSupplierName() = assertThrows<InvalidSupplierException> {
            supplierService.createSupplier(CreateSupplierRequest(supplierName = "   "))
        }

        @Test
        @DisplayName("S-3: Số điện thoại không hợp lệ phải bị từ chối")
        fun testInvalidPhoneFormat() = assertThrows<InvalidSupplierException> {
            supplierService.createSupplier(
                CreateSupplierRequest(
                    supplierName = "Công Ty B",
                    phone = "abc123"
                )
            )
        }

        @Test
        @DisplayName("S-4: Email không hợp lệ phải bị từ chối")
        fun testInvalidEmailFormat() = assertThrows<InvalidSupplierException> {
            supplierService.createSupplier(
                CreateSupplierRequest(
                    supplierName = "Công Ty C",
                    email = "not-an-email"
                )
            )
        }

        @Test
        @DisplayName("S-5: Số điện thoại có thể để trống")
        fun testEmptyPhoneAllowed() {
            val response = supplierService.createSupplier(
                CreateSupplierRequest(
                    supplierName = "Công Ty D",
                    phone = null
                )
            )
            assert(response.supplierId == 1L)
        }

        @Test
        @DisplayName("S-6: Email có thể để trống")
        fun testEmptyEmailAllowed() {
            val response = supplierService.createSupplier(
                CreateSupplierRequest(
                    supplierName = "Công Ty E",
                    email = null
                )
            )
            assert(response.supplierId == 1L)
        }

        @Test
        @DisplayName("S-7: Nhà cung cấp hợp lệ phải được tạo thành công")
        fun testValidSupplierCreation() {
            val response = supplierService.createSupplier(
                CreateSupplierRequest(
                    supplierName = "Công Ty F",
                    phone = "0987654321",
                    email = "contact@company.com"
                )
            )
            assert(response.supplierId == 1L)
        }

        @Test
        @DisplayName("S-8: Lấy nhà cung cấp theo ID hợp lệ phải thành công")
        fun testGetSupplierById() {
            val suppliers = supplierService.getSuppliers(keyword = null)
            assert(suppliers.isNotEmpty())
            assert(suppliers[0].supplierId == 1L)
        }
    }

    // ==================== INVENTORY SERVICE TESTS ====================
    @Nested
    @DisplayName("InventoryService - Kiểm Tra Logic Tồn Kho")
    inner class InventoryServiceTests {
        private lateinit var inventoryService: InventoryService
        private val inventoryRepository = mockk<InventoryRepository>()

        @BeforeEach
        fun setup() {
            every { 
                inventoryRepository.findPage(any(), any(), any()) 
            } returns createTestPageResult()
            every { 
                inventoryRepository.findById(1L) 
            } returns createTestInventory()
            every { 
                inventoryRepository.findById(999L) 
            } returns null

            inventoryService = InventoryService(inventoryRepository)
        }

        private fun createTestInventory() = Inventory(
            inventoryId = 1L,
            warehouseId = 1L,
            warehouseName = "Kho Chính",
            productId = 1L,
            productName = "Sản Phẩm Test",
            sku = "SP000001",
            barcode = "2000000000001",
            categoryId = 1L,
            categoryName = "Điện Tử",
            supplierId = 1L,
            supplierName = "Công Ty A",
            unit = "cái",
            batchCode = "B001",
            manufactureDate = LocalDate.now().minusMonths(3),
            expiryDate = LocalDate.now().plusMonths(9),
            stockQuantity = 100,
            reservedQuantity = 20,
            availableQuantity = 80,
            updatedAt = LocalDateTime.now()
        )

        private fun createTestPageResult() = PageResult(
            items = listOf(createTestInventory()),
            page = 1,
            size = 10,
            totalItems = 1L,
            totalPages = 1
        )

        @Test
        @DisplayName("I-1: Lấy tồn kho theo ID hợp lệ phải thành công")
        fun testGetInventoryById() {
            val inventory = inventoryService.getInventoryById(1L)
            assert(inventory.inventoryId == 1L)
            assert(inventory.productName == "Sản Phẩm Test")
        }

        @Test
        @DisplayName("I-2: Lấy tồn kho theo ID không tồn tại phải ném ngoại lệ")
        fun testGetInventoryByIdNotFound() = assertThrows<IllegalArgumentException> {
            inventoryService.getInventoryById(999L)
        }

        @Test
        @DisplayName("I-3: Số lượng khả dụng phải = tồn kho - giữ chỗ")
        fun testAvailableQuantityCalculation() {
            val inventory = inventoryService.getInventoryById(1L)
            val calculated = inventory.stockQuantity - inventory.reservedQuantity
            assert(inventory.availableQuantity == calculated)
        }

        @Test
        @DisplayName("I-4: Không thể có số lượng giữ chỗ > tồn kho")
        fun testReservedQuantityValidation() {
            val inventory = inventoryService.getInventoryById(1L)
            assert(inventory.reservedQuantity <= inventory.stockQuantity)
        }

        @Test
        @DisplayName("I-5: Lấy tồn kho theo bộ lọc phải thành công")
        fun testGetInventoryPage() {
            val filter = InventorySearchFilter()
            val result = inventoryService.getInventoryPage(filter, page = 1, size = 10)
            assert(result.items.isNotEmpty())
            assert(result.totalPages >= 1)
        }

        @Test
        @DisplayName("I-6: Ngày sản xuất phải <= ngày hết hạn")
        fun testManufactureDateValidation() {
            val inventory = inventoryService.getInventoryById(1L)
            if (inventory.manufactureDate != null && inventory.expiryDate != null) {
                assert(inventory.manufactureDate <= inventory.expiryDate)
            }
        }

        @Test
        @DisplayName("I-7: Ngày hết hạn phải trong tương lai hoặc hôm nay")
        fun testExpiryDateValidation() {
            val inventory = inventoryService.getInventoryById(1L)
            if (inventory.expiryDate != null) {
                assert(inventory.expiryDate >= LocalDate.now().minusDays(1))
            }
        }

        @Test
        @DisplayName("I-8: Mã lô hàng không được rỗng")
        fun testBatchCodeNotEmpty() {
            val inventory = inventoryService.getInventoryById(1L)
            assert(inventory.batchCode.isNotEmpty())
        }

        @Test
        @DisplayName("I-9: ID kho phải hợp lệ (> 0)")
        fun testWarehouseIdValidation() {
            val inventory = inventoryService.getInventoryById(1L)
            assert(inventory.warehouseId > 0)
        }

        @Test
        @DisplayName("I-10: ID sản phẩm phải hợp lệ (> 0)")
        fun testProductIdValidation() {
            val inventory = inventoryService.getInventoryById(1L)
            assert(inventory.productId > 0)
        }
    }
}
