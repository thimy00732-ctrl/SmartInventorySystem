package com.sis.android.ui.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Kiểm tra các giới hạn nghiệp vụ của ô cập nhật số lượng giữ chỗ.
 */
class InventoryReservationValidationTest {
    @Test
    fun `cho phep gia tri tu khong den bang ton thuc te`() {
        assertNull(validateReservedQuantityInput("0", 10))
        assertNull(validateReservedQuantityInput("10", 10))
    }

    @Test
    fun `tu choi so am va so vuot ton thuc te`() {
        assertEquals(
            "Số lượng giữ chỗ không được âm.",
            validateReservedQuantityInput("-1", 10)
        )
        assertEquals(
            "Số lượng giữ chỗ không được vượt tồn thực tế.",
            validateReservedQuantityInput("11", 10)
        )
    }

    @Test
    fun `tu choi gia tri khong phai so nguyen`() {
        assertEquals(
            "Vui lòng nhập số nguyên không âm.",
            validateReservedQuantityInput("1.5", 10)
        )
    }
}
