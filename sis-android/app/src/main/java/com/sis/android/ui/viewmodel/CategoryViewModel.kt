package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.Category
import com.sis.android.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _categoryList = MutableStateFlow<List<Category>>(emptyList())
    val categoryList: StateFlow<List<Category>> = _categoryList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCategories()
    }

    /** Tải danh sách danh mục từ API */
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val categories = repository.getCategories()
                _categoryList.value = categories
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi tải danh sách danh mục"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Thêm danh mục mới qua API */
    fun addCategory(category: Category, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.createCategory(category)
                loadCategories()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi thêm danh mục"
            }
        }
    }

    /** Cập nhật danh mục qua API */
    fun updateCategory(category: Category, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val id = category.categoryId ?: return@launch
                repository.updateCategory(id, category)
                loadCategories()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi cập nhật danh mục"
            }
        }
    }

    /** Ngừng hoạt động danh mục qua API */
    fun deactivateCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                repository.deactivateCategory(categoryId)
                loadCategories()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi ngừng hoạt động danh mục"
            }
        }
    }

    /** Kích hoạt lại danh mục qua API */
    fun reactivateCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                repository.reactivateCategory(categoryId)
                loadCategories()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi kích hoạt lại danh mục"
            }
        }
    }

    /** Xóa danh mục qua API */
    fun deleteCategory(categoryId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId)
                loadCategories()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Lỗi xóa danh mục"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            }
        }
    }
}
