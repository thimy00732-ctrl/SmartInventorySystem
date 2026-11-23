package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.*
import com.sis.android.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

// ==================== UI States ====================

sealed interface InventoryUiState {
    object Loading : InventoryUiState
    data class Success(
        val items: List<InventoryItem>,
        val totalStock: Int,
        val totalReserved: Int,
        val totalAvailable: Int
    ) : InventoryUiState
    data class Error(val message: String) : InventoryUiState
}

sealed interface AlertUiState {
    object Loading : AlertUiState
    data class Success(
        val lowStockAlerts: List<LowStockAlert>,
        val overStockAlerts: List<OverStockAlert>,
        val summary: AlertSummary?
    ) : AlertUiState
    data class Error(val message: String) : AlertUiState
}

sealed interface ThresholdUiState {
    object Loading : ThresholdUiState
    data class Success(
        val products: List<ThresholdProduct>
    ) : ThresholdUiState
    data class Error(val message: String) : ThresholdUiState
}

sealed interface StockLedgerUiState {
    object Loading : StockLedgerUiState
    data class Success(
        val entries: List<StockLedgerEntry>,
        val page: Int,
        val totalPages: Int,
        val totalItems: Long
    ) : StockLedgerUiState
    data class Error(val message: String) : StockLedgerUiState
}

sealed interface ValuationUiState {
    object Loading : ValuationUiState
    data class Success(
        val items: List<ValuationDetail>,
        val total: BigDecimal,
        val summary: ValuationSummary?,
        val page: Int,
        val totalPages: Int
    ) : ValuationUiState
    data class Error(val message: String) : ValuationUiState
}

// ==================== ViewModel ====================

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    // ==================== Inventory State ====================
    
    private val _inventoryUiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val inventoryUiState: StateFlow<InventoryUiState> = _inventoryUiState.asStateFlow()

    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private var currentInventoryPage = 1
    private var hasMoreInventoryPages = true
    private var totalInventoryPages = 1
    
    // Exposed pagination state for UI
    private val _currentInventoryPageState = MutableStateFlow(1)
    val currentInventoryPageState: StateFlow<Int> = _currentInventoryPageState.asStateFlow()
    
    private val _totalInventoryPagesState = MutableStateFlow(1)
    val totalInventoryPagesState: StateFlow<Int> = _totalInventoryPagesState.asStateFlow()

    // ==================== Alert State ====================
    
    private val _alertUiState = MutableStateFlow<AlertUiState>(AlertUiState.Loading)
    val alertUiState: StateFlow<AlertUiState> = _alertUiState.asStateFlow()

    // ==================== Threshold State ====================
    
    private val _thresholdUiState = MutableStateFlow<ThresholdUiState>(ThresholdUiState.Loading)
    val thresholdUiState: StateFlow<ThresholdUiState> = _thresholdUiState.asStateFlow()

    // ==================== Stock Ledger State ====================
    
    private val _stockLedgerUiState = MutableStateFlow<StockLedgerUiState>(StockLedgerUiState.Loading)
    val stockLedgerUiState: StateFlow<StockLedgerUiState> = _stockLedgerUiState.asStateFlow()
    
    private var currentStockLedgerPage = 1
    private var totalStockLedgerPages = 1
    
    // Exposed pagination state for UI
    private val _currentStockLedgerPageState = MutableStateFlow(1)
    val currentStockLedgerPageState: StateFlow<Int> = _currentStockLedgerPageState.asStateFlow()
    
    private val _totalStockLedgerPagesState = MutableStateFlow(1)
    val totalStockLedgerPagesState: StateFlow<Int> = _totalStockLedgerPagesState.asStateFlow()

    // ==================== Valuation State ====================
    
    private val _valuationUiState = MutableStateFlow<ValuationUiState>(ValuationUiState.Loading)
    val valuationUiState: StateFlow<ValuationUiState> = _valuationUiState.asStateFlow()
    
    private var currentValuationPage = 1
    private var totalValuationPages = 1
    
    // Exposed pagination state for UI
    private val _currentValuationPageState = MutableStateFlow(1)
    val currentValuationPageState: StateFlow<Int> = _currentValuationPageState.asStateFlow()
    
    private val _totalValuationPagesState = MutableStateFlow(1)
    val totalValuationPagesState: StateFlow<Int> = _totalValuationPagesState.asStateFlow()

    // ==================== Filter Options ====================
    
    private val _filterOptions = MutableStateFlow<ProductFilterOptions?>(null)
    val filterOptions: StateFlow<ProductFilterOptions?> = _filterOptions.asStateFlow()

    init {
        loadFilterOptions()
        loadInventory()
    }

    // ==================== Inventory Methods (UC29-UC32) ====================

    private fun loadFilterOptions() {
        viewModelScope.launch {
            try {
                val options = repository.getInventoryFilterOptions()
                _filterOptions.value = options
            } catch (e: Exception) {
                android.util.Log.e("InventoryVM", "❌ Error loadFilterOptions: ${e.message}")
            }
        }
    }

    fun loadInventory(
        keyword: String? = null,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        supplierId: Long? = null,
        stockStatus: String? = null,
        sortBy: String? = "product_name",
        sortDirection: String? = "ASC",
        page: Int = 1
    ) {
        viewModelScope.launch {
            _inventoryUiState.value = InventoryUiState.Loading

            try {
                val response = repository.getInventory(
                    keyword = keyword,
                    warehouseId = warehouseId,
                    categoryId = categoryId,
                    supplierId = supplierId,
                    stockStatus = stockStatus,
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    page = page,
                    size = 10
                )

                currentInventoryPage = page
                totalInventoryPages = response.totalPages
                hasMoreInventoryPages = page < totalInventoryPages
                
                // Update exposed state for UI
                _currentInventoryPageState.value = page
                _totalInventoryPagesState.value = totalInventoryPages

                val totalStock = response.items.sumOf { it.stockQuantity }
                val totalReserved = response.items.sumOf { it.reservedQuantity }
                val totalAvailable = response.items.sumOf { it.availableQuantity }

                _inventoryUiState.value = InventoryUiState.Success(
                    items = response.items,
                    totalStock = totalStock,
                    totalReserved = totalReserved,
                    totalAvailable = totalAvailable
                )

                android.util.Log.d("InventoryVM", "✅ Loaded ${response.items.size} inventory items, page $page/$totalInventoryPages")
            } catch (e: Exception) {
                android.util.Log.e("InventoryVM", "❌ Error loadInventory: ${e.message}", e)
                _inventoryUiState.value = InventoryUiState.Error(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
        loadInventory(keyword = query.trim().takeIf { it.isNotEmpty() })
    }

    fun applyInventoryFilter(
        keyword: String?,
        warehouseId: Long?,
        categoryId: Long?,
        supplierId: Long?,
        stockStatus: String?,
        sortBy: String,
        sortDirection: String
    ) {
        loadInventory(
            keyword = keyword,
            warehouseId = warehouseId,
            categoryId = categoryId,
            supplierId = supplierId,
            stockStatus = stockStatus,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
    }

    fun nextInventoryPage() {
        if (hasMoreInventoryPages) {
            loadInventory(page = currentInventoryPage + 1)
        }
    }

    fun previousInventoryPage() {
        if (currentInventoryPage > 1) {
            loadInventory(page = currentInventoryPage - 1)
        }
    }
    
    fun goToInventoryPage(page: Int) {
        if (page in 1..totalInventoryPages) {
            loadInventory(page = page)
        }
    }

    fun updateReservedQuantity(
        inventoryId: Long,
        reservedQuantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.updateReservedQuantity(inventoryId, reservedQuantity)
                if (result != null) {
                    loadInventory() // Reload data
                    onSuccess()
                } else {
                    onError("Không thể cập nhật reserved quantity")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }

    // ==================== Alert Methods (UC34-UC35) ====================

    fun loadAlerts() {
        viewModelScope.launch {
            _alertUiState.value = AlertUiState.Loading

            try {
                val lowStockAlerts = repository.getLowStockAlerts()
                val overStockAlerts = repository.getOverStockAlerts()
                val summary = repository.getAlertSummary()

                _alertUiState.value = AlertUiState.Success(
                    lowStockAlerts = lowStockAlerts,
                    overStockAlerts = overStockAlerts,
                    summary = summary
                )

                android.util.Log.d("InventoryVM", "✅ Loaded alerts: ${lowStockAlerts.size} low, ${overStockAlerts.size} over")
            } catch (e: Exception) {
                android.util.Log.e("InventoryVM", "❌ Error loadAlerts: ${e.message}", e)
                _alertUiState.value = AlertUiState.Error(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }

    // ==================== Threshold Methods (UC33) ====================

    fun loadThresholds(warehouseId: Long? = null) {
        viewModelScope.launch {
            _thresholdUiState.value = ThresholdUiState.Loading

            try {
                val products = repository.getProductThresholds(warehouseId)

                _thresholdUiState.value = ThresholdUiState.Success(
                    products = products
                )

                android.util.Log.d("InventoryVM", "✅ Loaded ${products.size} threshold products")
            } catch (e: Exception) {
                android.util.Log.e("InventoryVM", "❌ Error loadThresholds: ${e.message}", e)
                _thresholdUiState.value = ThresholdUiState.Error(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }

    fun updateThreshold(
        productId: Long,
        minStock: Int,
        maxStock: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.updateProductThresholds(productId, minStock, maxStock)
                if (result != null) {
                    loadThresholds() // Reload data
                    onSuccess()
                } else {
                    onError("Không thể cập nhật ngưỡng tồn kho")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }

    // ==================== Stock Ledger Methods (UC36) ====================

    fun loadStockLedger(
        fromDate: String,
        toDate: String,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1
    ) {
        viewModelScope.launch {
            _stockLedgerUiState.value = StockLedgerUiState.Loading

            try {
                val response = repository.getStockLedger(
                    fromDate = fromDate,
                    toDate = toDate,
                    warehouseId = warehouseId,
                    categoryId = categoryId,
                    keyword = keyword,
                    page = page,
                    size = 10
                )

                if (response != null) {
                    currentStockLedgerPage = page
                    totalStockLedgerPages = response.totalPages
                    
                    // Update exposed state for UI
                    _currentStockLedgerPageState.value = page
                    _totalStockLedgerPagesState.value = response.totalPages
                    
                    _stockLedgerUiState.value = StockLedgerUiState.Success(
                        entries = response.items,
                        page = response.page,
                        totalPages = response.totalPages,
                        totalItems = response.totalItems
                    )
                    android.util.Log.d("InventoryVM", "✅ Loaded ${response.items.size} stock ledger entries")
                } else {
                    _stockLedgerUiState.value = StockLedgerUiState.Error("Không thể tải sổ kho")
                }
            } catch (e: Exception) {
                android.util.Log.e("InventoryVM", "❌ Error loadStockLedger: ${e.message}", e)
                _stockLedgerUiState.value = StockLedgerUiState.Error(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }
    
    fun nextStockLedgerPage(fromDate: String, toDate: String) {
        if (currentStockLedgerPage < totalStockLedgerPages) {
            loadStockLedger(fromDate = fromDate, toDate = toDate, page = currentStockLedgerPage + 1)
        }
    }
    
    fun previousStockLedgerPage(fromDate: String, toDate: String) {
        if (currentStockLedgerPage > 1) {
            loadStockLedger(fromDate = fromDate, toDate = toDate, page = currentStockLedgerPage - 1)
        }
    }
    
    fun goToStockLedgerPage(page: Int, fromDate: String, toDate: String) {
        if (page in 1..totalStockLedgerPages) {
            loadStockLedger(fromDate = fromDate, toDate = toDate, page = page)
        }
    }

    // ==================== Valuation Methods (UC39) ====================

    fun loadValuation(
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1
    ) {
        viewModelScope.launch {
            _valuationUiState.value = ValuationUiState.Loading

            try {
                val response = repository.getValuation(
                    warehouseId = warehouseId,
                    categoryId = categoryId,
                    keyword = keyword,
                    page = page,
                    size = 10
                )

                if (response != null) {
                    currentValuationPage = page
                    totalValuationPages = response.totalPages
                    
                    // Update exposed state for UI
                    _currentValuationPageState.value = page
                    _totalValuationPagesState.value = response.totalPages
                    
                    _valuationUiState.value = ValuationUiState.Success(
                        items = response.items,
                        total = response.total,
                        summary = response.summary,
                        page = response.page,
                        totalPages = response.totalPages
                    )
                    android.util.Log.d("InventoryVM", "✅ Loaded ${response.items.size} valuation items")
                } else {
                    _valuationUiState.value = ValuationUiState.Error("Không thể tải giá trị tồn kho")
                }
            } catch (e: Exception) {
                android.util.Log.e("InventoryVM", "❌ Error loadValuation: ${e.message}", e)
                _valuationUiState.value = ValuationUiState.Error(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }
    
    fun nextValuationPage() {
        if (currentValuationPage < totalValuationPages) {
            loadValuation(page = currentValuationPage + 1)
        }
    }
    
    fun previousValuationPage() {
        if (currentValuationPage > 1) {
            loadValuation(page = currentValuationPage - 1)
        }
    }
    
    fun goToValuationPage(page: Int) {
        if (page in 1..totalValuationPages) {
            loadValuation(page = page)
        }
    }
}
