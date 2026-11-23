package com.sis.android.ui.inventory

import com.sis.android.data.model.InventoryItem
import com.sis.android.data.model.InventorySortBy
import com.sis.android.data.model.StockStatusFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryListRulesTest {
    private val items = listOf(
        InventoryItem(
            inventoryId = 1,
            sku = "SP-01",
            productName = "Sữa",
            barcode = "8930001",
            categoryName = "Thực phẩm",
            warehouseName = "Kho A",
            stockQuantity = 8,
            reservedQuantity = 2,
            expiryDate = "2026-12-31"
        ),
        InventoryItem(
            inventoryId = 2,
            sku = "SP-02",
            productName = "Bột giặt",
            barcode = "8930002",
            categoryName = "Gia dụng",
            warehouseName = "Kho B",
            stockQuantity = 0,
            expiryDate = null
        )
    )

    @Test
    fun `search matches barcode`() {
        val result = InventoryListRules.filterAndSort(
            items = items,
            keyword = "8930001",
            warehouseName = "Tất cả kho",
            categoryName = "Tất cả danh mục",
            status = StockStatusFilter.ALL,
            sortBy = InventorySortBy.PRODUCT_NAME,
            ascending = true
        )

        assertEquals(listOf(1L), result.map { it.inventoryId })
    }

    @Test
    fun `filters by selected category`() {
        val result = InventoryListRules.filterAndSort(
            items = items,
            keyword = "",
            warehouseName = "Tất cả kho",
            categoryName = "Gia dụng",
            status = StockStatusFilter.ALL,
            sortBy = InventorySortBy.PRODUCT_NAME,
            ascending = true
        )

        assertEquals(listOf(2L), result.map { it.inventoryId })
    }

    @Test
    fun `sorts product name in both directions`() {
        val ascending = InventoryListRules.filterAndSort(
            items, "", "Tất cả kho", "Tất cả danh mục",
            StockStatusFilter.ALL, InventorySortBy.PRODUCT_NAME, true
        )
        val descending = InventoryListRules.filterAndSort(
            items, "", "Tất cả kho", "Tất cả danh mục",
            StockStatusFilter.ALL, InventorySortBy.PRODUCT_NAME, false
        )

        assertEquals(listOf(2L, 1L), ascending.map { it.inventoryId })
        assertEquals(listOf(1L, 2L), descending.map { it.inventoryId })
    }
}
