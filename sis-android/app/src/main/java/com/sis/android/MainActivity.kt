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
import com.sis.android.data.model.Category
import com.sis.android.data.model.Supplier
import com.sis.android.ui.products.BarcodeScannerScreen
import com.sis.android.ui.products.ProductDetailScreen
import com.sis.android.ui.products.ProductFormScreen
import com.sis.android.ui.products.ProductListScreen
import com.sis.android.ui.categories.CategoryListScreen
import com.sis.android.ui.categories.CategoryDetailScreen
import com.sis.android.ui.categories.CategoryFormScreen
import com.sis.android.ui.suppliers.SupplierListScreen
import com.sis.android.ui.suppliers.SupplierDetailScreen
import com.sis.android.ui.suppliers.SupplierFormScreen
import com.sis.android.ui.theme.SISTheme
import com.sis.android.ui.viewmodel.ProductViewModel
import com.sis.android.ui.viewmodel.CategoryViewModel
import com.sis.android.ui.viewmodel.SupplierViewModel
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
                    val supplierViewModel: SupplierViewModel = viewModel()

                    val categories by categoryViewModel.categoryList.collectAsState()

                    LaunchedEffect(Unit) {
                        categoryViewModel.loadCategories()
                    }

                    var isSaving by remember { mutableStateOf(false) }
                    var formError by remember { mutableStateOf<String?>(null) }

                    var currentScreen by remember { mutableStateOf("LIST") }
                   //Thay đổi màn hình để test
                    // Nhà cung cấp (Suppliers)
                    //var currentScreen by remember { mutableStateOf("SUPPLIERS") }

                    // Danh mục (Categories)
                    //var currentScreen by remember { mutableStateOf("CATEGORIES") }




                    var selectedProduct by remember { mutableStateOf<Product?>(null) }
                    var productToEdit by remember { mutableStateOf<Product?>(null) }
                    var notFoundBarcode by remember { mutableStateOf<String?>(null) }

                    var selectedCategory by remember { mutableStateOf<Category?>(null) }
                    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

                    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
                    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }

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
                                        isSaving = true
                                        viewModel.addProduct(
                                            savedProduct,
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
                                        isSaving = true
                                        viewModel.updateProduct(
                                            savedProduct,
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

                        "CATEGORIES" -> {
                            CategoryListScreen(
                                viewModel = categoryViewModel,
                                onCategoryClick = { category ->
                                    selectedCategory = category
                                    currentScreen = "CATEGORY_DETAIL"
                                },
                                onAddClick = {
                                    categoryToEdit = null
                                    currentScreen = "CATEGORY_FORM"
                                }
                            )
                        }

                        "CATEGORY_DETAIL" -> {
                            selectedCategory?.let { category ->
                                CategoryDetailScreen(
                                    category = category,
                                    onBackClick = { currentScreen = "CATEGORIES" },
                                    onEditClick = { editCat ->
                                        categoryToEdit = editCat
                                        currentScreen = "CATEGORY_FORM"
                                    },
                                    onDeleteClick = { deleteCat ->
                                        categoryViewModel.deleteCategory(
                                            categoryId = deleteCat.categoryId ?: 0L,
                                            onSuccess = {
                                                selectedCategory = null  // Reset để tránh hiển thị stale data
                                                currentScreen = "CATEGORIES"
                                            },
                                            onError = { errorMsg ->
                                                android.util.Log.e("MainActivity", "Lỗi xóa: $errorMsg")
                                            }
                                        )
                                    },
                                    onToggleActiveClick = { toggleCat ->
                                        if (toggleCat.isActive) {
                                            categoryViewModel.deactivateCategory(toggleCat.categoryId ?: 0L)
                                        } else {
                                            categoryViewModel.reactivateCategory(toggleCat.categoryId ?: 0L)
                                        }
                                        // Reload list và update selectedCategory
                                        categoryViewModel.loadCategories()
                                        // Update selectedCategory để DetailScreen refresh
                                        selectedCategory = toggleCat.copy(isActive = !toggleCat.isActive)
                                    }

                                )
                            }
                        }

                        "CATEGORY_FORM" -> {
                            CategoryFormScreen(
                                categoryToEdit = categoryToEdit,
                                isSaving = isSaving,
                                serverError = formError,
                                onSave = { savedCategory ->
                                    formError = null
                                    if (categoryToEdit == null) {
                                        isSaving = true
                                        categoryViewModel.addCategory(savedCategory) {
                                            isSaving = false
                                            currentScreen = "CATEGORIES"
                                        }
                                    } else {
                                        isSaving = true
                                        categoryViewModel.updateCategory(savedCategory) {
                                            isSaving = false
                                            currentScreen = "CATEGORIES"
                                        }
                                    }
                                },
                                onBackClick = {
                                    formError = null
                                    currentScreen = if (categoryToEdit != null) "CATEGORY_DETAIL" else "CATEGORIES"
                                }
                            )
                        }

                        "SUPPLIERS" -> {
                            SupplierListScreen(
                                viewModel = supplierViewModel,
                                onSupplierClick = { supplier ->
                                    selectedSupplier = supplier
                                    currentScreen = "SUPPLIER_DETAIL"
                                },
                                onAddClick = {
                                    supplierToEdit = null
                                    currentScreen = "SUPPLIER_FORM"
                                }
                            )
                        }

                        "SUPPLIER_DETAIL" -> {
                            selectedSupplier?.let { supplier ->
                                SupplierDetailScreen(
                                    supplier = supplier,
                                    onBackClick = { currentScreen = "SUPPLIERS" },
                                    onEditClick = { editSup ->
                                        supplierToEdit = editSup
                                        currentScreen = "SUPPLIER_FORM"
                                    },
                                    onDeleteClick = { deleteSup ->
                                        supplierViewModel.deleteSupplier(
                                            supplierId = deleteSup.supplierId ?: 0L,
                                            onSuccess = {
                                                selectedSupplier = null  // Reset để tránh hiển thị stale data
                                                currentScreen = "SUPPLIERS"
                                            },
                                            onError = { errorMsg ->
                                                android.util.Log.e("MainActivity", "Lỗi xóa: $errorMsg")
                                            }
                                        )
                                    },
                                    onToggleActiveClick = { toggleSup ->
                                        if (toggleSup.isActive) {
                                            supplierViewModel.deactivateSupplier(toggleSup.supplierId ?: 0L)
                                        } else {
                                            supplierViewModel.reactivateSupplier(toggleSup.supplierId ?: 0L)
                                        }
                                        // Reload list và update selectedSupplier
                                        supplierViewModel.loadSuppliers()
                                        selectedSupplier = toggleSup.copy(isActive = !toggleSup.isActive)
                                    }
                                )
                            }
                        }

                        "SUPPLIER_FORM" -> {
                            SupplierFormScreen(
                                supplierToEdit = supplierToEdit,
                                isSaving = isSaving,
                                serverError = formError,
                                onSave = { savedSupplier ->
                                    formError = null
                                    if (supplierToEdit == null) {
                                        isSaving = true
                                        supplierViewModel.addSupplier(savedSupplier) {
                                            isSaving = false
                                            currentScreen = "SUPPLIERS"
                                        }
                                    } else {
                                        isSaving = true
                                        supplierViewModel.updateSupplier(savedSupplier) {
                                            isSaving = false
                                            currentScreen = "SUPPLIERS"
                                        }
                                    }
                                },
                                onBackClick = {
                                    formError = null
                                    currentScreen = if (supplierToEdit != null) "SUPPLIER_DETAIL" else "SUPPLIERS"
                                }
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
