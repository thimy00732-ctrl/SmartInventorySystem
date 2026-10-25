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

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                // Gọi suspend fun trực tiếp để lấy danh sách Product
                val list = repository.getProducts()
                _products.value = list
            } catch (e: Exception) {
                // Bắt lỗi nếu gọi repository gặp exception (ví dụ simulateError = true)
                _products.value = emptyList()
            }
        }
    }

    // Thêm sản phẩm mới
    fun addProduct(product: Product) {
        _products.update { currentList ->
            listOf(product) + currentList
        }
    }

    // Cập nhật sản phẩm
    fun updateProduct(updatedProduct: Product) {
        _products.update { currentList ->
            currentList.map { product ->
                if (product.productId == updatedProduct.productId) updatedProduct else product
            }
        }
    }

    // Xóa sản phẩm
    fun deleteProduct(productId: Long) {
        _products.update { currentList ->
            currentList.filter { it.productId != productId }
        }
    }
}