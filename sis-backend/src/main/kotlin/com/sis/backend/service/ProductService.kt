package com.sis.backend.service

import com.sis.backend.dto.ProductRequest
import com.sis.backend.entity.Product
import com.sis.backend.exception.DuplicateBarcodeException
import com.sis.backend.exception.DuplicateSkuException
import com.sis.backend.exception.InvalidProductCategoryException
import com.sis.backend.exception.InvalidProductException
import com.sis.backend.exception.ProductNotFoundException
import com.sis.backend.repository.ProductRepository
import java.util.UUID

/**
 * Xử lý nghiệp vụ quản lý sản phẩm.
 *
 * @property repository Repository dùng để truy xuất sản phẩm.
 */
class ProductService(
    private val repository: ProductRepository
) {

    /**
     * Lấy danh sách sản phẩm.
     *
     * @return Danh sách sản phẩm.
     */
    fun getAll(): List<Product> = repository.findAll()

    /**
     * Lấy chi tiết sản phẩm theo ID.
     *
     * @param id ID sản phẩm.
     * @return Sản phẩm được tìm thấy.
     * @throws ProductNotFoundException nếu sản phẩm không tồn tại.
     */
    fun getById(id: Long): Product =
        repository.findById(id) ?: throw ProductNotFoundException()

    /**
     * Tạo sản phẩm mới sau khi kiểm tra dữ liệu.
     *
     * Nếu Barcode không được cung cấp, hệ thống sẽ tự sinh Barcode.
     *
     * @param request Dữ liệu sản phẩm.
     * @param createdBy ID người tạo, nếu có.
     * @return Sản phẩm vừa được tạo.
     */
    fun create( request: ProductRequest, createdBy: Long? = null): Product {
        var normalized = validateAndNormalize(request)

        if (normalized.barcode == null) {
            var barcode: String

            do {
                barcode = generateBarcode()
            } while (repository.existsByBarcode(barcode))

            normalized = normalized.copy(barcode = barcode)
        }

        validateUniqueFields(normalized)
        validateCategory(normalized.categoryId)

        return repository.create(normalized, createdBy)
    }

    /**
     * Sinh mã Barcode gồm 13 chữ số để định danh sản phẩm trong hệ thống.
     *
     * Mã được tạo dựa trên UUID và không đại diện cho mã GS1
     * được cấp chính thức.
     *
     * @return Chuỗi Barcode gồm 13 chữ số.
     */
    private fun generateBarcode(): String {
        val digits = UUID.randomUUID()
            .toString()
            .filter { it.isDigit() }

        return digits.padEnd(13, '0').take(13)
    }

    /**
     * Cập nhật sản phẩm hiện có.
     *
     * @param id ID sản phẩm.
     * @param request Dữ liệu cần cập nhật.
     * @return Sản phẩm sau khi cập nhật.
     * @throws ProductNotFoundException nếu sản phẩm không tồn tại.
     */
    fun update(id: Long, request: ProductRequest): Product {
        requireValidId(id)
        getById(id)

        val normalized = validateAndNormalize(request)
        validateUniqueFields(normalized, id)
        validateCategory(normalized.categoryId)

        return repository.update(id, normalized)
            ?: throw ProductNotFoundException()
    }

    /**
     * Xóa sản phẩm.
     *
     * Việc xác thực quyền Admin được thực hiện ở tầng Routes.
     *
     * @param id ID sản phẩm cần xóa.
     * @throws ProductNotFoundException nếu sản phẩm không tồn tại.
     */
    fun delete(id: Long) {
        requireValidId(id)

        if (!repository.delete(id)) {
            throw ProductNotFoundException()
        }
    }

    /**
     * Kiểm tra và chuẩn hóa dữ liệu đầu vào.
     *
     * @param request Dữ liệu gốc.
     * @return Request đã chuẩn hóa.
     */
    private fun validateAndNormalize(
        request: ProductRequest
    ): ProductRequest {
        val sku = request.sku.trim()
        val barcode = request.barcode?.trim()?.ifEmpty { null }
        val productName = request.productName.trim()
        val description = request.description?.trim()?.ifEmpty { null }
        val unit = request.unit.trim()
        val status = request.status.trim().uppercase()

        if (sku.isEmpty() || sku.length > 80) {
            throw InvalidProductException("SKU không được để trống và tối đa 80 ký tự")
        }

        if (barcode != null && barcode.length > 100) {
            throw InvalidProductException("Barcode tối đa 100 ký tự")
        }

        if (productName.isEmpty() || productName.length > 200) {
            throw InvalidProductException(
                "Tên sản phẩm không được để trống và tối đa 200 ký tự"
            )
        }

        if (unit.isEmpty() || unit.length > 40) {
            throw InvalidProductException(
                "Đơn vị tính không được để trống và tối đa 40 ký tự"
            )
        }

        if (!request.minStock.isFinite() || request.minStock < 0) {
            throw InvalidProductException(
                "Tồn kho tối thiểu phải là số hữu hạn không âm"
            )
        }

        if (request.categoryId != null && request.categoryId <= 0) {
            throw InvalidProductException("ID danh mục không hợp lệ")
        }

        if (status !in setOf("ACTIVE", "INACTIVE", "DISCONTINUED")) {
            throw InvalidProductException("Trạng thái sản phẩm không hợp lệ")
        }

        return request.copy(
            sku = sku,
            barcode = barcode,
            productName = productName,
            description = description,
            unit = unit,
            status = status
        )
    }

    /**
     * Kiểm tra tính duy nhất của SKU và Barcode.
     *
     * @param request Request đã chuẩn hóa.
     * @param excludeId ID sản phẩm cần loại trừ khi cập nhật.
     */
    private fun validateUniqueFields(
        request: ProductRequest,
        excludeId: Long? = null
    ) {
        if (repository.existsBySku(request.sku, excludeId)) {
            throw DuplicateSkuException()
        }

        if (repository.existsByBarcode(request.barcode, excludeId)) {
            throw DuplicateBarcodeException()
        }
    }

    /**
     * Kiểm tra danh mục được chọn.
     *
     * @param categoryId ID danh mục, hoặc null nếu không chọn danh mục.
     */
    private fun validateCategory(categoryId: Long?) {
        if (categoryId != null && !repository.isActiveCategory(categoryId)) {
            throw InvalidProductCategoryException()
        }
    }

    /**
     * Kiểm tra ID phải là số dương.
     *
     * @param id ID cần kiểm tra.
     */
    private fun requireValidId(id: Long) {
        if (id <= 0) {
            throw InvalidProductException("ID sản phẩm không hợp lệ")
        }
    }

    /**
     * Tìm kiếm sản phẩm theo tên hoặc SKU.
     *
     * @param keyword Từ khóa tìm kiếm; có thể là chuỗi rỗng.
     * @return Danh sách sản phẩm phù hợp.
     */
    fun searchProducts(keyword: String): List<Product> {
        return repository.search(keyword)
    }

    /**
     * Tra cứu sản phẩm bằng Barcode.
     *
     * @param barcode Barcode cần tra cứu.
     * @return Sản phẩm tương ứng.
     * @throws InvalidProductException Nếu Barcode rỗng.
     * @throws ProductNotFoundException Nếu không tìm thấy sản phẩm.
     */
    fun getProductByBarcode(barcode: String): Product {
        val normalizedBarcode = barcode.trim()

        if (normalizedBarcode.isEmpty()) {
            throw InvalidProductException("Barcode không được để trống.")
        }

        return repository.findByBarcode(normalizedBarcode)
            ?: throw ProductNotFoundException()
    }
}