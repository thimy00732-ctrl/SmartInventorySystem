
package com.sis.android.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun UpdateReservationDialog(
    currentQuantity: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var quantityText by remember(currentQuantity) {
        mutableStateOf(currentQuantity.toString())
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cập nhật số lượng đặt trước")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it.filter { c -> c.isDigit() }
                        error = null
                    },
                    label = { Text("Số lượng đặt trước") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = error != null,
                    supportingText = {
                        error?.let { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantity = quantityText.toIntOrNull()

                    if (quantity == null) {
                        error = "Vui lòng nhập số lượng hợp lệ"
                    } else {
                        onSave(quantity)
                    }
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
