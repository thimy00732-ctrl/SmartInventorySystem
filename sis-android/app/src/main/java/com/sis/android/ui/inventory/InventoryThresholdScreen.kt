package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.InventoryThresholdProduct
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

/*
 * Màn hình UC33 cho phép ADMIN cập nhật min_stock và max_stock.
 * SKU, tên sản phẩm và tổng tồn được hiển thị chỉ đọc.
 */
@Composable
fun InventoryThresholdScreen(
    products: List<InventoryThresholdProduct>,
    canEditThresholds: Boolean,
    onBack: () -> Unit,
    onSaveThresholds: (productId: Long, minStock: Int, maxStock: Int) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        topBar = {
            AppHeader(
                title = "Thiết lập ngưỡng tồn kho",
                subtitle = "Cấu hình mức tồn tối thiểu và tối đa",
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Quay lại", color = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!canEditThresholds) {
                ThresholdInfoCard("Bạn không có quyền cập nhật ngưỡng tồn kho.")
            }

            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
                shape = CrudStyles.CardShape
            ) {
                InventoryContentState(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    isEmpty = products.isEmpty(),
                    emptyMessage = "Chưa có sản phẩm để thiết lập ngưỡng.",
                    onRetry = onRetry
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Row(
                                modifier = Modifier
                                    .widthIn(min = 880.dp)
                                    .background(CrudColors.LightGreenBg)
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThresholdCell("SKU / Sản phẩm", 2f, true)
                                ThresholdCell("Tổng tồn hiện tại", 1.1f, true)
                                ThresholdCell("Tồn tối thiểu", 1.2f, true)
                                ThresholdCell("Tồn tối đa", 1.2f, true)
                                ThresholdCell("Thao tác", 1.3f, true)
                            }
                        }
                        Divider(color = CrudColors.BorderColor)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(products, key = { it.productId }) { product ->
                                ThresholdProductRow(
                                    product = product,
                                    canEdit = canEditThresholds,
                                    onSave = onSaveThresholds
                                )
                                Divider(color = CrudColors.BorderColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThresholdProductRow(
    product: InventoryThresholdProduct,
    canEdit: Boolean,
    onSave: (productId: Long, minStock: Int, maxStock: Int) -> Unit
) {
    var minInput by remember(product.productId) { mutableStateOf(product.minStock.toString()) }
    var maxInput by remember(product.productId) { mutableStateOf(product.maxStock.toString()) }
    var successMessage by remember(product.productId) { mutableStateOf<String?>(null) }

    val minValue = minInput.toIntOrNull()
    val maxValue = maxInput.toIntOrNull()
    val minError = InventoryThresholdRules.validateMinStock(minInput)
    val maxError = InventoryThresholdRules.validateMaxStock(minInput, maxInput)
    val isValid = canEdit && minError == null && maxError == null

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.widthIn(min = 880.dp).padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                ThresholdCell("${product.sku}\n${product.productName}", 2f)
                ThresholdCell(InventoryFormatters.formatNumber(product.totalStock), 1.1f)
                Column(modifier = Modifier.weight(1.2f).padding(horizontal = 4.dp)) {
                    OutlinedTextField(
                        value = minInput,
                        onValueChange = {
                            minInput = it
                            successMessage = null
                        },
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tối thiểu") },
                        singleLine = true,
                        isError = minError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = CrudStyles.InputShape,
                        colors = thresholdFieldColors()
                    )
                    minError?.let { Text(it, color = CrudColors.DangerAction, fontSize = 10.sp) }
                }
                Column(modifier = Modifier.weight(1.2f).padding(horizontal = 4.dp)) {
                    OutlinedTextField(
                        value = maxInput,
                        onValueChange = {
                            maxInput = it
                            successMessage = null
                        },
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tối đa") },
                        singleLine = true,
                        isError = maxError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = CrudStyles.InputShape,
                        colors = thresholdFieldColors()
                    )
                    maxError?.let { Text(it, color = CrudColors.DangerAction, fontSize = 10.sp) }
                }
                Column(
                    modifier = Modifier.weight(1.3f).padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            if (isValid && minValue != null && maxValue != null) {
                                onSave(product.productId, minValue, maxValue)
                                successMessage = "Đã lưu ngưỡng."
                            }
                        },
                        enabled = isValid,
                        shape = CrudStyles.InputShape,
                        colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
                    ) {
                        Text("Lưu")
                    }
                    OutlinedButton(
                        onClick = {
                            minInput = product.minStock.toString()
                            maxInput = product.maxStock.toString()
                            successMessage = null
                        },
                        enabled = canEdit,
                        shape = CrudStyles.InputShape
                    ) {
                        Text("Hủy")
                    }
                    successMessage?.let {
                        Text(it, color = CrudColors.OkText, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ThresholdCell(text: String, weight: Float, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.weight(weight).padding(horizontal = 6.dp),
        color = if (bold) CrudColors.TextPrimary else CrudColors.TextSecondary,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = 12.sp
    )
}

@Composable
private fun thresholdFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CrudColors.Primary,
    unfocusedBorderColor = CrudColors.BorderColor,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

@Composable
private fun ThresholdInfoCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CrudColors.WarnBg),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            color = CrudColors.WarnText,
            fontSize = 13.sp
        )
    }
}
