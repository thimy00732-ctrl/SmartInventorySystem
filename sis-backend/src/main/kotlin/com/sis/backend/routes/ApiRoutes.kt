package com.sis.backend.routes

/**
 * Constants đường dẫn API (Sprint 2).
 * 
 * GOM MỌI ENDPOINT Ở MỘT CHỖ để dễ quản lý và tránh typo.
 * Dùng trong route definition và test.
 */
object ApiRoutes {
    // ===== Inventory (UC29-32) =====
    const val INVENTORY = "/api/inventory"
    const val INVENTORY_BY_ID = "/api/inventory/{id}"
    const val INVENTORY_BY_PRODUCT = "/api/inventory/by-product/{productId}"
    const val INVENTORY_RESERVED = "/api/inventory/{id}/reserved"
    
    // ===== Products - Thresholds (UC33) =====
    // Chú ý: /thresholds phải đặt TRƯỚC /{id} trong route definition
    const val PRODUCTS_THRESHOLDS = "/api/products/thresholds"
    const val PRODUCTS_THRESHOLDS_BY_ID = "/api/products/{id}/thresholds"
    
    // ===== Alerts (UC34, UC35) =====
    const val ALERTS_LOW_STOCK = "/api/inventory/alerts/low-stock"
    const val ALERTS_OVER_STOCK = "/api/inventory/alerts/over-stock"
    const val ALERTS_SUMMARY = "/api/inventory/alerts/summary"
    
    // ===== Stock Ledger (UC36) =====
    const val STOCK_LEDGER = "/api/stock-ledger"
    
    // ===== Transactions (UC37, UC38) =====
    const val TRANSACTION_MOVEMENTS = "/api/transactions/movements"
    const val TRANSACTION_BY_ID = "/api/transactions/{id}"
    
    // ===== Valuation (UC39) =====
    const val VALUATION = "/api/inventory/valuation"
    const val VALUATION_SUMMARY = "/api/inventory/valuation/summary"
}
