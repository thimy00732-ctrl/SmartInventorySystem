package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.InventoryItem
import com.sis.android.data.model.InventoryThresholdProduct
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import java.util.Locale

/*
 * Trang UC34/UC35 gom cảnh báo tồn thấp và vượt mức thành hai tab.
 * Dữ liệu hiện lấy từ mock; danh sách được tổng hợp theo sản phẩm và ngưỡng.
 */
@Composable
fun InventoryAlertsScreen(
    products: List<InventoryThresholdProduct>,
    inventoryItems: List<InventoryItem>,
    canManageThresholds: Boolean,
    canViewValuation: Boolean,
    onBack: () -> Unit,
    onGoToThresholds: () -> Unit,
    onGoToValuation: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var keyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả danh mục") }
    var selectedSupplier by remember { mutableStateOf("Tất cả nhà cung cấp") }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }

    val activeProducts = products.filter { it.isActive }
    val lowStockProducts = InventoryThresholdRules.lowStock(activeProducts)
    val overStockProducts = InventoryThresholdRules.overStock(activeProducts)
    val outOfStockCount = lowStockProducts.count { it.totalStock == 0 }

    val categories = activeProducts.map { it.categoryName }.distinct().sorted()
    val suppliers = activeProducts.map { it.supplierName }.distinct().sorted()
    val visibleLowStock = lowStockProducts.filter {
        it.matchesAlertFilter(keyword, selectedCategory, selectedSupplier)
    }
    val visibleOverStock = overStockProducts.filter {
        it.matchesAlertFilter(keyword, selectedCategory, selectedSupplier)
    }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        topBar = {
            AppHeader(
                title = "Cảnh báo tồn kho",
                subtitle = "Theo dõi sản phẩm tồn thấp và vượt mức",
                actions = {
                    if (canManageThresholds) {
                        TextButton(onClick = onGoToThresholds) {
                            Text("Thiết lập ngưỡng", color = Color.White)
                        }
                    }
                    if (canViewValuation) {
                        TextButton(onClick = onGoToValuation) {
                            Text("Giá trị", color = Color.White)
                        }
                    }
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
            if (selectedTab == 0) {
                LowStockSummary(
                    lowCount = lowStockProducts.size,
                    outCount = outOfStockCount
                )
            }

            AlertFilters(
                keyword = keyword,
                onKeywordChange = { keyword = it },
                selectedCategory = selectedCategory,
                categories = categories,
                onCategoryChange = { selectedCategory = it },
                selectedSupplier = selectedSupplier,
                suppliers = suppliers,
                onSupplierChange = { selectedSupplier = it }
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = CrudColors.Primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Tồn thấp")
                            Badge { Text(lowStockProducts.size.toString()) }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Vượt mức")
                            Badge { Text(overStockProducts.size.toString()) }
                        }
                    }
                )
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
                    isEmpty = if (selectedTab == 0) visibleLowStock.isEmpty() else visibleOverStock.isEmpty(),
                    emptyMessage = if (selectedTab == 0) {
                        "Không có sản phẩm tồn thấp phù hợp."
                    } else {
                        "Không có sản phẩm vượt mức phù hợp."
                    },
                    onRetry = onRetry
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 1120.dp)) {
                            if (selectedTab == 0) {
                                LowStockTableHeader()
                                Divider(color = CrudColors.BorderColor)
                                visibleLowStock.forEach { product ->
                                    LowStockTableRow(product) { selectedProductId = product.productId }
                                    Divider(color = CrudColors.BorderColor)
                                }
                            } else {
                                OverStockTableHeader()
                                Divider(color = CrudColors.BorderColor)
                                visibleOverStock.forEach { product ->
                                    OverStockTableRow(product) { selectedProductId = product.productId }
                                    Divider(color = CrudColors.BorderColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedProductId?.let { productId ->
        val selectedProduct = products.firstOrNull { it.productId == productId }
        if (selectedProduct != null) {
            InventoryAllocationSheet(
                product = selectedProduct,
                inventoryItems = inventoryItems.filter { it.productId == productId },
                sortByStockDescending = selectedTab == 1,
                onDismiss = { selectedProductId = null }
            )
        }
    }
}

@Composable
private fun LowStockSummary(lowCount: Int, outCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
        shape = CrudStyles.CardShape
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("$lowCount sản phẩm dưới ngưỡng", fontWeight = FontWeight.Bold, color = CrudColors.TextPrimary)
            Text("$outCount sản phẩm đã hết hàng (tồn = 0)", color = CrudColors.DangerAction, fontSize = 13.sp)
            Text(
                "Tính theo tổng tồn của các kho đang hoạt động",
                color = CrudColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AlertFilters(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    selectedCategory: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    selectedSupplier: String,
    suppliers: List<String>,
    onSupplierChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
        shape = CrudStyles.CardShape
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrudColors.TextSecondary) },
                placeholder = { Text("Tìm theo SKU hoặc tên sản phẩm") },
                shape = CrudStyles.InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrudColors.Primary,
                    unfocusedBorderColor = CrudColors.BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertFilterDropdown(
                    label = "Danh mục",
                    value = selectedCategory,
                    allLabel = "Tất cả danh mục",
                    options = categories,
                    modifier = Modifier.weight(1f),
                    onValueChange = onCategoryChange
                )
                AlertFilterDropdown(
                    label = "Nhà cung cấp",
                    value = selectedSupplier,
                    allLabel = "Tất cả nhà cung cấp",
                    options = suppliers,
                    modifier = Modifier.weight(1f),
                    onValueChange = onSupplierChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertFilterDropdown(
    label: String,
    value: String,
    allLabel: String,
    options: List<String>,
    modifier: Modifier,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = CrudStyles.InputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrudColors.Primary,
                unfocusedBorderColor = CrudColors.BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    onValueChange(allLabel)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LowStockTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(CrudColors.LightGreenBg).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertCell("SKU / Tên sản phẩm", 2f, true)
        AlertCell("Đơn vị", 0.7f, true)
        AlertCell("Nhà cung cấp", 1.5f, true)
        AlertCell("Tồn tối thiểu", 1f, true)
        AlertCell("Tổng tồn", 0.9f, true)
        AlertCell("Số lượng thiếu", 1f, true)
        AlertCell("Mức độ", 0.9f, true)
        AlertCell("Thao tác", 1.5f, true)
    }
}

@Composable
private fun LowStockTableRow(product: InventoryThresholdProduct, onShowDistribution: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertCell("${product.sku}\n${product.productName}", 2f)
        AlertCell(product.unit, 0.7f)
        AlertCell(product.supplierName, 1.5f)
        AlertCell(InventoryFormatters.formatNumber(product.minStock), 1f)
        AlertCell(InventoryFormatters.formatNumber(product.totalStock), 0.9f)
        AlertCell(InventoryFormatters.formatNumber(product.minStock - product.totalStock), 1f, true)
        Row(modifier = Modifier.weight(0.9f), horizontalArrangement = Arrangement.Center) {
            AlertSeverityBadge(
                label = if (product.totalStock == 0) "Hết hàng" else "Thấp",
                isOutOfStock = product.totalStock == 0
            )
        }
        Row(modifier = Modifier.weight(1.5f), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = onShowDistribution,
                shape = CrudStyles.InputShape,
                colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
            ) {
                Text("Xem phân bổ theo kho", fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun OverStockTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(CrudColors.LightGreenBg).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertCell("SKU / Tên sản phẩm", 2f, true)
        AlertCell("Đơn vị", 0.7f, true)
        AlertCell("Tồn tối đa", 1f, true)
        AlertCell("Tổng tồn hiện tại", 1f, true)
        AlertCell("Số lượng vượt", 1f, true)
        AlertCell("Tỷ lệ vượt", 0.9f, true)
        AlertCell("Thao tác", 1.5f, true)
    }
}

@Composable
private fun OverStockTableRow(product: InventoryThresholdProduct, onShowDistribution: () -> Unit) {
    val excess = product.totalStock - product.maxStock
    val excessPercent = if (product.maxStock > 0) {
        String.format(Locale.getDefault(), "%.2f%%", (product.totalStock.toDouble() / product.maxStock - 1) * 100)
    } else {
        "-"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertCell("${product.sku}\n${product.productName}", 2f)
        AlertCell(product.unit, 0.7f)
        AlertCell(InventoryFormatters.formatNumber(product.maxStock), 1f)
        AlertCell(InventoryFormatters.formatNumber(product.totalStock), 1f)
        AlertCell(InventoryFormatters.formatNumber(excess), 1f, true)
        AlertCell(excessPercent, 0.9f)
        Row(modifier = Modifier.weight(1.5f), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = onShowDistribution,
                shape = CrudStyles.InputShape,
                colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
            ) {
                Text("Xem kho bị ảnh hưởng", fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun AlertSeverityBadge(label: String, isOutOfStock: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) Color(0xFFFFE6E6) else CrudColors.WarnBg
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = if (isOutOfStock) CrudColors.DangerAction else CrudColors.WarnText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RowScope.AlertCell(text: String, weight: Float, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.weight(weight).padding(horizontal = 5.dp),
        color = if (bold) CrudColors.TextPrimary else CrudColors.TextSecondary,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = 12.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryAllocationSheet(
    product: InventoryThresholdProduct,
    inventoryItems: List<InventoryItem>,
    sortByStockDescending: Boolean,
    onDismiss: () -> Unit
) {
    val allocations = if (sortByStockDescending) {
        inventoryItems.sortedByDescending { it.stockQuantity }
    } else {
        inventoryItems
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CrudColors.LightGreenBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Phân bổ tồn kho", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("${product.sku} · ${product.productName}", color = CrudColors.TextSecondary)
                }
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
            if (allocations.isEmpty()) {
                Text("Chưa có dữ liệu phân bổ theo kho.", color = CrudColors.TextSecondary)
            } else {
                Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.widthIn(min = 760.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(CrudColors.LightGreenBg).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AllocationCell("Kho", 1.2f, true)
                            AllocationCell("Mã lô", 1.1f, true)
                            AllocationCell("Hạn dùng", 1f, true)
                            AllocationCell("Tồn thực tế", 1f, true)
                            AllocationCell("Khả dụng", 1f, true)
                        }
                        Divider(color = CrudColors.BorderColor)
                        allocations.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AllocationCell(item.warehouseName, 1.2f)
                                AllocationCell(InventoryFormatters.formatBatch(item.batchCode), 1.1f)
                                AllocationCell(InventoryFormatters.formatDate(item.expiryDate), 1f)
                                AllocationCell(InventoryFormatters.formatNumber(item.stockQuantity), 1f)
                                AllocationCell(InventoryFormatters.formatNumber(item.availableQuantity), 1f)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.heightIn(min = 8.dp))
        }
    }
}

@Composable
private fun RowScope.AllocationCell(text: String, weight: Float, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.weight(weight).padding(horizontal = 5.dp),
        color = if (bold) CrudColors.TextPrimary else CrudColors.TextSecondary,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = 12.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

private fun InventoryThresholdProduct.matchesAlertFilter(
    keyword: String,
    category: String,
    supplier: String
): Boolean {
    val matchesKeyword = keyword.isBlank() ||
        sku.contains(keyword, ignoreCase = true) ||
        productName.contains(keyword, ignoreCase = true)
    val matchesCategory = category == "Tất cả danh mục" || categoryName == category
    val matchesSupplier = supplier == "Tất cả nhà cung cấp" || supplierName == supplier
    return matchesKeyword && matchesCategory && matchesSupplier
}
