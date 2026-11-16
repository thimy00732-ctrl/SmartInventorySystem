package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.sis.android.data.model.ExpiryStatus
import com.sis.android.data.model.InventoryItem
import com.sis.android.data.model.InventoryThresholdProduct
import com.sis.android.data.model.InventorySortBy
import com.sis.android.data.model.StockStatusFilter
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

/*
 * Màn hình danh sách tồn kho ở đợt 1.
 * Đây là giao diện đầu tiên triển khai theo prompt module quản lý tồn kho,
 * gồm bộ lọc, thống kê nhanh, sắp xếp, phân trang và hiển thị mẫu dữ liệu
 * để kiểm tra luồng nghiệp vụ trước khi tích hợp API thực.
 */
private const val PAGE_SIZE = 8

@Composable
fun InventoryScreen(
    onGoToProducts: () -> Unit = {},
    onGoToAlerts: () -> Unit = {},
    onGoToThresholds: () -> Unit = {},
    onGoToValuation: () -> Unit = {},
    canUpdateReserved: Boolean = false,
    canManageThresholds: Boolean = false,
    canViewValuation: Boolean = false,
    thresholdProducts: List<InventoryThresholdProduct> = InventoryMockData.thresholdProducts(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    var keyword by remember { mutableStateOf("") }
    var selectedWarehouse by remember { mutableStateOf("Tất cả kho") }
    var selectedCategory by remember { mutableStateOf("Tất cả danh mục") }
    var selectedStatus by remember { mutableStateOf(StockStatusFilter.ALL) }
    var sortBy by remember { mutableStateOf(InventorySortBy.PRODUCT_NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(1) }

    var allItems by remember { mutableStateOf(InventoryMockData.data()) }
    var selectedInventoryId by remember { mutableStateOf<Long?>(null) }

    val filteredItems = remember(allItems, keyword, selectedWarehouse, selectedCategory, selectedStatus, sortBy, sortAscending) {
        InventoryListRules.filterAndSort(
            items = allItems,
            keyword = keyword,
            warehouseName = selectedWarehouse,
            categoryName = selectedCategory,
            status = selectedStatus,
            sortBy = sortBy,
            ascending = sortAscending
        )
    }
    val warehouses = allItems.map { it.warehouseName }.distinct().sorted()
    val categories = allItems.mapNotNull { it.categoryName }.distinct().sorted()

    val pageCount = ((filteredItems.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
    if (currentPage > pageCount) currentPage = pageCount

    val pageItems = filteredItems
        .drop((currentPage - 1) * PAGE_SIZE)
        .take(PAGE_SIZE)

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            AppHeader(
                title = "Tồn kho",
                subtitle = "Danh sách kho và dòng tồn hiện có",
                actions = {
                    TextButton(onClick = onGoToAlerts) {
                        Text("Cảnh báo", color = Color.White)
                    }
                    if (canManageThresholds) {
                        TextButton(onClick = onGoToThresholds) {
                            Text("Ngưỡng", color = Color.White)
                        }
                    }
                    if (canViewValuation) {
                        TextButton(onClick = onGoToValuation) {
                            Text("Giá trị", color = Color.White)
                        }
                    }
                    TextButton(onClick = onGoToProducts) {
                        Text("Sản phẩm", color = Color.White)
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Bộ lọc"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = { keyword = it; currentPage = 1 },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrudColors.TextSecondary) },
                            placeholder = { Text("Tìm theo SKU, barcode, tên sản phẩm...", fontSize = 14.sp) },
                            shape = CrudStyles.InputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrudColors.Primary,
                                unfocusedBorderColor = CrudColors.BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        OutlinedButton(
                            onClick = {
                                keyword = ""
                                selectedWarehouse = "Tất cả kho"
                                selectedCategory = "Tất cả danh mục"
                                selectedStatus = StockStatusFilter.ALL
                                sortBy = InventorySortBy.PRODUCT_NAME
                                sortAscending = true
                                currentPage = 1
                            },
                            shape = CrudStyles.InputShape
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Xóa bộ lọc")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InventoryFilterDropdown(
                            label = "Kho",
                            value = selectedWarehouse,
                            options = listOf("Tất cả kho") + warehouses,
                            modifier = Modifier.weight(1f),
                            onValueChange = { selectedWarehouse = it; currentPage = 1 }
                        )
                        InventoryFilterDropdown(
                            label = "Danh mục",
                            value = selectedCategory,
                            options = listOf("Tất cả danh mục") + categories,
                            modifier = Modifier.weight(1f),
                            onValueChange = { selectedCategory = it; currentPage = 1 }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StockStatusFilterChip(
                            selected = selectedStatus == StockStatusFilter.ALL,
                            label = "Tất cả",
                            onClick = { selectedStatus = StockStatusFilter.ALL; currentPage = 1 }
                        )
                        StockStatusFilterChip(
                            selected = selectedStatus == StockStatusFilter.IN_STOCK,
                            label = "Còn hàng",
                            onClick = { selectedStatus = StockStatusFilter.IN_STOCK; currentPage = 1 }
                        )
                        StockStatusFilterChip(
                            selected = selectedStatus == StockStatusFilter.OUT,
                            label = "Hết hàng",
                            onClick = { selectedStatus = StockStatusFilter.OUT; currentPage = 1 }
                        )
                        StockStatusFilterChip(
                            selected = selectedStatus == StockStatusFilter.RESERVED,
                            label = "Có giữ chỗ",
                            onClick = { selectedStatus = StockStatusFilter.RESERVED; currentPage = 1 }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryPill("Tổng dòng", filteredItems.size.toString())
                    SummaryPill(
                        "Hiển thị",
                        if (filteredItems.isEmpty()) "0" else {
                            "${((currentPage - 1) * PAGE_SIZE + 1)}-${minOf(currentPage * PAGE_SIZE, filteredItems.size)}"
                        }
                    )
                    SummaryPill("Trang", "$currentPage/$pageCount")
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    title = "Danh sách tồn kho"
                ) {
                    InventoryContentState(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        isEmpty = filteredItems.isEmpty(),
                        emptyMessage = "Chưa có dữ liệu tồn kho",
                        onRetry = onRetry
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.widthIn(min = 1000.dp)) {
                                    InventoryTableHeader(
                                        sortBy = sortBy,
                                        sortAscending = sortAscending,
                                        onSortChange = {
                                            if (sortBy == it) {
                                                sortAscending = !sortAscending
                                            } else {
                                                sortBy = it
                                                sortAscending = true
                                            }
                                            currentPage = 1
                                        }
                                    )

                                    Divider(color = CrudColors.BorderColor)

                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(pageItems) { item ->
                                            InventoryTableRow(
                                                item = item,
                                                onDetailsClick = { selectedInventoryId = item.inventoryId }
                                            )
                                            Divider(color = CrudColors.BorderColor.copy(alpha = 0.8f))
                                        }
                                    }
                                }

                            }

                            Pagination(
                                currentPage = currentPage,
                                totalPages = pageCount,
                                onPageClick = { page -> currentPage = page },
                                onPreviousClick = { if (currentPage > 1) currentPage -= 1 },
                                onNextClick = { if (currentPage < pageCount) currentPage += 1 }
                            )
                        }
                    }

                }
            }
        }
    }

    selectedInventoryId?.let { inventoryId ->
        val selectedItem = allItems.firstOrNull { it.inventoryId == inventoryId }
        if (selectedItem != null) {
            InventoryDetailSheet(
                item = selectedItem,
                relatedItems = allItems.filter { it.productId == selectedItem.productId },
                productDetails = InventoryMockData.productDetails(selectedItem.productId).copy(
                    minStock = thresholdProducts.firstOrNull { it.productId == selectedItem.productId }?.minStock ?: 0,
                    maxStock = thresholdProducts.firstOrNull { it.productId == selectedItem.productId }?.maxStock ?: 0
                ),
                canUpdateReserved = canUpdateReserved,
                onDismiss = { selectedInventoryId = null },
                onSaveReserved = { updatedInventoryId, reservedQuantity ->
                    allItems = allItems.map { inventoryItem ->
                        if (inventoryItem.inventoryId == updatedInventoryId) {
                            inventoryItem.copy(
                                reservedQuantity = reservedQuantity,
                                availableQuantity = inventoryItem.stockQuantity - reservedQuantity,
                                updatedAt = java.time.LocalDateTime.now().toString()
                            )
                        } else {
                            inventoryItem
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryFilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
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
            label = { Text(label, fontSize = 13.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
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
private fun StockStatusFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = CrudColors.Primary.copy(alpha = 0.12f),
            selectedLabelColor = CrudColors.Primary,
            containerColor = Color.White,
            labelColor = CrudColors.TextPrimary
        )
    )
}

@Composable
private fun SummaryPill(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CrudColors.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, color = CrudColors.Primary, fontSize = 14.sp)
            Text(text = label, color = CrudColors.TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun InventoryTableHeader(
    sortBy: InventorySortBy,
    sortAscending: Boolean,
    onSortChange: (InventorySortBy) -> Unit
) {
    val sortIcon = if (sortAscending) "▲" else "▼"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrudColors.LightGreenBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(
            text = "Sản phẩm",
            weight = 2.1f,
            clickable = true,
            isSelected = sortBy == InventorySortBy.PRODUCT_NAME,
            trailing = if (sortBy == InventorySortBy.PRODUCT_NAME) sortIcon else "",
            onClick = { onSortChange(InventorySortBy.PRODUCT_NAME) }
        )
        TableCell(text = "Kho", weight = 1.2f)
        TableCell(text = "Mã lô", weight = 0.9f)
        TableCell(
            text = "Hạn sử dụng",
            weight = 1f,
            clickable = true,
            isSelected = sortBy == InventorySortBy.EXPIRY_DATE,
            trailing = if (sortBy == InventorySortBy.EXPIRY_DATE) sortIcon else "",
            onClick = { onSortChange(InventorySortBy.EXPIRY_DATE) }
        )
        TableCell(
            text = "Tồn thực tế",
            weight = 1f,
            clickable = true,
            isSelected = sortBy == InventorySortBy.STOCK_QUANTITY,
            trailing = if (sortBy == InventorySortBy.STOCK_QUANTITY) sortIcon else "",
            onClick = { onSortChange(InventorySortBy.STOCK_QUANTITY) }
        )
        TableCell(text = "Giữ chỗ", weight = 1f)
        TableCell(text = "Khả dụng", weight = 1f)
        TableCell(text = "Đơn vị", weight = 0.8f)
        TableCell(text = "Thao tác", weight = 0.8f)
    }
}

@Composable
private fun InventoryTableRow(item: InventoryItem, onDetailsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductCell(item)
        TableCell(text = item.warehouseName, weight = 1.2f)
        TableCell(text = InventoryFormatters.formatBatch(item.batchCode), weight = 0.9f)
        TableCell(text = InventoryFormatters.formatDate(item.expiryDate), weight = 1f, color = expiryTextColor(item.expiryStatus))
        TableCell(text = InventoryFormatters.formatNumber(item.stockQuantity), weight = 1f, color = CrudColors.TextPrimary, fontWeight = FontWeight.Bold)
        TableCell(text = InventoryFormatters.formatNumber(item.reservedQuantity), weight = 1f)
        TableCell(text = InventoryFormatters.formatNumber(item.availableQuantity), weight = 1f, color = CrudColors.Primary, fontWeight = FontWeight.Bold)
        TableCell(text = item.unit, weight = 0.8f)
        TableCell(
            text = "Chi tiết",
            weight = 0.8f,
            isAction = true,
            onClick = onDetailsClick
        )
    }
}

@Composable
private fun RowScope.ProductCell(item: InventoryItem) {
    Row(
        modifier = Modifier.weight(2.1f).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InventoryProductThumbnail(
            imageUrl = item.imageUrl,
            contentDescription = item.productName,
            modifier = Modifier.size(44.dp)
        )
        Column {
            Text(
                item.sku,
                color = CrudColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.productName,
                color = CrudColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color = CrudColors.TextPrimary,
    fontWeight: FontWeight = FontWeight.Normal,
    isTitle: Boolean = false,
    isAction: Boolean = false,
    clickable: Boolean = false,
    isSelected: Boolean = false,
    trailing: String = "",
    onClick: (() -> Unit)? = null
) {
    val fontSize = if (isTitle) 13.sp else 12.sp
    val cellModifier = Modifier.weight(weight).padding(horizontal = 4.dp)

    Box(
        modifier = if (clickable) cellModifier.clickable { onClick?.invoke() } else cellModifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (isAction) {
            Button(
                onClick = { onClick?.invoke() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrudColors.Primary.copy(alpha = 0.12f),
                    contentColor = CrudColors.Primary
                ),
                modifier = Modifier.heightIn(min = 32.dp)
            ) {
                Text(text, fontSize = 11.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        color = if (isSelected) CrudColors.Primary else color,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (trailing.isNotEmpty()) {
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = trailing,
                            color = CrudColors.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun expiryTextColor(status: ExpiryStatus): Color = when (status) {
    ExpiryStatus.EXPIRED -> Color(0xFFD32F2F)
    ExpiryStatus.NEAR_EXPIRY -> Color(0xFFF59E0B)
    ExpiryStatus.OK -> CrudColors.OkText
    ExpiryStatus.NONE -> CrudColors.TextSecondary
}
