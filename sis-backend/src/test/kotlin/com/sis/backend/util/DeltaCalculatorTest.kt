package com.sis.backend.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Test cho DeltaCalculator - shared function cho UC36 và UC37.
 * 
 * Xác minh delta calculation cho tất cả transaction types.
 */
class DeltaCalculatorTest {
    
    @Test
    fun `test IMPORT delta is positive quantity`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "IMPORT",
            adjustmentReason = null,
            quantity = BigDecimal("100"),
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal("100"), delta)
        assertEquals("NHẬP", DeltaCalculator.getMovementKind("IMPORT", null, true))
        assertEquals("Nhập hàng", DeltaCalculator.getMovementLabel("IMPORT", null))
    }
    
    @Test
    fun `test EXPORT delta is negative quantity`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "EXPORT",
            adjustmentReason = null,
            quantity = BigDecimal("30"),
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal("-30"), delta)
        assertEquals("XUẤT", DeltaCalculator.getMovementKind("EXPORT", null, true))
        assertEquals("Xuất hàng", DeltaCalculator.getMovementLabel("EXPORT", null))
    }
    
    @Test
    fun `test TRANSFER source delta is negative`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "TRANSFER",
            adjustmentReason = null,
            quantity = BigDecimal("20"),
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal("-20"), delta)
        assertEquals("CHUYỂN XUẤT", DeltaCalculator.getMovementKind("TRANSFER", null, true))
        assertEquals("Chuyển kho", DeltaCalculator.getMovementLabel("TRANSFER", null))
    }
    
    @Test
    fun `test TRANSFER target delta is positive`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "TRANSFER",
            adjustmentReason = null,
            quantity = BigDecimal("20"),
            differenceQuantity = null,
            isSource = false
        )
        
        assertEquals(BigDecimal("20"), delta)
        assertEquals("CHUYỂN NHẬP", DeltaCalculator.getMovementKind("TRANSFER", null, false))
    }
    
    @Test
    fun `test STOCKTAKE delta uses difference_quantity`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "STOCKTAKE",
            adjustmentReason = null,
            quantity = null,
            differenceQuantity = BigDecimal("5"),
            isSource = true
        )
        
        assertEquals(BigDecimal("5"), delta)
        assertEquals("KIỂM KÊ", DeltaCalculator.getMovementKind("STOCKTAKE", null, true))
        assertEquals("Kiểm kê", DeltaCalculator.getMovementLabel("STOCKTAKE", null))
    }
    
    @Test
    fun `test STOCKTAKE delta is zero when no difference`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "STOCKTAKE",
            adjustmentReason = null,
            quantity = null,
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal.ZERO, delta)
    }
    
    @Test
    fun `test ADJUSTMENT SURPLUS delta is positive`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "ADJUSTMENT",
            adjustmentReason = "SURPLUS",
            quantity = BigDecimal("10"),
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal("10"), delta)
        assertEquals("ĐIỀU CHỈNH", DeltaCalculator.getMovementKind("ADJUSTMENT", "SURPLUS", true))
        assertEquals("Thừa hàng", DeltaCalculator.getMovementLabel("ADJUSTMENT", "SURPLUS"))
    }
    
    @Test
    fun `test ADJUSTMENT SHORTAGE delta is negative`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "ADJUSTMENT",
            adjustmentReason = "SHORTAGE",
            quantity = BigDecimal("5"),
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal("-5"), delta)
        assertEquals("Thiếu hàng", DeltaCalculator.getMovementLabel("ADJUSTMENT", "SHORTAGE"))
    }
    
    @Test
    fun `test ADJUSTMENT DAMAGE delta is negative`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "ADJUSTMENT",
            adjustmentReason = "DAMAGE",
            quantity = BigDecimal("3"),
            differenceQuantity = null,
            isSource = true
        )
        
        assertEquals(BigDecimal("-3"), delta)
        assertEquals("Hư hỏng", DeltaCalculator.getMovementLabel("ADJUSTMENT", "DAMAGE"))
    }
    
    @Test
    fun `test ADJUSTMENT OTHER uses difference_quantity`() {
        val delta = DeltaCalculator.calculate(
            transactionType = "ADJUSTMENT",
            adjustmentReason = "OTHER",
            quantity = null,
            differenceQuantity = BigDecimal("7"),
            isSource = true
        )
        
        assertEquals(BigDecimal("7"), delta)
        assertEquals("Khác", DeltaCalculator.getMovementLabel("ADJUSTMENT", "OTHER"))
    }
}
