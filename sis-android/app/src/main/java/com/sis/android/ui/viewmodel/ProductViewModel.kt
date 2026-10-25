package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.Product
import com.sis.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                // Gọi từ repository của bạn
                val list = productRepository.getProducts()
                _products.value = list
            } catch (e: Exception) {
                _products.value = emptyList()
            }
        }
    }
}