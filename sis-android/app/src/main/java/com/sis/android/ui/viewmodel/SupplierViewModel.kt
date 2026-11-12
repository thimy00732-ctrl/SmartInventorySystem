package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.Supplier
import com.sis.android.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SupplierViewModel(
    private val repository: SupplierRepository = SupplierRepository()
) : ViewModel() {

    private val _supplierList = MutableStateFlow<List<Supplier>>(emptyList())
    val supplierList: StateFlow<List<Supplier>> = _supplierList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val suppliers = repository.getSuppliers()
                _supplierList.value = suppliers
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi tải danh sách nhà cung cấp"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSupplier(supplier: Supplier, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.createSupplier(supplier)
                loadSuppliers()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi thêm nhà cung cấp"
            }
        }
    }

    fun updateSupplier(supplier: Supplier, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val id = supplier.supplierId ?: return@launch
                repository.updateSupplier(id, supplier)
                loadSuppliers()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi cập nhật nhà cung cấp"
            }
        }
    }

    fun deactivateSupplier(supplierId: Long) {
        viewModelScope.launch {
            try {
                repository.deactivateSupplier(supplierId)
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi ngừng hoạt động nhà cung cấp"
            }
        }
    }

    fun reactivateSupplier(supplierId: Long) {
        viewModelScope.launch {
            try {
                repository.reactivateSupplier(supplierId)
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi kích hoạt lại nhà cung cấp"
            }
        }
    }

    /** Xóa nhà cung cấp qua API */
    fun deleteSupplier(supplierId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplierId)
                loadSuppliers()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Lỗi xóa nhà cung cấp"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            }
        }
    }
}


