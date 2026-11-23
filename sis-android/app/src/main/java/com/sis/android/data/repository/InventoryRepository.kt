package com.sis.android.data.repository

import com.sis.android.data.model.*
import com.sis.android.data.remote.ApiService

/**
 * Repository for Inventory Module (UC29-UC39)
 * Handles all API calls related to inventory management, alerts, thresholds, stock ledger, and valuation.
 */
class InventoryRepository(
    private val apiService: ApiService
) {
    
    // ==================== UC29-UC32: Inventory Management ====================
    
    /**
     * UC29: Get inventory list with filters and pagination
     */
    suspend fun getInventory(
        keyword: String? = null,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        supplierId: Long? = null,
        stockStatus: String? = null,
        sortBy: String? = "product_name",
        sortDirection: String? = "ASC",
        page: Int = 1,
        size: Int = 10
    ): InventoryPageResponse {
        return try {
            apiService.getInventory(
                keyword = keyword,
                warehouseId = warehouseId,
                categoryId = categoryId,
                supplierId = supplierId,
                stockStatus = stockStatus,
                sortBy = sortBy,
                sortDirection = sortDirection,
                page = page,
                size = size
            )
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getInventory: ${e.message}", e)
            InventoryPageResponse(emptyList(), page, size, 0, 0)
        }
    }
    
    /**
     * UC30: Get inventory detail by ID
     */
    suspend fun getInventoryDetail(inventoryId: Long): InventoryItem? {
        return try {
            apiService.getInventoryDetail(inventoryId)
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getInventoryDetail: ${e.message}", e)
            null
        }
    }
    
    /**
     * UC30: Get inventory by product ID
     */
    suspend fun getInventoryByProduct(productId: Long): List<InventoryItem> {
        return try {
            apiService.getInventoryByProduct(productId)
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getInventoryByProduct: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * UC31: Update reserved quantity
     */
    suspend fun updateReservedQuantity(inventoryId: Long, reservedQuantity: Int): InventoryItem? {
        return try {
            apiService.updateReservedQuantity(inventoryId, mapOf("reservedQuantity" to reservedQuantity))
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error updateReservedQuantity: ${e.message}", e)
            null
        }
    }
    
    // ==================== UC34-UC35: Alerts ====================
    
    /**
     * UC34: Get low stock alerts
     */
    suspend fun getLowStockAlerts(): List<LowStockAlert> {
        return try {
            apiService.getLowStockAlerts()
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getLowStockAlerts: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * UC35: Get over stock alerts
     */
    suspend fun getOverStockAlerts(): List<OverStockAlert> {
        return try {
            apiService.getOverStockAlerts()
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getOverStockAlerts: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * UC34-UC35: Get alert summary
     */
    suspend fun getAlertSummary(): AlertSummary? {
        return try {
            apiService.getAlertSummary()
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getAlertSummary: ${e.message}", e)
            null
        }
    }
    
    // ==================== UC33: Thresholds ====================
    
    /**
     * UC33: Get product thresholds
     */
    suspend fun getProductThresholds(warehouseId: Long? = null): List<ThresholdProduct> {
        return try {
            apiService.getProductThresholds(warehouseId)
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getProductThresholds: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * UC33: Update product thresholds
     */
    suspend fun updateProductThresholds(
        productId: Long,
        minStock: Int,
        maxStock: Int
    ): ThresholdProduct? {
        return try {
            val request = UpdateThresholdRequest(minStock, maxStock)
            apiService.updateProductThresholds(productId, request)
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error updateProductThresholds: ${e.message}", e)
            null
        }
    }
    
    // ==================== UC36: Stock Ledger ====================
    
    /**
     * UC36: Get stock ledger
     */
    suspend fun getStockLedger(
        fromDate: String,
        toDate: String,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): StockLedgerResponse? {
        return try {
            apiService.getStockLedger(
                fromDate = fromDate,
                toDate = toDate,
                warehouseId = warehouseId,
                categoryId = categoryId,
                keyword = keyword,
                page = page,
                size = size
            )
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getStockLedger: ${e.message}", e)
            null
        }
    }
    
    // ==================== UC39: Valuation ====================
    
    /**
     * UC39: Get inventory valuation
     */
    suspend fun getValuation(
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): ValuationResponse? {
        return try {
            apiService.getValuation(
                warehouseId = warehouseId,
                categoryId = categoryId,
                keyword = keyword,
                page = page,
                size = size
            )
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getValuation: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get filter options for inventory screens
     */
    suspend fun getInventoryFilterOptions(): ProductFilterOptions? {
        return try {
            val response = apiService.getInventoryFilterOptions()
            if (response.isSuccessful) {
                response.body()
            } else {
                android.util.Log.e("InventoryRepo", "❌ Error getInventoryFilterOptions: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepo", "❌ Error getInventoryFilterOptions: ${e.message}", e)
            null
        }
    }
}
