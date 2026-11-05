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
                // Gửi từ khóa tìm kiếm lên API backend
                val keywordParam = _searchQuery.value.trim().takeIf { it.isNotEmpty() }
                val list = repository.getProducts(keyword = keywordParam)

                allProductsCache = list
                updateFilteredState()
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối Server")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // 🔄 Gọi lại hàm tải dữ liệu từ server với từ khóa mới
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
        // Đếm số lượng tồn thấp thực tế
        val lowStockCount = allProductsCache.count { it.availableQuantity <= it.minStock }

        // 🛠️ Đếm số lượng cận hạn thực tế từ cache (Thay thế số 5 cứng bằng logic thực tế của bạn)
        val nearExpiryCount = allProductsCache.count { product ->
            // Ví dụ: Kiểm tra nếu sản phẩm thuộc diện cận hạn (nếu có trường hạn sử dụng)
            // product.isNearExpiry == true
            false // Tạm thời trả về 0 nếu chưa có dữ liệu cận hạn, số hiển thị sẽ tự động cập nhật về (0)
        }

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

    fun deleteProduct(
        productId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.deleteProduct(productId)
                if (success) {
                    // 🔄 Gọi tải lại danh sách sản phẩm từ server để UI cập nhật ngay lập tức
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

    /** Ngừng kinh doanh sản phẩm */
    fun toggleProductActive(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = if (product.isActive) {
                    // Gọi API ngừng kinh doanh (isActive = true -> false)
                    repository.deactivateProduct(product.productId)
                } else {
                    // Gọi API tiếp tục kinh doanh (isActive = false -> true)
                    repository.reactivateProduct(product.productId)
                }

                if (success) {
                    loadProducts() // Tải lại danh sách sản phẩm để cập nhật UI chung
                    onSuccess()    // Báo thành công về màn hình chi tiết
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
                // 1. Thử tìm chính xác theo barcode trước
                var product = repository.getProductByBarcode(cleanCode)

                // 2. Nếu không thấy bằng barcode, thử tìm vét cạn trong cache xem có khớp với SKU không
                if (product == null) {
                    product = allProductsCache.find {
                        it.sku.equals(cleanCode, ignoreCase = true) ||
                                it.barcode.equals(cleanCode, ignoreCase = true)
                    }
                }

                if (product != null) {
                    android.util.Log.d("ProductVM", "✅ Quét thành công: ${product.productName}")
                    onFound(product)
                } else {
                    android.util.Log.w("ProductVM", "⚠️ Không tìm thấy sản phẩm cho mã: $cleanCode")
                    onNotFound(cleanCode)
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductVM", "❌ Lỗi API scan: ${e.message}")
                // Thử tìm trong cache local phòng hờ mất mạng
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