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
import com.sis.android.ui.products.BarcodeScannerScreen
import com.sis.android.ui.products.ProductDetailScreen
import com.sis.android.ui.products.ProductFormScreen
import com.sis.android.ui.products.ProductListScreen
import com.sis.android.ui.theme.SISTheme
import com.sis.android.ui.viewmodel.ProductViewModel
import androidx.activity.enableEdgeToEdge
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SISTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ProductViewModel = viewModel()
                    // Lấy danh sách sản phẩm thông qua UIState mới của ViewModel
                    val uiState by viewModel.uiState.collectAsState()

                    var currentScreen by remember { mutableStateOf("LIST") }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) }
                    var productToEdit by remember { mutableStateOf<Product?>(null) }
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
                                    currentScreen = "SCANNER"
                                }
                            )
                        }
                        "SCANNER" -> {
                            // Lấy danh sách sản phẩm từ UiState nếu thành công để quét mã
                            val productList = if (uiState is com.sis.android.ui.viewmodel.ProductUiState.Success) {
                                (uiState as com.sis.android.ui.viewmodel.ProductUiState.Success).products
                            } else {
                                emptyList()
                            }

                            BarcodeScannerScreen(
                                products = productList,
                                onProductFound = { foundProduct ->
                                    selectedProduct = foundProduct
                                    currentScreen = "DETAIL"
                                },
                                onBarcodeNotFound = { scannedCode ->
                                    notFoundBarcode = scannedCode
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
                                    },
                                    onToggleActiveClick = { toggleProd ->
                                        // Đảo ngược trạng thái kinh doanh: 1 thành 0 (ngừng kinh doanh) và ngược lại
                                        val newActiveStatus = if (toggleProd.isActive == 1) 0 else 1
                                        val updated = toggleProd.copy(isActive = newActiveStatus)
                                        viewModel.updateProduct(updated)
                                        selectedProduct = updated // Cập nhật lại state chi tiết đang xem
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

                    // Hộp thoại cảnh báo khi quét mã không có trong hệ thống
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