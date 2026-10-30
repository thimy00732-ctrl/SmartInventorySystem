package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.Product
import com.sis.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(
        val products: List<Product>,
        val totalStock: Double,
        val totalReserved: Double,
        val totalAvailable: Double,
        val lowStockCount: Int,
        val nearExpiryCount: Int
    ) : ProductUiState
    data class Error(val message: String) : ProductUiState
}


class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow("ALL") // "ALL", "LOW_STOCK", "EXPIRING"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private var allProductsCache = listOf<Product>()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                // Gọi repository lấy danh sách sản phẩm từ API thực tế
                // (Có thể truyền _searchQuery.value nếu API hỗ trợ tìm kiếm trực tiếp trên server)
                val list = repository.getProducts(keyword = _searchQuery.value.ifEmpty { null })
                allProductsCache = list
                updateFilteredState()
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối Server")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredState()
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
        updateFilteredState()
    }

    private fun updateFilteredState() {
        val query = _searchQuery.value.trim().lowercase()
        val tab = _selectedTab.value

        val filtered = allProductsCache.filter { product ->
            val matchesSearch = query.isEmpty() ||
                    product.productName.lowercase().contains(query) ||
                    product.sku.lowercase().contains(query) ||
                    (product.barcode != null && product.barcode.contains(query))

            val matchesTab = when (tab) {
                "LOW_STOCK" -> product.availableQuantity <= product.minStock
                "EXPIRING" -> false // Giả lập logic cận hạn (có thể bổ sung khi có hạn sử dụng chi tiết)
                else -> true
            }

            matchesSearch && matchesTab
        }

        // Tính toán các chỉ số tổng hợp thống kê cho Dashboard Header
        val totalStock = allProductsCache.sumOf { it.stockQuantity }
        val totalReserved = allProductsCache.sumOf { it.reservedQuantity }
        val totalAvailable = allProductsCache.sumOf { it.availableQuantity }
        val lowStockCount = allProductsCache.count { it.availableQuantity <= it.minStock }
        val nearExpiryCount = 5 // Giá trị demo theo UI Figma

        _uiState.value = ProductUiState.Success(
            products = filtered,
            totalStock = totalStock,
            totalReserved = totalReserved,
            totalAvailable = totalAvailable,
            lowStockCount = lowStockCount,
            nearExpiryCount = nearExpiryCount
        )
    }

    // Các hàm CRUD hỗ trợ mở rộng cho các nhiệm vụ sau
    fun addProduct(product: Product) {
        allProductsCache = listOf(product) + allProductsCache
        updateFilteredState()
    }

    fun updateProduct(updated: Product) {
        allProductsCache = allProductsCache.map { if (it.productId == updated.productId) updated else it }
        updateFilteredState()
    }

    fun deleteProduct(productId: Long) {
        allProductsCache = allProductsCache.filter { it.productId != productId }
        updateFilteredState()
    }

    // Thêm vào trong ProductViewModel.kt
    fun scanBarcode(
        barcode: String,
        onFound: (Product) -> Unit,
        onNotFound: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val product = repository.getProductByBarcode(barcode)
                if (product != null) {
                    onFound(product)
                } else {
                    onNotFound(barcode)
                }
            } catch (e: Exception) {
                onNotFound(barcode)
            }
        }
    }
}