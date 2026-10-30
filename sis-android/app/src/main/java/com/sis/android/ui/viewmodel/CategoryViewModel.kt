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
                loadCategories() // Load lại danh sách mới nhất từ server
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
                repository.updateCategory(category.categoryId, category)
                loadCategories() // Load lại danh sách mới nhất từ server
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi cập nhật danh mục"
            }
        }
    }

    /** Xóa danh mục qua API */
    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId)
                loadCategories() // Load lại danh sách sau khi xóa
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi xóa danh mục"
            }
        }
    }
}