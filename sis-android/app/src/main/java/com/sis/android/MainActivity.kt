package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.model.Product
import com.sis.android.ui.products.BarcodeScannerScreen // Import màn hình Scanner vừa tạo
import com.sis.android.ui.products.ProductDetailScreen
import com.sis.android.ui.products.ProductFormScreen
import com.sis.android.ui.products.ProductListScreen
import com.sis.android.ui.theme.SISTheme
import com.sis.android.ui.viewmodel.ProductViewModel

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
                    val products by viewModel.products.collectAsState()

                    // Thêm "SCANNER" vào danh sách trạng thái màn hình
                    var currentScreen by remember { mutableStateOf("LIST") }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) }
                    var productToEdit by remember { mutableStateOf<Product?>(null) }

                    // Biến lưu mã barcode không tìm thấy để hiển thị thông báo
                    var notFoundBarcode by remember { mutableStateOf<String?>(null) }

                    when (currentScreen) {
                        "LIST" -> {
                            ProductListScreen(
                                viewModel = viewModel,
                                onProductClick = { product ->
                                    selectedProduct = product
                                    currentScreen = "DETAIL"
                                },
                                onAddClick = {
                                    productToEdit = null
                                    currentScreen = "FORM"
                                },
                                onScanClick = {
                                    currentScreen = "SCANNER" // Chuyển sang màn hình quét mã
                                }
                            )
                        }
                        "SCANNER" -> {
                            BarcodeScannerScreen(
                                products = products,
                                onProductFound = { foundProduct ->
                                    selectedProduct = foundProduct
                                    currentScreen = "DETAIL" // Tìm thấy thì mở chi tiết sản phẩm
                                },
                                onBarcodeNotFound = { scannedCode ->
                                    notFoundBarcode = scannedCode // Kích hoạt hiện thông báo lỗi
                                },
                                onBackClick = { currentScreen = "LIST" }
                            )
                        }
                        "DETAIL" -> {
                            selectedProduct?.let { product ->
                                ProductDetailScreen(
                                    product = product,
                                    onBackClick = { currentScreen = "LIST" },
                                    onEditClick = { editProd ->
                                        productToEdit = editProd
                                        currentScreen = "FORM"
                                    },
                                    onDeleteClick = { deleteProd ->
                                        viewModel.deleteProduct(deleteProd.productId)
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
                                        viewModel.addProduct(savedProduct)
                                    } else {
                                        viewModel.updateProduct(savedProduct)
                                    }
                                    currentScreen = "LIST"
                                },
                                onBackClick = { currentScreen = "LIST" }
                            )
                        }
                    }

                    // Hộp thoại cảnh báo khi quét trúng mã vạch nhưng không có trong cơ sở dữ liệu
                    notFoundBarcode?.let { code ->
                        AlertDialog(
                            onDismissRequest = { notFoundBarcode = null },
                            title = { Text("Không tìm thấy sản phẩm") },
                            text = { Text("Mã vạch hoặc SKU [$code] không tồn tại trong hệ thống kho!") },
                            confirmButton = {
                                TextButton(onClick = { notFoundBarcode = null }) {
                                    Text("Đóng")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}