package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.ExpiryStatus
import com.sis.android.data.model.InventoryItem
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.form.ChipRow
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import com.sis.android.ui.viewmodel.InventoryUiState
import com.sis.android.ui.viewmodel.InventoryViewModel
// Reuse UI components from Product module
import com.sis.android.ui.products.StockFilterChip
import com.sis.android.ui.products.ProductStatItem
// Explicit import to resolve OutlinedTextField overload ambiguity
import androidx.compose.material3.OutlinedTextField as Material3OutlinedTextField

/**
 * UC29: Màn hình Danh sách Tồn kho
 * Layout 100% giống ProductListScreen để đồng nhất UI
 */
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onGoToProducts: () -> Unit,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.inventoryUiState.collectAsState()
    val searchQuery by viewModel.inventorySearchQuery.collectAsState()
    
    // Backend StockStatus: ALL, IN_STOCK, OUT_OF_STOCK, HAS_RESERVED
    var selectedStockFilter by remember { mutableStateOf("ALL") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Auto-reload when filter changes
    LaunchedEffect(searchQuery, selectedStockFilter) {
        viewModel.loadInventory(
            keyword = searchQuery.ifBlank { null },
            stockStatus = selectedStockFilter
        )
    }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // Header with menu button
            AppHeader(
                title = "Tồn kho",
                subtitle = "Quản lý tồn kho theo kho, lô, hạn sử dụng",
                onMenu = onMenuClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search bar + Filter button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Material3OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setInventorySearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                "Tìm theo SKU, tên sản phẩm...",
                                fontSize = 14.sp,
                                color = CrudColors.TextSecondary
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = CrudColors.TextSecondary
                            ) 
                        },
                        singleLine = true,
                        shape = CrudStyles.InputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = CrudColors.BorderColor,
                            focusedBorderColor = CrudColors.Primary,
                            cursorColor = CrudColors.Primary
                        )
                    )
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CrudStyles.InputShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Lọc",
                            tint = CrudColors.Primary
                        )
                    }
                }

                // Filter chips - Backend StockStatus
                ChipRow {
                    StockFilterChip(
                        selected = selectedStockFilter == "ALL",
                        text = "Tất cả",
                        onClick = { selectedStockFilter = "ALL" }
                    )
                    StockFilterChip(
                        selected = selectedStockFilter == "IN_STOCK",
                        text = "Còn hàng",
                        onClick = { selectedStockFilter = "IN_STOCK" }
                    )
                    StockFilterChip(
                        selected = selectedStockFilter == "OUT_OF_STOCK",
                        text = "Hết hàng",
                        onClick = { selectedStockFilter = "OUT_OF_STOCK" }
                    )
                    StockFilterChip(
                        selected = selectedStockFilter == "HAS_RESERVED",
                        text = "Có giữ chỗ",
                        onClick = { selectedStockFilter = "HAS_RESERVED" }
                    )
                }

                // Summary stats - Tổng tồn, Giữ chỗ, Khả dụng
                when (val state = uiState) {
                    is InventoryUiState.Success -> {
                        val totalStock = state.items.sumOf { it.stockQuantity }
                        val totalReserved = state.items.sumOf { it.reservedQuantity }
                        val totalAvailable = state.items.sumOf { it.availableQuantity }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CrudStyles.CardShape)
                                .background(CrudColors.StatBrush)
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductStatItem("Tổng tồn", totalStock.toString())
                                VerticalDivider(
                                    Modifier.height(36.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                )
                                ProductStatItem("Giữ chỗ", totalReserved.toString())
                                VerticalDivider(
                                    Modifier.height(36.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                )
                                ProductStatItem("Khả dụng", totalAvailable.toString())
                            }
                        }
                    }
                    else -> { /* No stats when loading or error */ }
                }

                // Inventory list
                AppCard(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    title = "Danh sách tồn kho"
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = uiState) {
                            is InventoryUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CrudColors.Primary
                                )
                            }
                            is InventoryUiState.Error -> {
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
                                        onClick = { viewModel.loadInventory() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CrudColors.Primary
                                        )
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                            is InventoryUiState.Success -> {
                                if (state.items.isEmpty()) {
                                    Text(
                                        "Không có dữ liệu tồn kho!",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        color = CrudColors.TextSecondary
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(12.dp)
                                    ) {
                                        itemsIndexed(
                                            items = state.items,
                                            key = { _, item -> item.inventoryId }
                                        ) { _, item ->
                                            InventoryRowItem(
                                                item = item,
                                                onClick = { /* TODO: Show detail sheet */ }
                                            )
                                        }
                                        
                                        // Pagination at bottom of list
                                        item {
                                            val currentPageValue by viewModel.currentInventoryPageState.collectAsState()
                                            val totalPagesValue by viewModel.totalInventoryPagesState.collectAsState()
                                            
                                            Pagination(
                                                currentPage = currentPageValue,
                                                totalPages = totalPagesValue,
                                                onPageClick = { page -> viewModel.goToInventoryPage(page) },
                                                onPreviousClick = { viewModel.previousInventoryPage() },
                                                onNextClick = { viewModel.nextInventoryPage() }
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

    // TODO: Filter bottom sheet
    if (showFilterSheet) {
        showFilterSheet = false
    }
}

/**
 * Component hiển thị một dòng tồn kho với đầy đủ thông tin kho, lô, hạn dùng
 */
@Composable
private fun InventoryRowItem(
    item: InventoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = CrudStyles.CardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product thumbnail
            InventoryProductThumbnail(
                imageUrl = item.imageUrl,
                contentDescription = item.productName,
                modifier = Modifier.size(60.dp)
            )
            
            // Product info + Inventory details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Product name
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CrudColors.TextPrimary
                    ),
                    maxLines = 2
                )
                
                // SKU + Warehouse name
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.sku,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CrudColors.TextSecondary
                        )
                    )
                    Text("•", color = CrudColors.TextSecondary, fontSize = 10.sp)
                    Text(
                        text = item.warehouseName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CrudColors.Primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                // Batch + Expiry info
                if (!item.noBatch || item.expiryDate != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.noBatch) {
                            Surface(
                                color = CrudColors.BorderColor.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Lô: ${item.displayBatch}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = CrudColors.TextSecondary
                                )
                            }
                        }
                        
                        item.expiryDate?.let { expiry ->
                            val (color, label) = when (item.expiryStatus) {
                                ExpiryStatus.EXPIRED -> CrudColors.DangerAction to "Đã hết hạn"
                                ExpiryStatus.NEAR_EXPIRY -> CrudColors.WarnText to "Gần hết hạn"
                                ExpiryStatus.OK -> CrudColors.OkText to "HSD: $expiry"
                                ExpiryStatus.NONE -> CrudColors.TextSecondary to ""
                            }
                            if (label.isNotEmpty()) {
                                Surface(
                                    color = color.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        color = color,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Stock quantities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stock quantity
                    Column {
                        Text(
                            text = "Tồn kho",
                            fontSize = 10.sp,
                            color = CrudColors.TextSecondary
                        )
                        Text(
                            text = "${item.stockQuantity} ${item.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (item.stockQuantity == 0) 
                                    CrudColors.DangerAction 
                                else 
                                    CrudColors.TextPrimary
                            )
                        )
                    }
                    
                    // Reserved quantity
                    if (item.reservedQuantity > 0) {
                        Column {
                            Text(
                                text = "Giữ chỗ",
                                fontSize = 10.sp,
                                color = CrudColors.TextSecondary
                            )
                            Text(
                                text = "${item.reservedQuantity} ${item.unit}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CrudColors.WarnText
                                )
                            )
                        }
                    }
                    
                    // Available quantity
                    Column {
                        Text(
                            text = "Khả dụng",
                            fontSize = 10.sp,
                            color = CrudColors.TextSecondary
                        )
                        Text(
                            text = "${item.availableQuantity} ${item.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CrudColors.Primary
                            )
                        )
                    }
                }
            }
        }
    }
}
