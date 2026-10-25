package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.model.Product
import com.sis.android.ui.products.ProductDetailScreen
import com.sis.android.ui.products.ProductFormScreen
import com.sis.android.ui.products.ProductListScreen
import com.sis.android.ui.viewmodel.ProductViewModel
import com.sis.android.ui.theme.SISTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SISTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ProductViewModel = viewModel()

                    // Quản lý trạng thái màn hình hiện tại: "LIST", "DETAIL", "FORM"
                    var currentScreen by remember { mutableStateOf("LIST") }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) }
                    var productToEdit by remember { mutableStateOf<Product?>(null) }

                    when (currentScreen) {
                        "LIST" -> {
                            ProductListScreen(
                                viewModel = viewModel,
                                onProductClick = { product ->
                                    selectedProduct = product
                                    currentScreen = "DETAIL"
                                },
                                onAddClick = {
                                    productToEdit = null // Null nghĩa là Thêm mới
                                    currentScreen = "FORM"
                                }
                            )
                        }
                        "DETAIL" -> {
                            if (selectedProduct != null) {
                                ProductDetailScreen(
                                    product = selectedProduct!!,
                                    onBackClick = { currentScreen = "LIST" },
                                    onEditClick = { product ->
                                        productToEdit = product
                                        currentScreen = "FORM"
                                    },
                                    onDeleteClick = { product ->
                                        // Thay product.id thành tên trường ID thực tế trong model Product của bạn (ví dụ: productId)
                                        viewModel.deleteProduct(product.productId)

                                        // Quay lại màn hình danh sách
                                        currentScreen = "LIST"
                                    }
                                )
                            }
                        }
                        "FORM" -> {
                            ProductFormScreen(
                                productToEdit = productToEdit,
                                onSave = { savedProduct ->
                                    if (productToEdit == null) {
                                        // Gọi ViewModel để thêm sản phẩm mới
                                        viewModel.addProduct(savedProduct)
                                    } else {
                                        // Gọi ViewModel để cập nhật sản phẩm
                                        viewModel.updateProduct(savedProduct)
                                    }
                                    // Quay lại màn hình danh sách sau khi lưu
                                    currentScreen = "LIST"
                                },
                                onBackClick = { currentScreen = "LIST" }
                            )
                        }
                    }
                }
            }
        }
    }
}