package com.sis.backend.controller

import com.sis.backend.dto.ProductRequest
import com.sis.backend.entity.Product
import com.sis.backend.service.ProductService

/**
 * Tiếp nhận yêu cầu quản lý sản phẩm và chuyển tiếp đến Service.
 *
 * @property service Service xử lý nghiệp vụ sản phẩm.
 */
class ProductController(
    private val service: ProductService
) {

    /**
     * Lấy danh sách sản phẩm.
     *
     * @return Danh sách sản phẩm.
     */
    fun getAll(): List<Product> = service.getAll()

    /**
     * Lấy sản phẩm theo ID.
     *
     * @param id ID sản phẩm.
     * @return Sản phẩm tương ứng.
     */
    fun getById(id: Long): Product = service.getById(id)

    /**
     * Tạo sản phẩm.
     *
     * @param request Dữ liệu sản phẩm.
     * @param createdBy ID người tạo, nếu có.
     * @return Sản phẩm vừa tạo.
     */
    fun create(request: ProductRequest, createdBy: Long? = null): Product =
        service.create(request, createdBy)

    /**
     * Cập nhật sản phẩm.
     *
     * @param id ID sản phẩm.
     * @param request Dữ liệu mới.
     * @return Sản phẩm sau cập nhật.
     */
    fun update(id: Long, request: ProductRequest): Product =
        service.update(id, request)

    /**
     * Xóa sản phẩm.
     *
     * @param id ID sản phẩm.
     */
    fun delete(id: Long) = service.delete(id)

    /**
     * Tìm kiếm sản phẩm theo tên hoặc SKU.
     *
     * @param keyword Từ khóa tìm kiếm.
     * @return Danh sách sản phẩm phù hợp.
     */
    fun searchProducts(keyword: String): List<Product> {
        return service.searchProducts(keyword)
    }

    /**
     * Tra cứu sản phẩm theo Barcode.
     *
     * @param barcode Barcode cần tra cứu.
     * @return Sản phẩm tìm được.
     * @throws InvalidProductException Nếu Barcode rỗng.
     * @throws ProductNotFoundException Nếu Barcode không tồn tại.
     */
    fun getProductByBarcode(barcode: String): Product {
        return service.getProductByBarcode(barcode)
    }
}