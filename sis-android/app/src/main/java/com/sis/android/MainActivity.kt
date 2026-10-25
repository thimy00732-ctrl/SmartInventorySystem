package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.model.Product
import com.sis.android.ui.products.ProductDetailScreen   // <-- Import đúng từ thư mục products
import com.sis.android.ui.products.ProductListScreen     // <-- Import đúng từ thư mục products
import com.sis.android.ui.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: ProductViewModel = viewModel()
            var selectedProduct by remember { mutableStateOf<Product?>(null) }

            if (selectedProduct == null) {
                ProductListScreen(
                    viewModel = viewModel,
                    onProductClick = { product ->
                        selectedProduct = product
                    }
                )
            } else {
                ProductDetailScreen(
                    product = selectedProduct!!,
                    onBackClick = {
                        selectedProduct = null
                    }
                )
            }
        }
    }
}