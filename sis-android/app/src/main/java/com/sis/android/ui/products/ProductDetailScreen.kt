package com.sis.android.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sis.android.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit // <-- Thêm tham số sự kiện xóa
) {
    // Biến quản lý việc hiển thị hộp thoại xác nhận xóa
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi Tiết Sản Phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Nút Xóa đặt ngay trên góc phải TopAppBar cho gọn gàng
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa sản phẩm",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // Nút Chỉnh sửa sản phẩm
            FloatingActionButton(
                onClick = { onEditClick(product) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa sản phẩm")
            }
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Mã SKU:", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.sku, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Đơn vị tính:", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.unit, style = MaterialTheme.typography.bodyLarge)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Trạng thái:", style = MaterialTheme.typography.bodyLarge)
                Text(text = product.status, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Mô tả sản phẩm:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = product.description ?: "Chưa có mô tả chi tiết.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Hộp thoại (Dialog) xác nhận trước khi xóa
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa sản phẩm") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm \"${product.productName}\" không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(product) // Gọi hàm xóa
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}