package com.sis.backend.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Điều kiện tìm kiếm và lọc sản phẩm.
 *
 * @property keyword Từ khóa tìm kiếm theo SKU, barcode hoặc tên sản phẩm.
 * @property categoryId Mã danh mục cần lọc.
 * @property categoryName Tên danh mục cần lọc (tìm kiếm gần đúng).
 * @property supplierId Mã nhà cung cấp cần lọc.
 * @property supplierName Tên nhà cung cấp cần lọc (tìm kiếm gần đúng).
 * @property isActive Trạng thái hoạt động; null là lấy cả hai trạng thái.
 * @property minPrice Giá bán thấp nhất.
 * @property maxPrice Giá bán cao nhất.
 * @property sortBy Cột dùng để sắp xếp.
 * @property sortDirection Chiều sắp xếp ASC hoặc DESC.
 */
data class ProductSearchFilter(
    val keyword: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val isActive: Boolean? = true,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val sortBy: String = "created_at",
    val sortDirection: String = "DESC"
)

/**
 * Điều kiện tìm kiếm và lọc tồn kho.
 *
 * @property keyword Từ khóa tìm kiếm theo SKU, barcode hoặc tên sản phẩm.
 * @property warehouseId Mã kho cần lọc (null = tất cả kho).
 * @property warehouseName Tên kho cần lọc (tìm kiếm gần đúng).
 * @property categoryId Mã danh mục sản phẩm cần lọc.
 * @property categoryName Tên danh mục sản phẩm cần lọc (tìm kiếm gần đúng).
 * @property supplierId Mã nhà cung cấp cần lọc.
 * @property supplierName Tên nhà cung cấp cần lọc (tìm kiếm gần đúng).
 * @property stockStatus Trạng thái tồn kho: ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED.
 * @property minQuantity Số lượng tồn tối thiểu.
 * @property maxQuantity Số lượng tồn tối đa.
 * @property expiryDateFrom Lọc hàng hết hạn từ ngày (YYYY-MM-DD).
 * @property expiryDateTo Lọc hàng hết hạn đến ngày (YYYY-MM-DD).
 * @property sortBy Cột dùng để sắp xếp.
 * @property sortDirection Chiều sắp xếp ASC hoặc DESC.
 */
data class InventorySearchFilter(
    val keyword: String? = null,
    val warehouseId: Long? = null,
    val warehouseName: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val stockStatus: StockStatus = StockStatus.ALL,
    val minQuantity: Int? = null,
    val maxQuantity: Int? = null,
    val expiryDateFrom: String? = null,
    val expiryDateTo: String? = null,
    val sortBy: String = "stock_quantity",
    val sortDirection: String = "DESC"
)

/**
 * Trạng thái tồn kho cho filtering.
 */
enum class StockStatus {
    ALL,           // Tất cả
    IN_STOCK,      // Còn hàng (stock_quantity > 0)
    OUT_OF_STOCK,  // Hết hàng (stock_quantity = 0)
    HAS_RESERVED   // Có giữ chỗ (reserved_quantity > 0)
}

/**
 * Kết quả phân trang chung.
 *
 * @param T Kiểu dữ liệu của items trong trang.
 * @property items Danh sách items của trang hiện tại.
 * @property page Trang hiện tại, bắt đầu từ 1.
 * @property size Số items tối đa trên một trang.
 * @property totalItems Tổng số items thỏa mãn điều kiện.
 * @property totalPages Tổng số trang.
 */
@Serializable
data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)
