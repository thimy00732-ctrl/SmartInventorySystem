package com.sis.backend.service

import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.product.ProductDetailResponse
import com.sis.backend.dto.product.ProductResponse
import com.sis.backend.dto.product.UpdateProductRequest
import com.sis.backend.entity.Product
import com.sis.backend.exception.DuplicateBarcodeException
import com.sis.backend.exception.DuplicateSkuException
import com.sis.backend.exception.InvalidProductCategoryException
import com.sis.backend.exception.InvalidProductException
import com.sis.backend.exception.InvalidProductSupplierException
import com.sis.backend.exception.ProductCodeGenerationException
import com.sis.backend.exception.ProductNotFoundException
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.SupplierRepository
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.LocalDateTime
import com.sis.backend.dto.product.ProductPageResponse
import com.sis.backend.repository.ProductSearchFilter

/**
 * Xử lý nghiệp vụ quản lý sản phẩm.
 *
 * Chịu trách nhiệm kiểm tra dữ liệu, sinh mã sản phẩm,
 * xác thực danh mục và nhà cung cấp trước khi thao tác dữ liệu.
 */
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val supplierRepository: SupplierRepository
) {

    private val random = SecureRandom()

    /**
     * Lấy danh sách sản phẩm.
     *
     * @param keyword từ khóa tìm kiếm
     * @param includeInactive có lấy sản phẩm ngừng hoạt động hay không
     * @return danh sách sản phẩm
     */
    fun getAllProducts(
        keyword: String?,
        includeInactive: Boolean
    ): List<ProductResponse> {
        return productRepository.findAll(keyword, includeInactive)
            .map { it.toProductResponse() }
    }

    /**
     * Lấy danh sách sản phẩm theo điều kiện tìm kiếm, lọc và phân trang.
     *
     * @param filter điều kiện tìm kiếm, lọc và sắp xếp
     * @param page số trang cần lấy, bắt đầu từ 1
     * @param size số sản phẩm tối đa trong một trang
     * @return danh sách sản phẩm kèm thông tin phân trang
     */
    fun getProductsPage(
        filter: ProductSearchFilter = ProductSearchFilter(),
        page: Int = 1,
        size: Int = 10
    ): ProductPageResponse {
        val result = productRepository.findPage(
            filter = filter,
            page = page,
            size = size
        )

        return ProductPageResponse(
            items = result.items.map { it.toProductResponse() },
            page = result.page,
            size = result.size,
            totalItems = result.totalItems,
            totalPages = result.totalPages
        )
    }

    /**
     * Lấy thông tin chi tiết sản phẩm và tồn kho tại các kho.
     *
     * @param productId mã sản phẩm
     * @return thông tin chi tiết sản phẩm
     */
    fun getProductDetail(productId: Long): ProductDetailResponse {
        val product = productRepository.findById(productId)
            ?: throw ProductNotFoundException()

        val inventories = productRepository.findInventories(productId)

        return ProductDetailResponse(
            product = product.toProductResponse(),
            inventories = inventories
        )
    }

    /**
     * Tạo sản phẩm mới.
     *
     * SKU và barcode được tự động sinh nếu người dùng không cung cấp.
     *
     * @param request dữ liệu sản phẩm cần tạo
     * @return thông tin sản phẩm vừa tạo
     */
    fun createProduct(request: CreateProductRequest): ProductResponse {
        validateProductData(
            productName = request.productName,
            unit = request.unit,
            costPrice = request.costPrice,
            salePrice = request.salePrice,
            minStock = request.minStock,
            maxStock = request.maxStock
        )

        validateCategory(request.categoryId)
        validateSupplier(request.supplierId)

        val sku = request.sku
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: generateUniqueSku()

        val barcode = request.barcode
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: generateUniqueBarcode()

        if (productRepository.existsSku(sku)) {
            throw DuplicateSkuException()
        }

        if (productRepository.existsBarcode(barcode)) {
            throw DuplicateBarcodeException()
        }

        val now = LocalDateTime.now()

        val product = Product(
            productId = 0,
            sku = sku,
            barcode = barcode,
            productName = request.productName.trim(),
            imageUrl = request.imageUrl,
            categoryId = request.categoryId,
            supplierId = request.supplierId,
            unit = request.unit.trim(),
            costPrice = request.costPrice,
            salePrice = request.salePrice,
            minStock = request.minStock,
            maxStock = request.maxStock,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        // Repository trả về ID của sản phẩm vừa tạo.
        val createdProductId = productRepository.create(product)

        // Lấy lại dữ liệu vừa lưu để bảo đảm có ID và timestamp từ database.
        val createdProduct = productRepository.findById(createdProductId)
            ?: throw ProductNotFoundException()

        return createdProduct.toProductResponse()
    }

    /**
     * Cập nhật thông tin sản phẩm.
     *
     * @param productId mã sản phẩm cần cập nhật
     * @param request dữ liệu mới
     * @return thông tin sản phẩm sau khi cập nhật
     */
    fun updateProduct(
        productId: Long,
        request: UpdateProductRequest
    ): ProductResponse {
        val existingProduct = productRepository.findById(productId)
            ?: throw ProductNotFoundException()

        validateProductData(
            productName = request.productName,
            unit = request.unit,
            costPrice = request.costPrice,
            salePrice = request.salePrice,
            minStock = request.minStock,
            maxStock = request.maxStock
        )

        validateCategory(request.categoryId)
        validateSupplier(request.supplierId)

        val sku = request.sku.trim()

        if (sku.isEmpty()) {
            throw InvalidProductException("SKU không được để trống.")
        }

        if (productRepository.existsSku(sku, productId)) {
            throw DuplicateSkuException()
        }

        val barcode = request.barcode
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (barcode != null &&
            productRepository.existsBarcode(barcode, productId)
        ) {
            throw DuplicateBarcodeException()
        }

        val updatedProduct = existingProduct.copy(
            sku = sku,
            barcode = barcode,
            productName = request.productName.trim(),
            imageUrl = request.imageUrl,
            categoryId = request.categoryId,
            supplierId = request.supplierId,
            unit = request.unit.trim(),
            costPrice = request.costPrice,
            salePrice = request.salePrice,
            minStock = request.minStock,
            maxStock = request.maxStock,
            updatedAt = LocalDateTime.now()
        )

        val success = productRepository.update(updatedProduct)

        if (!success) {
            throw InvalidProductException("Không thể cập nhật sản phẩm.")
        }

        val result = productRepository.findById(productId)
            ?: throw ProductNotFoundException()

        return result.toProductResponse()
    }

    /**
     * Ngừng kinh doanh sản phẩm bằng cách chuyển trạng thái hoạt động.
     *
     * @param productId mã sản phẩm
     */
    fun deactivateProduct(productId: Long) {
        val product = productRepository.findById(productId)
            ?: throw ProductNotFoundException()

        if (!product.isActive) {
            throw InvalidProductException("Sản phẩm đã ngừng kinh doanh.")
        }

        val success = productRepository.deactivate(productId)

        if (!success) {
            throw InvalidProductException(
                "Không thể ngừng kinh doanh sản phẩm."
            )
        }
    }

    /**
     * Kích hoạt lại sản phẩm đã ngừng kinh doanh.
     *
     * @param productId mã sản phẩm
     */
    fun reactivateProduct(productId: Long) {
        val product = productRepository.findById(productId)
            ?: throw ProductNotFoundException()

        if (product.isActive) {
            throw InvalidProductException("Sản phẩm đang hoạt động.")
        }

        validateCategory(product.categoryId)
        validateSupplier(product.supplierId)

        val success = productRepository.reactivate(productId)

        if (!success) {
            throw InvalidProductException(
                "Không thể kích hoạt lại sản phẩm."
            )
        }
    }

    /**
     * Tra cứu sản phẩm bằng SKU hoặc barcode.
     *
     * @param code mã SKU hoặc barcode
     * @return thông tin sản phẩm
     */
    fun lookupProduct(code: String): ProductResponse {
        val normalizedCode = code.trim()

        if (normalizedCode.isEmpty()) {
            throw InvalidProductException(
                "Vui lòng nhập SKU hoặc mã vạch."
            )
        }

        val product = productRepository.findBySku(normalizedCode)
            ?: productRepository.findByBarcode(normalizedCode)
            ?: throw ProductNotFoundException()

        if (!product.isActive) {
            throw InvalidProductException(
                "Sản phẩm đã ngừng kinh doanh."
            )
        }

        return product.toProductResponse()
    }

    /**
     * Kiểm tra dữ liệu cơ bản của sản phẩm.
     */
    private fun validateProductData(
        productName: String,
        unit: String,
        costPrice: BigDecimal,
        salePrice: BigDecimal,
        minStock: Int,
        maxStock: Int
    ) {
        if (productName.isBlank()) {
            throw InvalidProductException(
                "Tên sản phẩm không được để trống."
            )
        }

        if (unit.isBlank()) {
            throw InvalidProductException(
                "Đơn vị tính không được để trống."
            )
        }

        if (costPrice < BigDecimal.ZERO) {
            throw InvalidProductException(
                "Giá vốn không được nhỏ hơn 0."
            )
        }

        if (salePrice < BigDecimal.ZERO) {
            throw InvalidProductException(
                "Giá bán không được nhỏ hơn 0."
            )
        }

        if (minStock < 0 || maxStock < 0) {
            throw InvalidProductException(
                "Số lượng tồn kho không được nhỏ hơn 0."
            )
        }

        if (maxStock < minStock) {
            throw InvalidProductException(
                "Tồn kho tối đa phải lớn hơn hoặc bằng tồn kho tối thiểu."
            )
        }
    }

    /**
     * Kiểm tra danh mục tồn tại và đang hoạt động.
     */
    private fun validateCategory(categoryId: Long?) {
        if (categoryId == null) return

        if (!categoryRepository.existsActiveById(categoryId)) {
            throw InvalidProductCategoryException()
        }
    }

    /**
     * Kiểm tra nhà cung cấp tồn tại và đang hoạt động.
     */
    private fun validateSupplier(supplierId: Long?) {
        if (supplierId == null) return

        if (!supplierRepository.existsActiveById(supplierId)) {
            throw InvalidProductSupplierException()
        }
    }

    /**
     * Sinh SKU duy nhất theo định dạng SPxxxxxx.
     */
    private fun generateUniqueSku(): String {
        repeat(100) {
            val number = random.nextInt(1_000_000)
            val candidate = "SP%06d".format(number)

            if (!productRepository.existsSku(candidate)) {
                return candidate
            }
        }

        throw ProductCodeGenerationException(
            "Không thể tạo SKU duy nhất."
        )
    }

    /**
     * Sinh mã vạch EAN-13 với tiền tố nội bộ 200.
     */
    private fun generateUniqueBarcode(): String {
        repeat(100) {
            val prefixAndNumber = "200" +
                    "%09d".format(random.nextInt(1_000_000_000))

            val candidate = prefixAndNumber +
                    calculateEan13CheckDigit(prefixAndNumber)

            if (!productRepository.existsBarcode(candidate)) {
                return candidate
            }
        }

        throw ProductCodeGenerationException(
            "Không thể tạo mã vạch duy nhất."
        )
    }

    /**
     * Tính chữ số kiểm tra của mã EAN-13.
     *
     * @param value 12 chữ số đầu của mã vạch
     * @return chữ số kiểm tra
     */
    private fun calculateEan13CheckDigit(value: String): Int {
        if (value.length != 12 || !value.all { it.isDigit() }) {
            throw ProductCodeGenerationException(
                "Dữ liệu tạo mã vạch không hợp lệ."
            )
        }

        val sum = value.mapIndexed { index, char ->
            val digit = char.digitToInt()

            if (index % 2 == 0) {
                digit
            } else {
                digit * 3
            }
        }.sum()

        return (10 - sum % 10) % 10
    }

    /**
     * Chuyển entity sản phẩm thành dữ liệu phản hồi.
     *
     * @return thông tin sản phẩm dạng DTO
     */
    private fun Product.toProductResponse(): ProductResponse {
        return ProductResponse(
            productId = productId,
            sku = sku,
            barcode = barcode,
            productName = productName,
            imageUrl = imageUrl,
            categoryId = categoryId,
            supplierId = supplierId,
            unit = unit,
            costPrice = costPrice,
            salePrice = salePrice,
            minStock = minStock,
            maxStock = maxStock,
            isActive = isActive,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString()
        )
    }
}
