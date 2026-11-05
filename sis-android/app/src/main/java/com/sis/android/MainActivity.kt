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
import com.sis.android.ui.viewmodel.CategoryViewModel
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
                    val categoryViewModel: CategoryViewModel = viewModel()

                    // Lấy danh sách sản phẩm thông qua UIState mới của ViewModel
                    val uiState by viewModel.uiState.collectAsState()
                    val categories by categoryViewModel.categoryList.collectAsState()

                    // Load categories khi khởi động
                    LaunchedEffect(Unit) {
                        categoryViewModel.loadCategories()
                    }

                    var isSaving by remember { mutableStateOf(false) }
                    var formError by remember { mutableStateOf<String?>(null) }
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
                        "FORM" -> {
                            ProductFormScreen(
                                productToEdit = productToEdit,
                                categories = categories,
                                isSaving = isSaving,
                                serverError = formError,
                                onSave = { savedProduct ->
                                    formError = null
                                    if (productToEdit == null) {
                                        // Thêm mới sản phẩm (Truyền trực tiếp savedProduct hoặc đổi tên tham số cho khớp ViewModel)
                                        isSaving = true
                                        viewModel.addProduct(
                                            savedProduct, // 👈 Truyền trực tiếp đối tượng product không cần tên tham số `updated =`
                                            onSuccess = {
                                                isSaving = false
                                                currentScreen = "LIST"
                                            },
                                            onError = { errorMsg ->
                                                isSaving = false
                                                formError = errorMsg
                                            }
                                        )
                                    } else {
                                        // Cập nhật sản phẩm
                                        isSaving = true
                                        viewModel.updateProduct(
                                            savedProduct, // 👈 Tương tự, truyền trực tiếp đối tượng product
                                            onSuccess = {
                                                isSaving = false
                                                currentScreen = "LIST"
                                            },
                                            onError = { errorMsg ->
                                                isSaving = false
                                                formError = errorMsg
                                            }
                                        )
                                    }
                                },
                                onBackClick = {
                                    formError = null
                                    currentScreen = "LIST"
                                }
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
                                        // 🛠️ Bổ sung đầy đủ tham số onSuccess và onError cho hàm deleteProduct
                                        viewModel.deleteProduct(
                                            productId = deleteProd.productId,
                                            onSuccess = {
                                                currentScreen = "LIST"
                                            },
                                            onError = { errorMsg ->
                                                android.util.Log.e("MainActivity", "Lỗi xóa: $errorMsg")
                                            }
                                        )
                                    },
                                    onToggleActiveClick = { toggleProd ->
                                        viewModel.toggleProductActive(
                                            product = toggleProd,
                                            onSuccess = {
                                                selectedProduct = toggleProd.copy(isActive = !toggleProd.isActive)
                                            },
                                            onError = { errorMsg ->
                                                android.util.Log.e("MainActivity", "Error: $errorMsg")
                                            }
                                        )
                                    }
                                )
                            }
                        }
                        "SCANNER" -> {
                            BarcodeScannerScreen(
                                onBarcodeScanned = { code ->
                                    viewModel.scanBarcode(
                                        barcode = code,
                                        onFound = { product ->
                                            selectedProduct = product
                                            currentScreen = "DETAIL"
                                        },
                                        onNotFound = { notFoundCode ->
                                            notFoundBarcode = notFoundCode
                                        }
                                    )
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