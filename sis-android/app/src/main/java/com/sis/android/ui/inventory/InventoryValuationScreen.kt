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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
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
import com.sis.android.data.model.InventoryValuationRow
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import java.math.BigDecimal

/*
 * Màn hình UC39 chỉ hiển thị giá vốn cho ADMIN.
 * Giá trị được tính theo giá vốn hiện tại và tổng tồn theo kho/sản phẩm.
 */
@Composable
fun InventoryValuationScreen(
    rows: List<InventoryValuationRow>,
    userRole: InventoryUserRole,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    var selectedWarehouse by remember { mutableStateOf("Tất cả kho") }
    var selectedCategory by remember { mutableStateOf("Tất cả danh mục") }
    var keyword by remember { mutableStateOf("") }

    val filteredRows = rows.filter { row ->
        (selectedWarehouse == "Tất cả kho" || row.warehouseName == selectedWarehouse) &&
            (selectedCategory == "Tất cả danh mục" || row.categoryName == selectedCategory) &&
            (keyword.isBlank() ||
                row.sku.contains(keyword, ignoreCase = true) ||
                row.productName.contains(keyword, ignoreCase = true))
    }
    val totalValue = filteredRows.fold(BigDecimal.ZERO) { total, row -> total.add(row.stockValue) }
    val totalQuantity = filteredRows.sumOf { it.totalQuantity }
    val productCount = filteredRows.filter { it.totalQuantity > 0 }.distinctBy { it.productId }.size
    val warehouseTotals = filteredRows
        .groupBy { it.warehouseName }
        .mapValues { (_, warehouseRows) ->
            warehouseRows.fold(BigDecimal.ZERO) { total, row -> total.add(row.stockValue) }
        }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        topBar = {
            AppHeader(
                title = "Giá trị tồn kho",
                subtitle = "Tổng hợp theo giá vốn hiện tại",
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Quay lại", color = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!InventoryRolePolicy.canViewValuation(userRole)) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Bạn không có quyền xem giá trị tồn kho.",
                    color = CrudColors.DangerAction
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrudColors.WarnBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Giá trị tính theo giá vốn hiện tại, không phản ánh giá vốn lịch sử.",
                        modifier = Modifier.padding(12.dp),
                        color = CrudColors.WarnText,
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ValuationSummaryCard("Tổng giá trị tồn", InventoryFormatters.formatCurrency(totalValue))
                    ValuationSummaryCard("Tổng số lượng", InventoryFormatters.formatNumber(totalQuantity))
                    ValuationSummaryCard("Sản phẩm có tồn", productCount.toString())
                }

                if (warehouseTotals.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
                        shape = CrudStyles.CardShape
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Giá trị theo kho", fontWeight = FontWeight.Bold, color = CrudColors.TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                warehouseTotals.forEach { (warehouse, value) ->
                                    WarehouseValueCard(warehouse, InventoryFormatters.formatCurrency(value))
                                }
                            }
                        }
                    }
                }

                ValuationFilters(
                    rows = rows,
                    selectedWarehouse = selectedWarehouse,
                    onWarehouseChange = { selectedWarehouse = it },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    keyword = keyword,
                    onKeywordChange = { keyword = it }
                )

                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
                    shape = CrudStyles.CardShape
                ) {
                    InventoryContentState(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        isEmpty = filteredRows.isEmpty(),
                        emptyMessage = "Chưa có dữ liệu giá trị tồn kho.",
                        onRetry = onRetry
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.widthIn(min = 900.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CrudColors.LightGreenBg)
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ValuationCell("Kho", 1.2f, true)
                                    ValuationCell("SKU / Tên sản phẩm", 2f, true)
                                    ValuationCell("Tổng số lượng", 1f, true)
                                    ValuationCell("Giá vốn hiện tại", 1.2f, true)
                                    ValuationCell("Giá trị tồn", 1.2f, true)
                                }
                                Divider(color = CrudColors.BorderColor)
                                filteredRows.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ValuationCell(row.warehouseName, 1.2f)
                                        ValuationCell("${row.sku}\n${row.productName}", 2f)
                                        ValuationCell(InventoryFormatters.formatNumber(row.totalQuantity), 1f)
                                        ValuationCell(InventoryFormatters.formatCurrency(row.currentCostPrice), 1.2f)
                                        ValuationCell(InventoryFormatters.formatCurrency(row.stockValue), 1.2f, true)
                                    }
                                    Divider(color = CrudColors.BorderColor)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ValuationCell("Tổng cộng", 3.2f, true)
                                    ValuationCell(InventoryFormatters.formatNumber(totalQuantity), 1f, true)
                                    ValuationCell("", 1.2f)
                                    ValuationCell(InventoryFormatters.formatCurrency(totalValue), 1.2f, true)
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
private fun ValuationSummaryCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
        shape = CrudStyles.CardShape
    ) {
        Column(
            modifier = Modifier.widthIn(min = 150.dp).padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = CrudColors.Primary, fontSize = 16.sp)
            Text(label, color = CrudColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WarehouseValueCard(warehouse: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CrudColors.LightGreenBg),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(warehouse, color = CrudColors.TextSecondary, fontSize = 12.sp)
            Text(value, color = CrudColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ValuationFilters(
    rows: List<InventoryValuationRow>,
    selectedWarehouse: String,
    onWarehouseChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    val warehouses = rows.map { it.warehouseName }.distinct().sorted()
    val categories = rows.map { it.categoryName }.distinct().sorted()
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
                ValuationFilterDropdown(
                    label = "Kho",
                    value = selectedWarehouse,
                    allLabel = "Tất cả kho",
                    options = warehouses,
                    modifier = Modifier.weight(1f),
                    onValueChange = onWarehouseChange
                )
                ValuationFilterDropdown(
                    label = "Danh mục",
                    value = selectedCategory,
                    allLabel = "Tất cả danh mục",
                    options = categories,
                    modifier = Modifier.weight(1f),
                    onValueChange = onCategoryChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValuationFilterDropdown(
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
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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
private fun RowScope.ValuationCell(text: String, weight: Float, bold: Boolean = false) {
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
