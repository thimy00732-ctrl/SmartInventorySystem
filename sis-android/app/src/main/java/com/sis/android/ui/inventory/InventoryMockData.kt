package com.sis.android.ui.inventory

import com.sis.android.data.model.ExpiryStatus
import com.sis.android.data.model.InventoryItem
import com.sis.android.data.model.InventoryProductDetails
import com.sis.android.data.model.InventoryThresholdProduct
import com.sis.android.data.model.InventoryValuationRow
import java.math.BigDecimal

/*
 * Dữ liệu mẫu cho màn hình quản lý tồn kho giai đoạn 1.
 * Mỗi mục mô phỏng một dòng tồn kho theo từng kho, batch và trạng thái hết hạn
 * để giao diện có thể kiểm thử mà không phụ thuộc API thật.
 */
object InventoryMockData {
    private val productDetailsById = mapOf(
        101L to InventoryProductDetails("8934673120012", "Sữa và chế phẩm", "Công ty Vinamilk", true, 100, 2000, BigDecimal("12500.00")),
        102L to InventoryProductDetails("8936036020021", "Hàng gia dụng", "Công ty P&G", true, 50, 1000, BigDecimal("85000.00")),
        103L to InventoryProductDetails("8938501670038", "Gạo và ngũ cốc", "Nông sản Việt", true, 100, 500, BigDecimal("90000.00")),
        104L to InventoryProductDetails("8936012340044", "Bánh kẹo", "Thực phẩm Kinh Đô", true, 30, 30, BigDecimal("45000.00")),
        105L to InventoryProductDetails("8934588010059", "Đồ uống", "Công ty Coca-Cola", true, 100, 2500, BigDecimal("120000.00")),
        106L to InventoryProductDetails("8934567890065", "Thực phẩm đóng hộp", "Thực phẩm Việt", true, 20, 300, BigDecimal("18000.00")),
        107L to InventoryProductDetails("8934678900071", "Mì và thực phẩm khô", "Acecook Việt Nam", true, 100, 1500, BigDecimal("65000.00")),
        108L to InventoryProductDetails("8934567890088", "Hàng gia dụng", "Công ty Unilever", true, 50, 200, BigDecimal("22000.00")),
        109L to InventoryProductDetails("8934567890095", "Cà phê và trà", "Cà phê Việt", true, 60, 600, BigDecimal("150000.00")),
        110L to InventoryProductDetails("8934567890101", "Giấy và vệ sinh", "Giấy Sài Gòn", true, 1800, 2000, BigDecimal("8000.00")),
        111L to InventoryProductDetails("8934567890118", "Dầu ăn và gia vị", "Dầu thực vật Việt Nam", true, 50, 800, BigDecimal("45000.00")),
        112L to InventoryProductDetails("8934567890125", "Sữa và chế phẩm", "Công ty Vinamilk", true, 60, 500, BigDecimal("12000.00"))
    )

    fun productDetails(productId: Long): InventoryProductDetails =
        productDetailsById[productId] ?: InventoryProductDetails()

    fun thresholdProducts(items: List<InventoryItem> = data()): List<InventoryThresholdProduct> =
        items.distinctBy { it.productId }.map { item ->
            val details = productDetails(item.productId)
            InventoryThresholdProduct(
                productId = item.productId,
                sku = item.sku,
                productName = item.productName,
                unit = item.unit,
                categoryName = details.categoryName ?: "Chưa phân loại",
                supplierName = details.supplierName ?: "Chưa có",
                totalStock = items.filter { it.productId == item.productId }.sumOf { it.stockQuantity },
                minStock = details.minStock,
                maxStock = details.maxStock,
                isActive = details.isActive
            )
        }

    fun valuationRows(items: List<InventoryItem> = data()): List<InventoryValuationRow> =
        items.filter { it.stockQuantity > 0 }
            .groupBy { it.warehouseId to it.productId }
            .values
            .map { stockRows ->
                val item = stockRows.first()
                val details = productDetails(item.productId)
                InventoryValuationRow(
                    warehouseId = item.warehouseId,
                    warehouseName = item.warehouseName,
                    productId = item.productId,
                    sku = item.sku,
                    productName = item.productName,
                    categoryName = details.categoryName ?: "Chưa phân loại",
                    totalQuantity = stockRows.sumOf { it.stockQuantity },
                    currentCostPrice = details.costPrice
                )
            }
            .sortedByDescending { it.stockValue }

    fun data(): List<InventoryItem> = listOf(
        InventoryItem(
            inventoryId = 1L,
            productId = 101L,
            sku = "SP-001",
            productName = "Sữa tươi Vinamilk",
            unit = "Hộp",
            warehouseId = 1L,
            warehouseName = "Kho trung tâm",
            batchCode = "B001",
            expiryDate = "2026-12-31",
            manufactureDate = "2026-09-01",
            updatedAt = "2026-11-15T10:30:00",
            expiryStatus = ExpiryStatus.OK,
            stockQuantity = 1250,
            reservedQuantity = 180,
            availableQuantity = 1070,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 2L,
            productId = 102L,
            sku = "SP-002",
            productName = "Bột giặt Ariel",
            unit = "Hộp",
            warehouseId = 2L,
            warehouseName = "Kho miền Nam",
            batchCode = "",
            noBatch = true,
            expiryDate = null,
            manufactureDate = null,
            updatedAt = "2026-11-14T15:20:00",
            expiryStatus = ExpiryStatus.NONE,
            stockQuantity = 440,
            reservedQuantity = 60,
            availableQuantity = 380,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 3L,
            productId = 103L,
            sku = "SP-003",
            productName = "Gạo thơm 5kg",
            unit = "Bao",
            warehouseId = 1L,
            warehouseName = "Kho trung tâm",
            batchCode = "B210",
            expiryDate = "2026-11-18",
            manufactureDate = "2026-08-18",
            updatedAt = "2026-11-15T09:10:00",
            expiryStatus = ExpiryStatus.NEAR_EXPIRY,
            stockQuantity = 86,
            reservedQuantity = 12,
            availableQuantity = 74,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 4L,
            productId = 104L,
            sku = "SP-004",
            productName = "Bánh quy sữa",
            unit = "Thùng",
            warehouseId = 3L,
            warehouseName = "Kho miền Bắc",
            batchCode = "B555",
            expiryDate = "2026-08-15",
            manufactureDate = "2026-02-15",
            updatedAt = "2026-11-13T16:40:00",
            expiryStatus = ExpiryStatus.EXPIRED,
            stockQuantity = 32,
            reservedQuantity = 0,
            availableQuantity = 32,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 5L,
            productId = 105L,
            sku = "SP-005",
            productName = "Nước ngọt Coca",
            unit = "Thùng",
            warehouseId = 2L,
            warehouseName = "Kho miền Nam",
            batchCode = "B170",
            expiryDate = "2027-03-27",
            manufactureDate = "2026-09-27",
            updatedAt = "2026-11-12T11:25:00",
            expiryStatus = ExpiryStatus.OK,
            stockQuantity = 2100,
            reservedQuantity = 500,
            availableQuantity = 1600,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 6L,
            productId = 106L,
            sku = "SP-006",
            productName = "Đồ ăn đóng hộp",
            unit = "Hộp",
            warehouseId = 1L,
            warehouseName = "Kho trung tâm",
            batchCode = "",
            noBatch = true,
            expiryDate = "2026-10-01",
            manufactureDate = "2026-04-01",
            updatedAt = "2026-11-11T08:45:00",
            expiryStatus = ExpiryStatus.NEAR_EXPIRY,
            stockQuantity = 0,
            reservedQuantity = 0,
            availableQuantity = 0,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 7L,
            productId = 107L,
            sku = "SP-007",
            productName = "Mì gói Hảo Hảo",
            unit = "Thùng",
            warehouseId = 4L,
            warehouseName = "Kho vận chuyển",
            batchCode = "B310",
            expiryDate = "2027-05-10",
            manufactureDate = "2026-10-10",
            updatedAt = "2026-11-10T14:05:00",
            expiryStatus = ExpiryStatus.OK,
            stockQuantity = 980,
            reservedQuantity = 220,
            availableQuantity = 760,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 8L,
            productId = 108L,
            sku = "SP-008",
            productName = "Nước rửa chén Sunlight",
            unit = "Chai",
            warehouseId = 3L,
            warehouseName = "Kho miền Bắc",
            batchCode = "B702",
            expiryDate = null,
            manufactureDate = null,
            updatedAt = "2026-11-09T12:15:00",
            expiryStatus = ExpiryStatus.NONE,
            stockQuantity = 330,
            reservedQuantity = 0,
            availableQuantity = 330,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 9L,
            productId = 109L,
            sku = "SP-009",
            productName = "Cà phê hòa tan",
            unit = "Hộp",
            warehouseId = 2L,
            warehouseName = "Kho miền Nam",
            batchCode = "B405",
            expiryDate = "2026-09-30",
            manufactureDate = "2026-03-30",
            updatedAt = "2026-11-08T17:35:00",
            expiryStatus = ExpiryStatus.EXPIRED,
            stockQuantity = 54,
            reservedQuantity = 10,
            availableQuantity = 44,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 10L,
            productId = 110L,
            sku = "SP-010",
            productName = "Khăn giấy lau nhà",
            unit = "Cuộn",
            warehouseId = 4L,
            warehouseName = "Kho vận chuyển",
            batchCode = "",
            noBatch = true,
            expiryDate = null,
            manufactureDate = null,
            updatedAt = "2026-11-07T10:00:00",
            expiryStatus = ExpiryStatus.NONE,
            stockQuantity = 1600,
            reservedQuantity = 300,
            availableQuantity = 1300,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 11L,
            productId = 111L,
            sku = "SP-011",
            productName = "Dầu ăn Neptune",
            unit = "Can",
            warehouseId = 1L,
            warehouseName = "Kho trung tâm",
            batchCode = "B118",
            expiryDate = "2027-08-12",
            manufactureDate = "2026-10-12",
            updatedAt = "2026-11-06T09:50:00",
            expiryStatus = ExpiryStatus.OK,
            stockQuantity = 210,
            reservedQuantity = 50,
            availableQuantity = 160,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 12L,
            productId = 112L,
            sku = "SP-012",
            productName = "Sữa chua ăn dặm",
            unit = "Hộp",
            warehouseId = 3L,
            warehouseName = "Kho miền Bắc",
            batchCode = "B284",
            expiryDate = "2026-11-25",
            manufactureDate = "2026-08-25",
            updatedAt = "2026-11-05T13:30:00",
            expiryStatus = ExpiryStatus.NEAR_EXPIRY,
            stockQuantity = 75,
            reservedQuantity = 20,
            availableQuantity = 55,
            imageUrl = null
        ),
        InventoryItem(
            inventoryId = 13L,
            productId = 101L,
            sku = "SP-001",
            productName = "Sữa tươi Vinamilk",
            unit = "Hộp",
            warehouseId = 2L,
            warehouseName = "Kho miền Nam",
            batchCode = "B009",
            expiryDate = "2027-01-15",
            manufactureDate = "2026-10-15",
            updatedAt = "2026-11-15T11:00:00",
            expiryStatus = ExpiryStatus.OK,
            stockQuantity = 300,
            reservedQuantity = 45,
            availableQuantity = 255,
            imageUrl = null
        )
    )
}
