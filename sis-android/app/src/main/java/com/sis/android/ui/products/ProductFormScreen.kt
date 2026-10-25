package com.sis.android.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sis.android.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productToEdit: Product? = null, // Nếu null là Thêm mới, có dữ liệu là Sửa
    onSave: (Product) -> Unit,
    onBackClick: () -> Unit
) {
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var productName by remember { mutableStateOf(productToEdit?.productName ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "piece") }
    var minStock by remember { mutableStateOf(productToEdit?.minStock?.toString() ?: "0.0") }
    var status by remember { mutableStateOf(productToEdit?.status ?: "ACTIVE") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productToEdit == null) "Thêm Sản Phẩm Mới" else "Cập Nhật Sản Phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hiển thị thông báo lỗi nếu validation không đạt
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("Mã SKU (*)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Tên sản phẩm (*)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Mã vạch (Barcode)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Đơn vị tính (VD: Thùng, Cái) (*)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = minStock,
                onValueChange = { minStock = it },
                label = { Text("Định mức tồn tối thiểu (*)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Trạng thái (ACTIVE / INACTIVE)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm chi tiết") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // 1. Kiểm tra dữ liệu nhập (Validation)
                    if (sku.isBlank() || productName.isBlank() || unit.isBlank() || minStock.isBlank()) {
                        errorMessage = "⚠️ Vui lòng điền đầy đủ các trường có dấu (*)"
                        return@Button
                    }

                    // Sửa thành Double.toDoubleOrNull() vì minStock trong Product là Double
                    val stockDouble = minStock.toDoubleOrNull()
                    if (stockDouble == null || stockDouble < 0.0) {
                        errorMessage = "⚠️ Định mức tồn tối thiểu phải là số hợp lệ >= 0"
                        return@Button
                    }

                    // 2. Tạo đối tượng Product hoàn chỉnh khớp với Database và Model mới
                    val product = Product(
                        productId = productToEdit?.productId ?: System.currentTimeMillis(),
                        sku = sku.trim(),
                        barcode = barcode.ifBlank { null },
                        productName = productName.trim(),
                        description = description.ifBlank { null },
                        unit = unit.trim(),
                        minStock = stockDouble,
                        categoryId = productToEdit?.categoryId,
                        status = status.trim(),
                        stockQuantity = productToEdit?.stockQuantity ?: 0.0,
                        categoryName = productToEdit?.categoryName ?: "Chưa phân loại"
                    )

                    onSave(product)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (productToEdit == null) "Lưu Sản Phẩm Mới" else "Lưu Thay Đổi")
            }
        }
    }
}