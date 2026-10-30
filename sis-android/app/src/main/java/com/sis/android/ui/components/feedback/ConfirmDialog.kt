package com.sis.android.ui.components.feedback

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.sis.android.ui.theme.color.CrudColors

/** Hộp thoại xác nhận dùng cho mọi chức năng (xóa, ngừng hoạt động...). */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    danger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmText,
                    color = if (danger) CrudColors.DangerAction else CrudColors.Primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        }
    )
}