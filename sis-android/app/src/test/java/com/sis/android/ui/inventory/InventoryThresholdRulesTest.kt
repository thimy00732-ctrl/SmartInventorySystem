package com.sis.android.ui.inventory

import com.sis.android.data.model.InventoryThresholdProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Kiểm thử validation min/max và điều kiện phân loại cảnh báo tồn kho.
 */
class InventoryThresholdRulesTest {
    @Test
    fun `kiem tra nguong khong am va max khong nho hon min`() {
        assertNull(InventoryThresholdRules.validateMinStock("0"))
        assertEquals(
            "Không được nhập số âm.",
            InventoryThresholdRules.validateMinStock("-1")
        )
        assertNull(InventoryThresholdRules.validateMaxStock("10", "10"))
        assertEquals(
            "Tồn tối đa phải lớn hơn hoặc bằng tồn tối thiểu.",
            InventoryThresholdRules.validateMaxStock("11", "10")
        )
        assertEquals(
            "Nhập số nguyên không âm.",
            InventoryThresholdRules.validateMaxStock("1", "abc")
        )
    }

    @Test
    fun `phan loai va sap xep canh bao dua tren tong ton va nguong`() {
        val products = listOf(
            thresholdProduct(1, totalStock = 0, minStock = 5, maxStock = 20),
            thresholdProduct(2, totalStock = 4, minStock = 5, maxStock = 20),
            thresholdProduct(3, totalStock = 20, minStock = 5, maxStock = 20),
            thresholdProduct(4, totalStock = 30, minStock = 5, maxStock = 20),
            thresholdProduct(5, totalStock = 30, minStock = 5, maxStock = 20, isActive = false)
        )

        assertEquals(listOf(1L, 2L), InventoryThresholdRules.lowStock(products).map { it.productId })
        assertEquals(listOf(4L), InventoryThresholdRules.overStock(products).map { it.productId })
    }

    private fun thresholdProduct(
        id: Long,
        totalStock: Int,
        minStock: Int,
        maxStock: Int,
        isActive: Boolean = true
    ) = InventoryThresholdProduct(
        productId = id,
        sku = "SKU-$id",
        productName = "Sản phẩm $id",
        unit = "Cái",
        categoryName = "Danh mục",
        supplierName = "Nhà cung cấp",
        totalStock = totalStock,
        minStock = minStock,
        maxStock = maxStock,
        isActive = isActive
    )
}
