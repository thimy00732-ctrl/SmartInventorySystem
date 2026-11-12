package com.sis.android.data.repository

import com.sis.android.data.model.Supplier
import com.sis.android.data.remote.RetrofitClient

class SupplierRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getSuppliers(
        keyword: String? = null,
        activeOnly: Boolean? = null  // Đổi thành null để lấy tất cả
    ): List<Supplier>
    {
        return try {
            val suppliers = apiService.getSuppliers(keyword, activeOnly)
            android.util.Log.d("SupplierRepo", "🔍 API Response: ${suppliers.size} items")
            suppliers.forEach {
                android.util.Log.d("SupplierRepo", "  → ${it.supplierId}: ${it.supplierName} (active=${it.isActive})")
            }
            suppliers
        } catch (e: Exception) {
            android.util.Log.e("SupplierRepo", "❌ Error loading suppliers: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getSupplierById(id: Long): Supplier? {
        return try {
            apiService.getSupplierById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createSupplier(supplier: Supplier): Supplier? {
        return try {
            android.util.Log.d("SupplierRepo", "📝 Creating supplier: ${supplier.supplierName}")
            val result = apiService.createSupplier(supplier)
            android.util.Log.d("SupplierRepo", "✅ Created successfully: ID ${result.supplierId}")
            result
        } catch (e: Exception) {
            android.util.Log.e("SupplierRepo", "❌ Error creating supplier: ${e.message}", e)
            null
        }
    }

    suspend fun updateSupplier(id: Long, supplier: Supplier): Supplier? {
        return try {
            apiService.updateSupplier(id, supplier)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deactivateSupplier(id: Long): Boolean {
        return try {
            apiService.deactivateSupplier(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun reactivateSupplier(id: Long): Boolean {
        return try {
            apiService.reactivateSupplier(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteSupplier(id: Long): Boolean {
        return try {
            apiService.deleteSupplier(id)
            true
        } catch (e: Exception) {
            false
        }
    }

}
