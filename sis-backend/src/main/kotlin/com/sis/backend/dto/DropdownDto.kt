package com.sis.backend.dto

import kotlinx.serialization.Serializable

/**
 * Option cho dropdown danh mục sản phẩm.
 *
 * @property categoryId ID danh mục.
 * @property categoryName Tên danh mục.
 * @property description Mô tả danh mục (optional).
 */
@Serializable
data class CategoryOption(
    val categoryId: Long,
    val categoryName: String,
    val description: String? = null
)

/**
 * Option cho dropdown nhà cung cấp.
 *
 * @property supplierId ID nhà cung cấp.
 * @property supplierName Tên nhà cung cấp.
 * @property phone Số điện thoại (optional).
 * @property email Email (optional).
 */
@Serializable
data class SupplierOption(
    val supplierId: Long,
    val supplierName: String,
    val phone: String? = null,
    val email: String? = null
)

/**
 * Option cho dropdown kho hàng.
 *
 * @property warehouseId ID kho.
 * @property warehouseName Tên kho.
 * @property location Địa điểm (optional).
 */
@Serializable
data class WarehouseOption(
    val warehouseId: Long,
    val warehouseName: String,
    val location: String? = null
)

/**
 * Option cho dropdown trạng thái tồn kho.
 *
 * @property value Giá trị enum (ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED).
 * @property label Nhãn hiển thị tiếng Việt.
 */
@Serializable
data class StockStatusOption(
    val value: String,
    val label: String
) {
    companion object {
        /**
         * Danh sách tất cả các option trạng thái tồn kho.
         */
        fun all(): List<StockStatusOption> = listOf(
            StockStatusOption("ALL", "Tất cả"),
            StockStatusOption("IN_STOCK", "Còn hàng"),
            StockStatusOption("OUT_OF_STOCK", "Hết hàng"),
            StockStatusOption("HAS_RESERVED", "Có giữ chỗ")
        )
    }
}

/**
 * Option cho dropdown cột sắp xếp sản phẩm.
 *
 * @property value Tên cột trong database.
 * @property label Nhãn hiển thị.
 */
@Serializable
data class ProductSortOption(
    val value: String,
    val label: String
) {
    companion object {
        /**
         * Danh sách tất cả các option sắp xếp sản phẩm.
         */
        fun all(): List<ProductSortOption> = listOf(
            ProductSortOption("product_id", "Mã sản phẩm"),
            ProductSortOption("sku", "Mã SKU"),
            ProductSortOption("barcode", "Mã vạch"),
            ProductSortOption("product_name", "Tên sản phẩm"),
            ProductSortOption("cost_price", "Giá nhập"),
            ProductSortOption("sale_price", "Giá bán"),
            ProductSortOption("created_at", "Ngày tạo"),
            ProductSortOption("updated_at", "Ngày cập nhật")
        )
    }
}

/**
 * Option cho dropdown cột sắp xếp tồn kho.
 *
 * @property value Tên cột trong database.
 * @property label Nhãn hiển thị.
 */
@Serializable
data class InventorySortOption(
    val value: String,
    val label: String
) {
    companion object {
        /**
         * Danh sách tất cả các option sắp xếp tồn kho.
         */
        fun all(): List<InventorySortOption> = listOf(
            InventorySortOption("stock_quantity", "Số lượng tồn"),
            InventorySortOption("reserved_quantity", "Số lượng giữ chỗ"),
            InventorySortOption("available_quantity", "Số lượng khả dụng"),
            InventorySortOption("product_name", "Tên sản phẩm"),
            InventorySortOption("expiry_date", "Ngày hết hạn"),
            InventorySortOption("updated_at", "Ngày cập nhật")
        )
    }
}

/**
 * Option cho dropdown chiều sắp xếp.
 *
 * @property value ASC hoặc DESC.
 * @property label Nhãn hiển thị.
 */
@Serializable
data class SortDirectionOption(
    val value: String,
    val label: String
) {
    companion object {
        /**
         * Danh sách các option chiều sắp xếp.
         */
        fun all(): List<SortDirectionOption> = listOf(
            SortDirectionOption("ASC", "Tăng dần (A→Z, 1→9)"),
            SortDirectionOption("DESC", "Giảm dần (Z→A, 9→1)")
        )
    }
}

/**
 * Response tổng hợp tất cả dropdown options cho Product filter.
 */
@Serializable
data class ProductFilterOptions(
    val categories: List<CategoryOption>,
    val suppliers: List<SupplierOption>,
    val sortByOptions: List<ProductSortOption>,
    val sortDirectionOptions: List<SortDirectionOption>
)

/**
 * Response tổng hợp tất cả dropdown options cho Inventory filter.
 */
@Serializable
data class InventoryFilterOptions(
    val warehouses: List<WarehouseOption>,
    val categories: List<CategoryOption>,
    val suppliers: List<SupplierOption>,
    val stockStatusOptions: List<StockStatusOption>,
    val sortByOptions: List<InventorySortOption>,
    val sortDirectionOptions: List<SortDirectionOption>
)
