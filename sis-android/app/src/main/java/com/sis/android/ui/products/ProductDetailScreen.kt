package com.sis.android.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sis.android.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi Tiết Sản Phẩm Kho") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Đổi thành bản AutoMirrored đã import ở trên
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = product.productName,
                style = MaterialTheme.typography.headlineMedium
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Mã SKU:", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.sku, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Mã vạch (Barcode):", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.barcode ?: "Không có", style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Đơn vị tính:", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.unit, style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Định mức tồn tối thiểu:", style = MaterialTheme.typography.bodyLarge)
                Text(text = "${product.minStock}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Trạng thái:", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.status, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Mô tả sản phẩm:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = product.description ?: "Chưa có mô tả chi tiết cho sản phẩm này.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}