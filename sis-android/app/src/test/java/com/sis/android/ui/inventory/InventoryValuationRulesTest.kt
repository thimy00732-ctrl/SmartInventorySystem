package com.sis.android.ui.inventory

import com.sis.android.data.model.InventoryValuationRow
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Kiểm thử phép tính giá trị tồn và giới hạn hiển thị theo vai trò.
 */
class InventoryValuationRulesTest {
    @Test
    fun `gia tri ton dung BigDecimal va lam tron hai chu so`() {
        val row = InventoryValuationRow(
            warehouseId = 1,
            warehouseName = "Kho A",
            productId = 10,
            sku = "SP-10",
            productName = "Sản phẩm",
            categoryName = "Danh mục",
            totalQuantity = 3,
            currentCostPrice = BigDecimal("12.345")
        )

        assertEquals(BigDecimal("37.04"), row.stockValue)
    }

    @Test
    fun `gia von mau duoc tong hop theo kho va hien thi bang tien te VND`() {
        val rows = InventoryMockData.valuationRows()
            .filter { it.productId == 101L }
            .associateBy { it.warehouseId }
        val formatted = InventoryFormatters.formatCurrency(BigDecimal("15500000"))

        assertEquals(1250, rows[1L]?.totalQuantity)
        assertEquals(BigDecimal("15625000.00"), rows[1L]?.stockValue)
        assertEquals(300, rows[2L]?.totalQuantity)
        assertEquals(BigDecimal("3750000.00"), rows[2L]?.stockValue)
        assertTrue(formatted.contains("15.500.000") || formatted.contains("15,500,000"))
        assertTrue(formatted.contains("₫") || formatted.contains("VND"))
    }

    @Test
    fun `quyen giao dien khop voi role policy hien co`() {
        assertEquals(true, InventoryRolePolicy.canUpdateReserved(InventoryUserRole.WAREHOUSE_STAFF))
        assertEquals(false, InventoryRolePolicy.canManageThresholds(InventoryUserRole.WAREHOUSE_STAFF))
        assertEquals(false, InventoryRolePolicy.canViewValuation(InventoryUserRole.WAREHOUSE_STAFF))
        assertEquals(true, InventoryRolePolicy.canUpdateReserved(InventoryUserRole.ADMIN))
        assertEquals(true, InventoryRolePolicy.canManageThresholds(InventoryUserRole.ADMIN))
        assertEquals(true, InventoryRolePolicy.canViewValuation(InventoryUserRole.ADMIN))
    }
}
