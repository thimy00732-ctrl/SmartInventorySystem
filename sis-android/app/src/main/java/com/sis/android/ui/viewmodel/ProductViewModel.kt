package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.Product
import com.sis.android.data.model.ProductFilterOptions
import com.sis.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(
        val products: List<Product>,
        val totalStock: Int,
        val totalReserved: Int,
        val totalAvailable: Int,
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
    private val _selectedWarehouseName = MutableStateFlow<String?>(null)
    val selectedWarehouseName: StateFlow<String?> = _selectedWarehouseName.asStateFlow()

    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Expose pagination state
    private val _currentPage = MutableStateFlow(1)
    val currentPageState: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _totalPages = MutableStateFlow(1)
    val totalPagesState: StateFlow<Int> = _totalPages.asStateFlow()

    // Quản lý dữ liệu options cho dropdown lọc
    private val _filterOptions = MutableStateFlow<ProductFilterOptions?>(null)
    val filterOptions: StateFlow<ProductFilterOptions?> = _filterOptions.asStateFlow()

    private var allProductsCache = listOf<Product>()

    // Chỉ dùng _currentPage StateFlow, XÓA biến private var currentPage
    private var hasMorePages = true
    private var totalPages = 1  // Thêm: Tổng số trang từ backend

    // State phân trang
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Lưu filter hiện tại để load thêm
    private var currentFilter: FilterState? = null

    data class FilterState(
        val keyword: String?,
        val warehouseId: Long?,
        val categoryId: Long?,
        val supplierId: Long?,
        val isActive: Boolean?,
        val sortBy: String,
        val sortDirection: String
    )


    init {
        loadFilterOptions()
        loadProducts()
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            try {
                val options = repository.getFilterOptions()
                _filterOptions.value = options
            } catch (e: Exception) {
                android.util.Log.e("ProductVM", "❌ Lỗi load filter options: ${e.message}")
            }
        }
    }

    fun loadProducts(loadMore: Boolean = false) {
        if (loadMore && (_isLoadingMore.value || !hasMorePages)) {
            android.util.Log.d("ProductVM", "⏸️ Không load: isLoadingMore=${_isLoadingMore.value}, hasMorePages=$hasMorePages")
            return
        }

        android.util.Log.d("ProductVM", "📥 loadProducts: page=${_currentPage.value}")

        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading

            try {
                val keywordParam = _searchQuery.value.trim().takeIf { it.isNotEmpty() }
                val filter = currentFilter
                
                val pageResponse = repository.getProducts(
                    keyword = filter?.keyword ?: keywordParam,
                    warehouseId = filter?.warehouseId,
                    categoryId = filter?.categoryId,
                    supplierId = filter?.supplierId,
                    isActive = filter?.isActive,
                    sortBy = filter?.sortBy ?: "created_at",
                    sortDirection = filter?.sortDirection ?: "DESC",
                    page = _currentPage.value,
                    size = 10  // Thay đổi từ 20 → 10
                )

                android.util.Log.d("ProductVM", "✅ Nhận ${pageResponse.items.size} sản phẩm, page=${pageResponse.page}/${pageResponse.totalPages}, total=${pageResponse.totalItems}")

                // Chỉ hiển thị trang hiện tại, không cộng dồn
                allProductsCache = pageResponse.items
                totalPages = pageResponse.totalPages
                _totalPages.value = pageResponse.totalPages
                _currentPage.value = pageResponse.page // Sync từ backend
                hasMorePages = _currentPage.value < totalPages

                android.util.Log.d("ProductVM", "📊 Cache: ${allProductsCache.size}, hasMorePages=$hasMorePages, currentPage=${_currentPage.value}")

                updateFilteredState()
            } catch (e: Exception) {
                android.util.Log.e("ProductVM", "❌ Lỗi: ${e.message}")
                _uiState.value = ProductUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối Server")
            }
        }
    }

    // Hàm mới: Chuyển đến trang cụ thể
    fun goToPage(page: Int) {
        if (page in 1..totalPages && page != _currentPage.value) {
            _currentPage.value = page
            loadProducts()
        }
    }

    // Hàm mới: Trang trước
    fun previousPage() {
        if (_currentPage.value > 1) {
            _currentPage.value = _currentPage.value - 1
            loadProducts()
        }
    }

    // Hàm mới: Trang sau
    fun nextPage() {
        if (_currentPage.value < totalPages) {
            _currentPage.value = _currentPage.value + 1
            loadProducts()
        }
    }

    fun applyFilter(
        keyword: String?,
        warehouseId: Long? = null,
        categoryId: Long?,
        supplierId: Long?,
        isActive: Boolean?,
        sortBy: String,
        sortDirection: String,
        loadMore: Boolean = false
    ) {
        // Lưu filter hiện tại
        currentFilter = FilterState(keyword, warehouseId, categoryId, supplierId, isActive, sortBy, sortDirection)

        if (loadMore && (_isLoadingMore.value || !hasMorePages)) return

        // Lưu tên kho đã chọn
        _selectedWarehouseName.value = if (warehouseId != null) {
            filterOptions.value?.warehouses?.find { it.warehouseId == warehouseId }?.warehouseName
        } else {
            null
        }

        // Reset về trang 1 khi apply filter
        _currentPage.value = 1
        loadProducts()
    }

    // Function mới: Load thêm trang tiếp theo
    fun loadMoreProducts() {
        currentFilter?.let {
            applyFilter(
                keyword = it.keyword,
                warehouseId = it.warehouseId,
                categoryId = it.categoryId,
                supplierId = it.supplierId,
                isActive = it.isActive,
                sortBy = it.sortBy,
                sortDirection = it.sortDirection,
                loadMore = true
            )
        } ?: loadProducts(loadMore = true)
    }





    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadProducts()
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
                "LOW_STOCK" -> {
                    // Chỉ lọc sản phẩm có đủ thông tin inventory
                    product.availableQuantity > 0 && product.availableQuantity <= product.minStock
                }
                "EXPIRING" -> false
                else -> true
            }

            matchesSearch && matchesTab
        }

        val totalStock = allProductsCache.sumOf { it.stockQuantity }
        val totalReserved = allProductsCache.sumOf { it.reservedQuantity }
        val totalAvailable = allProductsCache.sumOf { it.availableQuantity }
        val lowStockCount = allProductsCache.count { 
            it.availableQuantity > 0 && it.availableQuantity <= it.minStock 
        }
        val nearExpiryCount = allProductsCache.count { false }

        _uiState.value = ProductUiState.Success(
            products = filtered,
            totalStock = totalStock,
            totalReserved = totalReserved,
            totalAvailable = totalAvailable,
            lowStockCount = lowStockCount,
            nearExpiryCount = nearExpiryCount
        )
    }

    // CRUD: Thêm sản phẩm mới
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

    // CRUD: Cập nhật sản phẩm
    fun updateProduct(
        updated: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val resultProduct = repository.updateProduct(updated.productId, updated)
                if (resultProduct != null) {
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

    fun deleteProduct(
        productId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.deleteProduct(productId)
                if (success) {
                    loadProducts()
                    onSuccess()
                } else {
                    onError("Không thể xóa sản phẩm từ server. Hãy kiểm tra lại trạng thái!")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductVM", "❌ Lỗi xóa sản phẩm: ${e.message}", e)
                onError("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }

    /** Ngừng kinh doanh / Kích hoạt lại sản phẩm */
    fun toggleProductActive(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = if (product.isActive) {
                    repository.deactivateProduct(product.productId)
                } else {
                    repository.reactivateProduct(product.productId)
                }

                if (success) {
                    loadProducts()
                    onSuccess()
                } else {
                    onError("Không thể thay đổi trạng thái sản phẩm từ server.")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductVM", "❌ Lỗi toggle active: ${e.message}", e)
                onError("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }

    fun scanBarcode(
        barcode: String,
        onFound: (Product) -> Unit,
        onNotFound: (String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanCode = barcode.trim()
            try {
                var product = repository.getProductByBarcode(cleanCode)

                if (product == null) {
                    product = allProductsCache.find {
                        it.sku.equals(cleanCode, ignoreCase = true) ||
                                it.barcode.equals(cleanCode, ignoreCase = true)
                    }
                }

                if (product != null) {
                    onFound(product)
                } else {
                    onNotFound(cleanCode)
                }
            } catch (e: Exception) {
                val fallbackProduct = allProductsCache.find {
                    it.sku.equals(cleanCode, ignoreCase = true) ||
                            it.barcode.equals(cleanCode, ignoreCase = true)
                }
                if (fallbackProduct != null) {
                    onFound(fallbackProduct)
                } else {
                    onNotFound(cleanCode)
                }
            }
        }
    }
}