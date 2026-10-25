package com.sis.android.data.repository

import com.sis.android.data.model.Product
import kotlinx.coroutines.delay

class ProductRepository {

    // Dữ liệu giả lập khớp hoàn toàn với cấu trúc bảng products trong Database
    private val mockProducts = listOf(
        Product(
            productId = 1L,
            sku = "SP001",
            barcode = "8934602001001",
            productName = "Sữa tươi tiệt trùng Vinamilk 1L",
            description = "Sữa tươi 100% sữa bò tươi nguyên chất, hộp 1 lít.",
            unit = "Hộp",
            minStock = 10.0,
            status = "ACTIVE"
        ),
        Product(
            productId = 2L,
            sku = "SP002",
            barcode = "8934602002002",
            productName = "Gạo thơm lài hạt ngọc trời 5kg",
            description = "Gạo đặc sản thượng hạng, dẻo mềm, thơm tự nhiên.",
            unit = "Túi",
            minStock = 5.0,
            status = "ACTIVE"
        ),
        Product(
            productId = 3L,
            sku = "SP003",
            barcode = "8934602003003",
            productName = "Nước giặt OMO Matic hương hoa lịch lãm 3.6kg",
            description = "Đánh bật vết bẩn cứng đầu, lưu hương dài lâu.",
            unit = "Can",
            minStock = 8.0,
            status = "ACTIVE"
        ),
        Product(
            productId = 4L,
            sku = "SP004",
            barcode = "8934602004004",
            productName = "Mì Hảo Hảo chua cay (Thùng 30 gói)",
            description = "Mì ăn liền hương vị tôm chua cay quốc dân.",
            unit = "Thùng",
            minStock = 15.0,
            status = "ACTIVE"
        )
    )

    suspend fun getProducts(simulateEmpty: Boolean = false, simulateError: Boolean = false): List<Product> {
        delay(1200L) // Giả lập độ trễ mạng

        if (simulateError) {
            throw Exception("Không thể kết nối đến Server Ktor (Lỗi 500)")
        }

        if (simulateEmpty) {
            return emptyList()
        }

        return mockProducts
    }

    suspend fun getProductById(productId: Long): Product? {
        delay(500L)
        return mockProducts.find { it.productId == productId }
    }
}