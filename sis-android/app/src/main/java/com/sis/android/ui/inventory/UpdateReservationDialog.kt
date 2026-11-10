
package com.sis.android.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.repository.InventoryRepository
import kotlinx.coroutines.launch

private val DarkGreen = Color(0xFF365314)
private val MainText = Color(0xFF263449)
private val GrayText = Color(0xFF929DB3)
private val BorderColor = Color(0xFFE4E9F1)
private val ErrorColor = Color(0xFFE34D4D)

@Composable
fun UpdateReservationDialog(
    inventoryId: Int,
    stockQuantity: Int,
    reservedQuantity: Int,
    repository: InventoryRepository,
    onDismiss: () -> Unit,
    onSuccess: (Int) -> Unit
) {
    var quantityText by remember(inventoryId, reservedQuantity) {
        mutableStateOf(reservedQuantity.toString())
    }

    var serverError by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    // Số lượng tối đa có thể giữ chỗ thêm
    val maxAllowed = (stockQuantity - reservedQuantity).coerceAtLeast(0)

    val newQuantity = quantityText.toIntOrNull()

    // Kiểm tra dữ liệu ngay khi người dùng nhập
    val validationError = when {
        quantityText.isBlank() ->
            "Vui lòng nhập số lượng"

        newQuantity == null ->
            "Số lượng không hợp lệ"

        newQuantity < 0 ->
            "Số lượng không được nhỏ hơn 0"

        newQuantity > maxAllowed ->
            "Số lượng vượt quá mức tối đa cho phép"

        else -> null
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,

        title = {
            Text(
                text = "Cập nhật giữ chỗ",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MainText
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Mã tồn kho: $inventoryId",
                    color = GrayText,
                    fontSize = 12.sp
                )

                Text(
                    text = "Số lượng giữ chỗ",
                    color = MainText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.all { it.isDigit() }) {
                            quantityText = value
                            serverError = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Nhập số lượng",
                            color = GrayText
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = validationError != null ||
                            serverError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorBorderColor = ErrorColor
                    )
                )

                Text(
                    text = "Tối đa cho phép: $maxAllowed",
                    color = GrayText,
                    fontSize = 12.sp
                )

                if (validationError != null) {
                    Text(
                        text = validationError,
                        color = ErrorColor,
                        fontSize = 12.sp
                    )
                }

                if (serverError != null) {
                    Text(
                        text = serverError.orEmpty(),
                        color = ErrorColor,
                        fontSize = 12.sp
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    val quantity = newQuantity ?: return@Button

                    scope.launch {
                        isLoading = true
                        serverError = null

                        try {
                            val result = repository.updateReservation(
                                inventoryId = inventoryId,
                                reservedQuantity = quantity
                            )

                            result.onSuccess { response ->
                                onSuccess(response.reservedQuantity)
                                onDismiss()
                            }.onFailure { error ->
                                serverError = error.message
                                    ?: "Cập nhật giữ chỗ thất bại"
                            }
                        } catch (error: Exception) {
                            serverError = error.message
                                ?: "Cập nhật giữ chỗ thất bại"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = validationError == null && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    disabledContainerColor = Color(0xFFB8C4A9)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Cập nhật",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    "Hủy",
                    color = GrayText
                )
            }
        }
    )
}
