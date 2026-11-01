package com.sis.android.data.repository

import com.sis.android.data.model.Category
import com.sis.android.data.remote.RetrofitClient

class CategoryRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getCategories(): List<Category> {
        return try {
            val categories = apiService.getCategories()
            android.util.Log.d("CategoryRepo", "🔍 API Response: ${categories.size} items")
            categories.forEach {
                android.util.Log.d("CategoryRepo", "  → ${it.categoryId}: ${it.categoryName} (active=${it.isActive})")
            }
            categories
        } catch (e: Exception) {
            android.util.Log.e("CategoryRepo", "❌ Error loading categories: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun createCategory(category: Category): Category? {
        return try {
            apiService.createCategory(category)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCategory(id: Long, category: Category): Category? {
        return try {
            apiService.updateCategory(id, category)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deactivateCategory(id: Long): Boolean {
        return try {
            apiService.deactivateCategory(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}