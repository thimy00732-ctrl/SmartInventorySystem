
package com.sis.android.ui.inventory.threshold

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val DarkGreen = Color(0xFF304B17)

@Composable
fun UpdateThresholdDialog(
    currentMin: Int,
    currentMax: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var minText by remember(currentMin) {
        mutableStateOf(currentMin.toString())
    }

    var maxText by remember(currentMax) {
        mutableStateOf(currentMax.toString())
    }

    var minError by remember {
        mutableStateOf<String?>(null)
    }

    var maxError by remember {
        mutableStateOf<String?>(null)
    }

    fun validate(): Boolean {
        val min = minText.toIntOrNull()
        val max = maxText.toIntOrNull()

        minError = when {
            minText.isBlank() ->
                "Vui lòng nhập mức tồn tối thiểu"

            min == null ->
                "Giá trị không hợp lệ"

            min < 0 ->
                "Giá trị phải lớn hơn hoặc bằng 0"

            else -> null
        }

        maxError = when {
            maxText.isBlank() ->
                "Vui lòng nhập mức tồn tối đa"

            max == null ->
                "Giá trị không hợp lệ"

            max < 0 ->
                "Giá trị phải lớn hơn hoặc bằng 0"

            min != null && max < min ->
                "Mức tối đa phải lớn hơn hoặc bằng mức tối thiểu"

            else -> null
        }

        return minError == null && maxError == null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Thiết lập ngưỡng tồn kho",
                color = DarkGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Điều chỉnh số lượng tồn kho tối thiểu và tối đa cho sản phẩm.",
                    color = Color.DarkGray
                )

                OutlinedTextField(
                    value = minText,
                    onValueChange = {
                        minText = it.filter { c -> c.isDigit() }
                        validate()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tồn tối thiểu (Min)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = minError != null,
                    supportingText = {
                        minError?.let { Text(it) }
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = maxText,
                    onValueChange = {
                        maxText = it.filter { c -> c.isDigit() }
                        validate()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tồn tối đa (Max)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = maxError != null,
                    supportingText = {
                        maxError?.let { Text(it) }
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        val min = minText.toIntOrNull()
                        val max = maxText.toIntOrNull()

                        if (min != null && max != null) {
                            onSave(min, max)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen
                )
            ) {
                Text("Lưu thay đổi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Hủy",
                    color = DarkGreen
                )
            }
        }
    )
}
