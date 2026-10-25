package com.sis.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sis.android.data.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryViewModel : ViewModel() {
    private val _categoryList = MutableStateFlow<List<Category>>(emptyList())
    val categoryList: StateFlow<List<Category>> = _categoryList.asStateFlow()

    fun addCategory(category: Category) {
        val current = _categoryList.value.toMutableList()
        current.add(category)
        _categoryList.value = current
    }

    fun updateCategory(category: Category) {
        _categoryList.value = _categoryList.value.map {
            if (it.categoryId == category.categoryId) category else it
        }
    }

    fun deleteCategory(categoryId: Long) {
        _categoryList.value = _categoryList.value.filter { it.categoryId != categoryId }
    }
}