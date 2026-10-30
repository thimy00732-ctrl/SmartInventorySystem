package com.sis.backend.dto.product

import com.sis.backend.serializer.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.Serializable

/**
 * Dữ liệu yêu cầu tạo sản phẩm mới.
 *
 * SKU và barcode không bắt buộc nhập.
 * Backend sẽ tự sinh mã nếu người dùng để trống.
 */
@Serializable
data class CreateProductRequest(
    val sku: String? = null,
    val barcode: String? = null,
    val productName: String,
    val imageUrl: String? = null,
    val categoryId: Long? = null,
    val supplierId: Long? = null,

    val unit: String,

    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal = BigDecimal.ZERO,

    @Serializable(with = BigDecimalSerializer::class)
    val salePrice: BigDecimal = BigDecimal.ZERO,

    val minStock: Int = 0,
    val maxStock: Int = 1000
)

/**
 * Dữ liệu yêu cầu cập nhật sản phẩm.
 *
 * SKU phải được cung cấp khi cập nhật.
 * Barcode có thể để null nếu sản phẩm không sử dụng mã vạch.
 */
@Serializable
data class UpdateProductRequest(
    val sku: String,
    val barcode: String? = null,
    val productName: String,
    val imageUrl: String? = null,
    val categoryId: Long? = null,
    val supplierId: Long? = null,
    val unit: String,

    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    val salePrice: BigDecimal,

    val minStock: Int,
    val maxStock: Int
)

/**
 * Thông tin sản phẩm trả về cho danh sách và kết quả tìm kiếm.
 */
@Serializable
data class ProductResponse(
    val productId: Long,
    val sku: String,
    val barcode: String?,
    val productName: String,
    val imageUrl: String?,
    val categoryId: Long?,
    val supplierId: Long?,
    val unit: String,

    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    val salePrice: BigDecimal,

    val minStock: Int,
    val maxStock: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Thông tin tồn kho của sản phẩm tại một kho cụ thể.
 */
@Serializable
data class ProductInventoryResponse(
    val inventoryId: Long,
    val warehouseId: Long,
    val warehouseName: String,
    val batchCode: String,
    val manufactureDate: String?,
    val expiryDate: String?,
    val stockQuantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val updatedAt: String
)

/**
 * Thông tin chi tiết sản phẩm kèm danh sách tồn kho.
 */
@Serializable
data class ProductDetailResponse(
    val product: ProductResponse,
    val inventories: List<ProductInventoryResponse>
)
/**
 * Thông tin danh sách sản phẩm có phân trang.
 *
 * @property items danh sách sản phẩm trong trang hiện tại
 * @property page số trang hiện tại
 * @property size số sản phẩm tối đa trong một trang
 * @property totalItems tổng số sản phẩm thỏa mãn điều kiện
 * @property totalPages tổng số trang
 */
@Serializable
data class ProductPageResponse(
    val items: List<ProductResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)