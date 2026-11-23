package com.sis.android.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.ThresholdProduct
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import com.sis.android.ui.viewmodel.InventoryViewModel
import com.sis.android.ui.viewmodel.ThresholdUiState

/**
 * UC33: Màn hình Thiết lập Ngưỡng Tồn kho
 * Cho phép ADMIN cập nhật minStock và maxStock
 */
@Composable
fun InventoryThresholdScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val thresholdUiState by viewModel.thresholdUiState.collectAsState()
    
    // Client-side pagination state
    var currentPage by remember { mutableStateOf(1) }
    val pageSize = 10

    // Load thresholds on first composition
    LaunchedEffect(Unit) {
        viewModel.loadThresholds()
    }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // Header
            AppHeader(
                title = "Thiết lập ngưỡng",
                subtitle = "Cấu hình tồn tối thiểu và tối đa",
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Quay lại", color = Color.White, fontSize = 13.sp)
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Content
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = thresholdUiState) {
                            is ThresholdUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CrudColors.Primary
                                )
                            }
                            is ThresholdUiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        state.message,
                                        color = CrudColors.DangerAction,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.loadThresholds() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CrudColors.Primary
                                        )
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                            is ThresholdUiState.Success -> {
                                if (state.products.isEmpty()) {
                                    Text(
                                        "Không có sản phẩm nào!",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        color = CrudColors.TextSecondary
                                    )
                                } else {
                                    // Client-side pagination
                                    val totalPages = (state.products.size + pageSize - 1) / pageSize
                                    val startIndex = (currentPage - 1) * pageSize
                                    val endIndex = minOf(startIndex + pageSize, state.products.size)
                                    val currentPageItems = state.products.subList(startIndex, endIndex)
                                    
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(currentPageItems, key = { it.productId }) { product ->
                                            ThresholdProductCard(
                                                product = product,
                                                onSave = { minStock, maxStock ->
                                                    viewModel.updateThreshold(
                                                        productId = product.productId,
                                                        minStock = minStock,
                                                        maxStock = maxStock,
                                                        onSuccess = {
                                                            // Success handled by ViewModel reload
                                                        },
                                                        onError = { error ->
                                                            // Error shown via Snackbar or Toast
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                        
                                        // Client-side pagination
                                        item {
                                            Pagination(
                                                currentPage = currentPage,
                                                totalPages = totalPages,
                                                onPageClick = { page -> currentPage = page },
                                                onPreviousClick = { 
                                                    if (currentPage > 1) currentPage--
                                                },
                                                onNextClick = { 
                                                    if (currentPage < totalPages) currentPage++
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThresholdProductCard(
    product: ThresholdProduct,
    onSave: (minStock: Int, maxStock: Int) -> Unit
) {
    var minInput by remember { mutableStateOf(product.minStock.toString()) }
    var maxInput by remember { mutableStateOf(product.maxStock.toString()) }
    var isSaving by remember { mutableStateOf(false) }
    
    val minValue = minInput.toIntOrNull()
    val maxValue = maxInput.toIntOrNull()
    
    val minError = when {
        minInput.isBlank() -> "Không được để trống"
        minValue == null -> "Phải là số nguyên"
        minValue < 0 -> "Phải >= 0"
        else -> null
    }
    
    val maxError = when {
        maxInput.isBlank() -> "Không được để trống"
        maxValue == null -> "Phải là số nguyên"
        maxValue < 0 -> "Phải >= 0"
        minValue != null && maxValue != null && maxValue < minValue -> "Phải >= tối thiểu"
        else -> null
    }
    
    val isValid = minError == null && maxError == null && minValue != null && maxValue != null
    val hasChanges = minValue != product.minStock || maxValue != product.maxStock

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CrudColors.TextPrimary
                    )
                    Text(
                        product.sku,
                        fontSize = 13.sp,
                        color = CrudColors.TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Tổng tồn",
                        fontSize = 11.sp,
                        color = CrudColors.TextSecondary
                    )
                    Text(
                        "${product.totalStock} ${product.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CrudColors.Primary
                    )
                }
            }

            Divider(color = CrudColors.BorderColor)

            // Threshold inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Min Stock
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Tồn tối thiểu",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrudColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = minInput,
                        onValueChange = { minInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0") },
                        singleLine = true,
                        isError = minError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = CrudStyles.InputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrudColors.Primary,
                            unfocusedBorderColor = CrudColors.BorderColor
                        )
                    )
                    if (minError != null) {
                        Text(
                            minError,
                            color = CrudColors.DangerAction,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // Max Stock
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Tồn tối đa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrudColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = maxInput,
                        onValueChange = { maxInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("1000") },
                        singleLine = true,
                        isError = maxError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = CrudStyles.InputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrudColors.Primary,
                            unfocusedBorderColor = CrudColors.BorderColor
                        )
                    )
                    if (maxError != null) {
                        Text(
                            maxError,
                            color = CrudColors.DangerAction,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        minInput = product.minStock.toString()
                        maxInput = product.maxStock.toString()
                    },
                    enabled = hasChanges && !isSaving
                ) {
                    Text("Hủy")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (isValid && minValue != null && maxValue != null) {
                            isSaving = true
                            onSave(minValue, maxValue)
                        }
                    },
                    enabled = isValid && hasChanges && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrudColors.Primary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Lưu ngưỡng")
                    }
                }
            }
        }
    }
}
