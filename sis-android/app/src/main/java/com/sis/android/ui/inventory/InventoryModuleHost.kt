package com.sis.android.ui.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/*
 * Điều phối màn hình trong module tồn kho và giữ ngưỡng mẫu dùng chung
 * để sau khi lưu, danh sách cảnh báo được tính lại ngay.
 */
@Composable
fun InventoryModuleHost(
    onGoToProducts: () -> Unit,
    userRole: InventoryUserRole = InventoryUserRole.WAREHOUSE_STAFF
) {
    var currentScreen by remember { mutableStateOf(InventoryModuleScreen.LIST) }
    var thresholdProducts by remember { mutableStateOf(InventoryMockData.thresholdProducts()) }
    val inventoryItems = remember { InventoryMockData.data() }
    val valuationRows = remember { InventoryMockData.valuationRows(inventoryItems) }
    val canUpdateReserved = InventoryRolePolicy.canUpdateReserved(userRole)
    val canManageThresholds = InventoryRolePolicy.canManageThresholds(userRole)
    val canViewValuation = InventoryRolePolicy.canViewValuation(userRole)

    when (currentScreen) {
        InventoryModuleScreen.LIST -> InventoryScreen(
            onGoToProducts = onGoToProducts,
            onGoToAlerts = { currentScreen = InventoryModuleScreen.ALERTS },
            onGoToThresholds = {
                if (canManageThresholds) currentScreen = InventoryModuleScreen.THRESHOLDS
            },
            onGoToValuation = {
                if (canViewValuation) currentScreen = InventoryModuleScreen.VALUATION
            },
            canUpdateReserved = canUpdateReserved,
            canManageThresholds = canManageThresholds,
            canViewValuation = canViewValuation,
            thresholdProducts = thresholdProducts
        )

        InventoryModuleScreen.ALERTS -> InventoryAlertsScreen(
            products = thresholdProducts,
            inventoryItems = inventoryItems,
            canManageThresholds = canManageThresholds,
            canViewValuation = canViewValuation,
            onBack = { currentScreen = InventoryModuleScreen.LIST },
            onGoToThresholds = {
                if (canManageThresholds) currentScreen = InventoryModuleScreen.THRESHOLDS
            },
            onGoToValuation = {
                if (canViewValuation) currentScreen = InventoryModuleScreen.VALUATION
            }
        )

        InventoryModuleScreen.THRESHOLDS -> {
            if (canManageThresholds) {
                InventoryThresholdScreen(
                    products = thresholdProducts,
                    canEditThresholds = canManageThresholds,
                    onBack = { currentScreen = InventoryModuleScreen.LIST },
                    onSaveThresholds = { productId, minStock, maxStock ->
                        thresholdProducts = thresholdProducts.map { product ->
                            if (product.productId == productId) {
                                product.copy(minStock = minStock, maxStock = maxStock)
                            } else {
                                product
                            }
                        }
                    }
                )
            } else {
                InventoryScreen(
                    onGoToProducts = onGoToProducts,
                    onGoToAlerts = { currentScreen = InventoryModuleScreen.ALERTS },
                    canUpdateReserved = canUpdateReserved,
                    canViewValuation = canViewValuation,
                    onGoToValuation = { currentScreen = InventoryModuleScreen.VALUATION },
                    thresholdProducts = thresholdProducts
                )
            }
        }

        InventoryModuleScreen.VALUATION -> {
            if (canViewValuation) {
                InventoryValuationScreen(
                    rows = valuationRows,
                    userRole = userRole,
                    onBack = { currentScreen = InventoryModuleScreen.LIST }
                )
            } else {
                InventoryScreen(
                    onGoToProducts = onGoToProducts,
                    onGoToAlerts = { currentScreen = InventoryModuleScreen.ALERTS },
                    canUpdateReserved = canUpdateReserved,
                    canManageThresholds = canManageThresholds,
                    canViewValuation = canViewValuation,
                    thresholdProducts = thresholdProducts
                )
            }
        }
    }
}

private enum class InventoryModuleScreen {
    LIST,
    ALERTS,
    THRESHOLDS,
    VALUATION
}
