package com.sis.backend.controller

import com.sis.backend.dto.product.CreateProductRequest
import com.sis.backend.dto.product.UpdateProductRequest
import com.sis.backend.repository.ProductSearchFilter
import com.sis.backend.service.ProductService

/**
 * Tiếp nhận và xử lý các yêu cầu HTTP liên quan đến sản phẩm.
 *
 * @property productService Service xử lý nghiệp vụ sản phẩm.
 */
class ProductController(
    private val productService: ProductService
) {

    /**
     * Lấy danh sách sản phẩm.
     *
     * @param keyword Từ khóa tìm kiếm.
     * @param includeInactive Có lấy sản phẩm ngừng hoạt động hay không.
     * @return Danh sách sản phẩm.
     */
    fun getAllProducts(
        keyword: String?,
        includeInactive: Boolean
    ) = productService.getAllProducts(keyword, includeInactive)

    /**
     * Lấy danh sách sản phẩm có tìm kiếm, lọc và phân trang.
     *
     * @param filter Điều kiện tìm kiếm và lọc.
     * @param page Số trang cần lấy.
     * @param size Số sản phẩm mỗi trang.
     * @return Danh sách sản phẩm kèm thông tin phân trang.
     */
    fun getProductsPage(
        filter: ProductSearchFilter,
        page: Int,
        size: Int
    ) = productService.getProductsPage(filter, page, size)

    /**
     * Lấy thông tin chi tiết sản phẩm.
     *
     * @param productId Mã sản phẩm.
     * @return Chi tiết sản phẩm kèm tồn kho.
     */
    fun getProductDetail(productId: Long) =
        productService.getProductDetail(productId)

    /**
     * Tạo sản phẩm mới.
     *
     * @param request Dữ liệu tạo sản phẩm.
     * @return Thông tin sản phẩm vừa tạo.
     */
    fun createProduct(request: CreateProductRequest) =
        productService.createProduct(request)

    /**
     * Cập nhật thông tin sản phẩm.
     *
     * @param productId Mã sản phẩm.
     * @param request Dữ liệu cập nhật.
     * @return Thông tin sản phẩm sau cập nhật.
     */
    fun updateProduct(
        productId: Long,
        request: UpdateProductRequest
    ) = productService.updateProduct(productId, request)

    /**
     * Ngừng kinh doanh sản phẩm.
     *
     * @param productId Mã sản phẩm.
     */
    fun deactivateProduct(productId: Long) =
        productService.deactivateProduct(productId)

    /**
     * Kích hoạt lại sản phẩm.
     *
     * @param productId Mã sản phẩm.
     */
    fun reactivateProduct(productId: Long) =
        productService.reactivateProduct(productId)

    /**
     * Tra cứu sản phẩm bằng SKU hoặc barcode.
     *
     * @param code Mã cần tra cứu.
     * @return Thông tin sản phẩm.
     */
    fun lookupProduct(code: String) =
        productService.lookupProduct(code)
}
