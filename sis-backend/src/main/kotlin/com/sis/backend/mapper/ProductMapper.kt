package com.sis.backend.mapper

import com.sis.backend.dto.product.ProductDetailResponse
import com.sis.backend.dto.product.ProductInventoryResponse
import com.sis.backend.dto.product.ProductResponse
import com.sis.backend.entity.Product

/**
 * Chuyển đổi dữ liệu sản phẩm giữa Entity và DTO.
 */
object ProductMapper {

    /**
     * Chuyển thông tin sản phẩm sang DTO phản hồi.
     *
     * @param product Sản phẩm cần chuyển đổi.
     * @return Thông tin sản phẩm dùng cho API.
     */
    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            productId = product.productId,
            sku = product.sku,
            barcode = product.barcode,
            productName = product.productName,
            imageUrl = product.imageUrl,
            categoryId = product.categoryId,
            supplierId = product.supplierId,
            unit = product.unit,
            costPrice = product.costPrice,
            salePrice = product.salePrice,
            minStock = product.minStock,
            maxStock = product.maxStock,
            isActive = product.isActive,
            createdAt = product.createdAt.toString(),
            updatedAt = product.updatedAt.toString()
        )
    }

    /**
     * Chuyển danh sách Entity sang danh sách DTO.
     *
     * @param products Danh sách sản phẩm.
     * @return Danh sách sản phẩm dùng cho API.
     */
    fun toResponseList(products: List<Product>): List<ProductResponse> {
        return products.map(::toResponse)
    }

    /**
     * Tạo phản hồi chi tiết sản phẩm kèm thông tin tồn kho.
     *
     * @param product Sản phẩm cần chuyển đổi.
     * @param inventories Danh sách tồn kho của sản phẩm.
     * @return Thông tin chi tiết sản phẩm.
     */
    fun toDetailResponse(
        product: Product,
        inventories: List<ProductInventoryResponse>
    ): ProductDetailResponse {
        return ProductDetailResponse(
            product = toResponse(product),
            inventories = inventories
        )
    }
}
