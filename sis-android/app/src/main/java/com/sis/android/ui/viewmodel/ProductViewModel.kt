package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.Product
import com.sis.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(
        val products: List<Product>,
        val totalStock: Int,         // Đổi thành Int
        val totalReserved: Int,      // Đổi thành Int
        val totalAvailable: Int,     // Đổi thành Int
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
                "EXPIRING" -> false
                else -> true
            }

            matchesSearch && matchesTab
        }

        // Tính toán các chỉ số tổng hợp thống kê cho Dashboard Header (Dùng sumOf với kiểu Int)
        val totalStock = allProductsCache.sumOf { it.stockQuantity }
        val totalReserved = allProductsCache.sumOf { it.reservedQuantity }
        val totalAvailable = allProductsCache.sumOf { it.availableQuantity }
        val lowStockCount = allProductsCache.count { it.availableQuantity <= it.minStock }
        val nearExpiryCount = 5

        _uiState.value = ProductUiState.Success(
            products = filtered,
            totalStock = totalStock,
            totalReserved = totalReserved,
            totalAvailable = totalAvailable,
            lowStockCount = lowStockCount,
            nearExpiryCount = nearExpiryCount
        )
    }

    // CRUD: Thêm sản phẩm mới gọi API qua Repository
    fun addProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val createdProduct = repository.createProduct(product)
                if (createdProduct != null) {
                    allProductsCache = listOf(createdProduct) + allProductsCache
                    updateFilteredState()
                    onSuccess()
                } else {
                    onError("Không thể tạo sản phẩm trên server.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Lỗi kết nối server khi thêm sản phẩm.")
            }
        }
    }

    // CRUD: Cập nhật sản phẩm gọi API qua Repository
    fun updateProduct(
        updated: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Gọi repository để bắn PUT request lên server
                val resultProduct = repository.updateProduct(updated.productId, updated)
                if (resultProduct != null) {
                    // Cập nhật thành công trên server thì mới đổi cache ở local
                    allProductsCache = allProductsCache.map {
                        if (it.productId == resultProduct.productId) resultProduct else it
                    }
                    updateFilteredState()
                    onSuccess()
                } else {
                    onError("Không thể cập nhật sản phẩm trên server.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Lỗi kết nối server khi cập nhật sản phẩm.")
            }
        }
    }

    fun deleteProduct(productId: Long) {
        allProductsCache = allProductsCache.filter { it.productId != productId }
        updateFilteredState()
    }

    /** Ngừng kinh doanh sản phẩm */
    fun toggleProductActive(product: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = if (product.isActive) {
                    repository.deactivateProduct(product.productId)
                } else {
                    repository.reactivateProduct(product.productId)
                }

                if (success) {
                    val updatedProduct = product.copy(isActive = !product.isActive)
                    allProductsCache = allProductsCache.map {
                        if (it.productId == product.productId) updatedProduct else it
                    }
                    updateFilteredState()
                    onSuccess()
                } else {
                    onError("Không thể ${if (product.isActive) "ngừng" else "kích hoạt"} sản phẩm")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Lỗi kết nối server")
            }
        }
    }

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
            } catch (_: Exception) { // Thay 'e' bằng '_' để tránh cảnh báo biến không dùng tới
                onNotFound(barcode)
            }
        }
    }
}