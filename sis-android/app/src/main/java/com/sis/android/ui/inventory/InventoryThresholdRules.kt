package com.sis.android.ui.inventory

import com.sis.android.data.model.InventoryThresholdProduct

/*
 * Quy tắc kiểm tra ngưỡng và tổng hợp danh sách cảnh báo theo nghiệp vụ tồn kho.
 */
internal object InventoryThresholdRules {
    fun validateMinStock(value: String): String? {
        val parsedValue = value.toIntOrNull()
            ?: return "Nhập số nguyên không âm."
        return if (parsedValue < 0) "Không được nhập số âm." else null
    }

    fun validateMaxStock(minInput: String, maxInput: String): String? {
        val minValue = minInput.toIntOrNull()
        val maxValue = maxInput.toIntOrNull()
            ?: return "Nhập số nguyên không âm."
        return when {
            maxValue < 0 -> "Không được nhập số âm."
            minValue != null && maxValue < minValue ->
                "Tồn tối đa phải lớn hơn hoặc bằng tồn tối thiểu."
            else -> null
        }
    }

    fun lowStock(products: List<InventoryThresholdProduct>): List<InventoryThresholdProduct> =
        products.filter { it.isActive && it.totalStock < it.minStock }
            .sortedWith(
                compareByDescending<InventoryThresholdProduct> { it.minStock - it.totalStock }
                    .thenBy { it.productName }
            )

    fun overStock(products: List<InventoryThresholdProduct>): List<InventoryThresholdProduct> =
        products.filter { it.isActive && it.totalStock > it.maxStock }
            .sortedWith(
                compareByDescending<InventoryThresholdProduct> {
                    if (it.maxStock > 0) (it.totalStock.toDouble() / it.maxStock - 1) * 100 else 0.0
                }.thenByDescending { it.totalStock - it.maxStock }
                    .thenBy { it.productName }
            )
}
