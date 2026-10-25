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
import com.sis.android.ui.theme.SISTheme // Gọi từ stylesform_crud.kt
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
                                    productToEdit = null
                                    currentScreen = "FORM"
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
                }
            }
        }
    }
}