package com.sis.android.ui.products

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun DeleteConfirmationDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận xóa sản phẩm") },
        text = { Text("Bạn có chắc chắn muốn xóa sản phẩm \"$productName\" khỏi hệ thống kho không? Hành động này không thể hoàn tác.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Xóa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}