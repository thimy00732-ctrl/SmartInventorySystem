package com.sis.android.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Product
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.form.ChipRow
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import com.sis.android.ui.viewmodel.ProductUiState
import com.sis.android.ui.viewmodel.ProductViewModel
import com.sis.android.ui.components.ProductFilterBottomSheet
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onProductClick: (Product) -> Unit,
    onAddClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val filterOptions = viewModel.filterOptions.collectAsState().value

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanClick,
                containerColor = CrudColors.Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Quét mã")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            val selectedWarehouseName by viewModel.selectedWarehouseName.collectAsState()

            AppHeader(
                title = if (selectedWarehouseName != null) "📦 $selectedWarehouseName" else "Kho hàng",
                subtitle = if (selectedWarehouseName != null) "Xem sản phẩm theo kho" else "Theo dõi sản phẩm và tồn kho",
                actions = {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color.White)
                    }
                }
            )


            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ô tìm kiếm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tìm tên, SKU hoặc mã vạch...", fontSize = 14.sp, color = CrudColors.TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrudColors.TextSecondary) },
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
                        modifier = Modifier.size(56.dp).clip(CrudStyles.InputShape).background(Color.White)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Lọc", tint = CrudColors.Primary)
                    }
                }

                val current = uiState
                if (current is ProductUiState.Success) {
                    ChipRow {
                        StockFilterChip(selectedTab == "ALL", "Tất cả (${current.products.size})") { viewModel.setSelectedTab("ALL") }
                        StockFilterChip(selectedTab == "LOW_STOCK", "Tồn thấp (${current.lowStockCount})") { viewModel.setSelectedTab("LOW_STOCK") }
                        StockFilterChip(selectedTab == "EXPIRING", "Cận hạn (${current.nearExpiryCount})") { viewModel.setSelectedTab("EXPIRING") }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().clip(CrudStyles.CardShape).background(CrudColors.StatBrush).padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductStatItem("Tổng tồn", "${current.totalStock.toInt()}")
                            VerticalDivider(Modifier.height(36.dp), color = Color.White.copy(alpha = 0.25f))
                            ProductStatItem("Giữ chỗ", "${current.totalReserved.toInt()}")
                            VerticalDivider(Modifier.height(36.dp), color = Color.White.copy(alpha = 0.25f))
                            ProductStatItem("Khả dụng", "${current.totalAvailable.toInt()}")
                        }
                    }
                }

                // ===== Danh sách sản phẩm bọc trong AppCard =====
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = uiState) {
                            is ProductUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CrudColors.Primary
                                )
                            }
                            is ProductUiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(state.message, color = CrudColors.DangerAction)
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.loadProducts() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                            is ProductUiState.Success -> {
                                if (state.products.isEmpty()) {
                                    Text(
                                        "Không tìm thấy sản phẩm phù hợp!",
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
                                            items = state.products,
                                            key = { _, product -> product.productId }
                                        ) { _, product ->
                                            ProductRowItem(
                                                product = product,
                                                onClick = { onProductClick(product) }
                                            )
                                        }

                                        // Phân trang ở cuối danh sách
                                        item {
                                            val currentPageValue by viewModel.currentPageState.collectAsState()
                                            val totalPagesValue by viewModel.totalPagesState.collectAsState()
                                            
                                            Pagination(
                                                currentPage = currentPageValue,
                                                totalPages = totalPagesValue,
                                                onPageClick = { page -> viewModel.goToPage(page) },
                                                onPreviousClick = { viewModel.previousPage() },
                                                onNextClick = { viewModel.nextPage() }
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
    // Hiển thị bottom sheet filter

    if (showFilterSheet) {
        filterOptions?.let { options ->
            ProductFilterBottomSheet(
                warehouses = options.warehouses,
                categories = options.categories,
                suppliers = options.suppliers,
                sortByOptions = options.sortByOptions,
                sortDirectionOptions = options.sortDirectionOptions,
                onApplyFilter = { warehouseId, categoryId, supplierId, isActive, sortBy, sortDirection ->
                    viewModel.applyFilter(
                        keyword = searchQuery.ifBlank { null },
                        warehouseId = warehouseId,
                        categoryId = categoryId,
                        supplierId = supplierId,
                        isActive = isActive,
                        sortBy = sortBy,
                        sortDirection = sortDirection
                    )
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }



}