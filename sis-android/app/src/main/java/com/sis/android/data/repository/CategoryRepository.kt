package com.sis.android.data.repository

import com.sis.android.data.model.Category
import com.sis.android.data.remote.RetrofitClient

class CategoryRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getCategories(): List<Category> {
        return try {
            val response = apiService.getCategories()
            if (response.success) {
                response.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createCategory(category: Category): Category? {
        return try {
            val response = apiService.createCategory(category)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCategory(id: Long, category: Category): Category? {
        return try {
            val response = apiService.updateCategory(id, category)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteCategory(id: Long): Boolean {
        return try {
            val response = apiService.deleteCategory(id)
            response.success
        } catch (e: Exception) {
            false
        }
    }
}