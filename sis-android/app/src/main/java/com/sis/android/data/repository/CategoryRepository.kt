package com.sis.android.data.repository

import com.sis.android.data.model.Category
import com.sis.android.data.remote.RetrofitClient

class CategoryRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getCategories(): List<Category> {
        return try {
            apiService.getCategories()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createCategory(category: Category): Category {
        return apiService.createCategory(category)
    }

    suspend fun updateCategory(id: Long, category: Category): Category {
        return apiService.updateCategory(id, category)
    }

    suspend fun deleteCategory(id: Long) {
        apiService.deleteCategory(id)
    }
}