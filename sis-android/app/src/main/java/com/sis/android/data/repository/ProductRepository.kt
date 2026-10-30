package com.sis.android.data.repository

import com.sis.android.data.model.Product
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class ProductRepository {

    private val mockProducts = listOf(
        Product(
            productId = 1L,
            sku = "SP001",
            barcode = "893000000001",
            productName = "Nước suối 500ml",
            description = "Nước uống đóng chai tinh khiết, thùng 24 chai.",
            unit = "chai",
            costPrice = 4000.0,
            salePrice = 6000.0,
            minStock = 20.0,
            isActive = 1,
            stockQuantity = 22.0,
            reservedQuantity = 4.0,
            availableQuantity = 18.0,
            warehouseName = "Kho Trung tâm",
            categoryName = "Đồ uống"
        ),
        Product(
            productId = 2L,
            sku = "SP002",
            barcode = "893000000002",
            productName = "Mì ăn liền",
            description = "Mì tôm chua cay thùng 30 gói.",
            unit = "thùng",
            costPrice = 80000.0,
            salePrice = 95000.0,
            minStock = 10.0,
            isActive = 1,
            stockQuantity = 130.0,
            reservedQuantity = 10.0,
            availableQuantity = 120.0,
            warehouseName = "Kho Trung tâm",
            categoryName = "Thực phẩm đóng gói"
        ),
        Product(
            productId = 3L,
            sku = "SP003",
            barcode = "893000000003",
            productName = "Dầu ăn 1L",
            description = "Dầu thực vật cao cấp chiết xuất tự nhiên.",
            unit = "chai",
            costPrice = 42000.0,
            salePrice = 52000.0,
            minStock = 15.0,
            isActive = 1,
            stockQuantity = 68.0,
            reservedQuantity = 4.0,
            availableQuantity = 64.0,
            warehouseName = "Kho Trung tâm",
            categoryName = "Gia vị"
        ),
        Product(
            productId = 4L,
            sku = "SP005",
            barcode = "893000000005",
            productName = "Bánh quy",
            description = "Bánh quy bơ hộp thiếc cao cấp.",
            unit = "hộp",
            costPrice = 30000.0,
            salePrice = 45000.0,
            minStock = 10.0,
            isActive = 1,
            stockQuantity = 9.0,
            reservedQuantity = 0.0,
            availableQuantity = 9.0,
            warehouseName = "Kho Trung tâm",
            categoryName = "Bánh kẹo"
        )
    )

    suspend fun getProducts(simulateEmpty: Boolean = false, simulateError: Boolean = false): List<Product> {
        delay(1000.milliseconds) // Cập nhật sang Kotlin Duration API
        if (simulateError) throw Exception("Lỗi kết nối cơ sở dữ liệu Ktor / MySQL (500)")
        if (simulateEmpty) return emptyList()
        return mockProducts
    }

    suspend fun getProductById(productId: Long): Product? {
        delay(300.milliseconds)
        return mockProducts.find { it.productId == productId }
    }
}