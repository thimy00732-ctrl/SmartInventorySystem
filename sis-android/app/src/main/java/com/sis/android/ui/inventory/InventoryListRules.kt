package com.sis.android.ui.inventory

import com.sis.android.data.model.InventoryItem
import com.sis.android.data.model.InventorySortBy
import com.sis.android.data.model.StockStatusFilter

internal object InventoryListRules {
    fun filterAndSort(
        items: List<InventoryItem>,
        keyword: String,
        warehouseName: String,
        categoryName: String,
        status: StockStatusFilter,
        sortBy: InventorySortBy,
        ascending: Boolean
    ): List<InventoryItem> {
        val filtered = items.filter { item ->
            val matchesKeyword = keyword.isBlank() ||
                item.sku.contains(keyword, ignoreCase = true) ||
                item.barcode?.contains(keyword, ignoreCase = true) == true ||
                item.productName.contains(keyword, ignoreCase = true)
            val matchesWarehouse = warehouseName == "Tất cả kho" || item.warehouseName == warehouseName
            val matchesCategory = categoryName == "Tất cả danh mục" || item.categoryName == categoryName
            val matchesStatus = when (status) {
                StockStatusFilter.ALL -> true
                StockStatusFilter.IN_STOCK -> item.stockQuantity > 0
                StockStatusFilter.OUT -> item.stockQuantity == 0
                StockStatusFilter.RESERVED -> item.reservedQuantity > 0
            }
            matchesKeyword && matchesWarehouse && matchesCategory && matchesStatus
        }

        val sorted = when (sortBy) {
            InventorySortBy.PRODUCT_NAME -> filtered.sortedBy { it.productName.lowercase() }
            InventorySortBy.STOCK_QUANTITY -> filtered.sortedBy { it.stockQuantity }
            InventorySortBy.EXPIRY_DATE -> filtered.sortedBy { it.expiryDate ?: "9999-12-31" }
        }
        return if (ascending) sorted else sorted.reversed()
    }
}
