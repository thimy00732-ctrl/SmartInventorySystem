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
            android.util.Log.d("CategoryRepo", "📝 Creating category: ${category.categoryName}")
            val result = apiService.createCategory(category)
            android.util.Log.d("CategoryRepo", "✅ Created successfully: ID ${result.categoryId}")
            result
        } catch (e: Exception) {
            android.util.Log.e("CategoryRepo", "❌ Error creating category: ${e.message}", e)
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
            android.util.Log.d("CategoryRepo", "🔴 Deactivating category ID=$id")
            apiService.deactivateCategory(id)
            android.util.Log.d("CategoryRepo", "✅ Deactivate successful")
            true
        } catch (e: Exception) {
            android.util.Log.e("CategoryRepo", "❌ Error deactivating: ${e.message}", e)
            false
        }
    }


    suspend fun reactivateCategory(id: Long): Boolean {
        return try {
            android.util.Log.d("CategoryRepo", "🟢 Reactivating category ID=$id")
            apiService.reactivateCategory(id)
            android.util.Log.d("CategoryRepo", "✅ Reactivate successful")
            true
        } catch (e: Exception) {
            android.util.Log.e("CategoryRepo", "❌ Error reactivating: ${e.message}", e)
            false
        }
    }


    suspend fun deleteCategory(id: Long): Boolean {
        return try {
            apiService.deleteCategory(id)
            true
        } catch (e: Exception) {
            false
        }
    }




}