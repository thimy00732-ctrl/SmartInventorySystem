package com.sis.android.ui.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.remote.ApiService
import com.sis.android.data.repository.InventoryRepository
import com.sis.android.ui.viewmodel.InventoryViewModel

/**
 * Điều phối navigation cho Inventory Module
 * Tạo ViewModel manually và quản lý navigation giữa các màn hình
 * 
 * @param initialScreen Màn hình ban đầu (từ navigation drawer)
 * @param onMenuClick Callback để mở navigation drawer
 */
@Composable
fun InventoryModuleHost(
    onGoToProducts: () -> Unit,
    userRole: InventoryUserRole = InventoryUserRole.WAREHOUSE_STAFF,
    apiService: ApiService,
    initialScreen: String? = null,
    onMenuClick: () -> Unit
) {
    // Create ViewModel
    val viewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModelFactory(
            repository = InventoryRepository(apiService)
        )
    )
    
    var currentScreen by remember { mutableStateOf(InventoryModuleScreen.LIST) }
    
    // Listen to initialScreen changes from navigation drawer
    LaunchedEffect(initialScreen) {
        val targetScreen = when (initialScreen) {
            "list" -> InventoryModuleScreen.LIST
            "alerts" -> InventoryModuleScreen.ALERTS
            "ledger" -> InventoryModuleScreen.STOCK_LEDGER
            "valuation" -> if (InventoryRolePolicy.canViewValuation(userRole)) 
                InventoryModuleScreen.VALUATION 
            else 
                InventoryModuleScreen.LIST
            "thresholds" -> if (InventoryRolePolicy.canManageThresholds(userRole)) 
                InventoryModuleScreen.THRESHOLDS 
            else 
                InventoryModuleScreen.LIST
            else -> InventoryModuleScreen.LIST
        }
        currentScreen = targetScreen
    }
    
    // Permission checks
    val canManageThresholds = InventoryRolePolicy.canManageThresholds(userRole)
    val canViewValuation = InventoryRolePolicy.canViewValuation(userRole)

    when (currentScreen) {
        InventoryModuleScreen.LIST -> InventoryScreen(
            viewModel = viewModel,
            onGoToProducts = onGoToProducts,
            onMenuClick = onMenuClick
        )

        InventoryModuleScreen.ALERTS -> InventoryAlertsScreen(
            viewModel = viewModel,
            canManageThresholds = canManageThresholds,
            onBack = { currentScreen = InventoryModuleScreen.LIST },
            onGoToThresholds = {
                if (canManageThresholds) currentScreen = InventoryModuleScreen.THRESHOLDS
            }
        )

        InventoryModuleScreen.THRESHOLDS -> {
            if (canManageThresholds) {
                InventoryThresholdScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = InventoryModuleScreen.LIST }
                )
            } else {
                // Redirect to main if no permission
                currentScreen = InventoryModuleScreen.LIST
            }
        }

        InventoryModuleScreen.VALUATION -> {
            if (canViewValuation) {
                InventoryValuationScreen(
                    viewModel = viewModel,
                    userRole = userRole,
                    onBack = { currentScreen = InventoryModuleScreen.LIST }
                )
            } else {
                // Redirect to main if no permission
                currentScreen = InventoryModuleScreen.LIST
            }
        }
        
        InventoryModuleScreen.STOCK_LEDGER -> StockLedgerScreen(
            viewModel = viewModel,
            onBack = { currentScreen = InventoryModuleScreen.LIST }
        )
    }
}

private enum class InventoryModuleScreen {
    LIST,
    ALERTS,
    THRESHOLDS,
    VALUATION,
    STOCK_LEDGER
}

// ViewModel Factory
class InventoryViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
