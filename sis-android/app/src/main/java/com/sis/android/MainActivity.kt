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

                    val categories by categoryViewModel.categoryList.collectAsState()

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
                                        // Thêm mới sản phẩm (Dùng đúng tham số 'product = savedProduct')
                                        isSaving = true
                                        viewModel.addProduct(
                                            product = savedProduct,
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
                                            updated = savedProduct,
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
                                        viewModel.deleteProduct(deleteProd.productId)
                                        currentScreen = "LIST"
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