
package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.sis.android.ui.inventory.InventoryScreen
import com.sis.android.ui.inventory.InventoryUiItem

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    color = Color(0xFFEAF0F5)
                ) {
                    InventoryScreen(
                        isAdmin = true,
                        inventoryList = sampleInventory
                    )
                }
            }
        }
    }
}

private val sampleInventory = listOf(

    InventoryUiItem(
        inventoryId = 1,
        productId = 1,
        sku = "SP001",
        productName = "Bàn phím cơ",
        categoryName = "Điện tử",
        stockQuantity = 100,
        minStock = 10,
        maxStock = 500
    ),

    InventoryUiItem(
        inventoryId = 2,
        productId = 2,
        sku = "SP002",
        productName = "Chuột không dây",
        categoryName = "Phụ kiện",
        stockQuantity = 200,
        minStock = 20,
        maxStock = 600
    ),

    InventoryUiItem(
        inventoryId = 3,
        productId = 3,
        sku = "SP003",
        productName = "Giấy A4",
        categoryName = "Văn phòng phẩm",
        stockQuantity = 300,
        minStock = 50,
        maxStock = 1000
    ),

    InventoryUiItem(
        inventoryId = 4,
        productId = 4,
        sku = "SP004",
        productName = "USB 64GB",
        categoryName = "Phụ kiện",
        stockQuantity = 150,
        minStock = 15,
        maxStock = 400
    ),

    InventoryUiItem(
        inventoryId = 5,
        productId = 5,
        sku = "SP005",
        productName = "Tai nghe",
        categoryName = "Điện tử",
        stockQuantity = 80,
        minStock = 10,
        maxStock = 300
    ),

    InventoryUiItem(
        inventoryId = 6,
        productId = 6,
        sku = "SP006",
        productName = "Dây điện",
        categoryName = "Dụng cụ cơ khí",
        stockQuantity = 120,
        minStock = 10,
        maxStock = 200
    ),

    InventoryUiItem(
        inventoryId = 7,
        productId = 7,
        sku = "SP007",
        productName = "Nước suối",
        categoryName = "Đồ uống",
        stockQuantity = 250,
        minStock = 30,
        maxStock = 800
    ),

    InventoryUiItem(
        inventoryId = 8,
        productId = 8,
        sku = "SP008",
        productName = "Găng tay bảo hộ",
        categoryName = "Bảo hộ lao động",
        stockQuantity = 400,
        minStock = 50,
        maxStock = 1000
    ),

    InventoryUiItem(
        inventoryId = 9,
        productId = 9,
        sku = "SP009",
        productName = "Nước lau sàn",
        categoryName = "Dụng cụ vệ sinh",
        stockQuantity = 180,
        minStock = 20,
        maxStock = 500
    ),

    InventoryUiItem(
        inventoryId = 10,
        productId = 10,
        sku = "SP010",
        productName = "Ổ cắm điện",
        categoryName = "Đồ gia dụng",
        stockQuantity = 90,
        minStock = 15,
        maxStock = 300
    )
)
