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
import com.sis.android.ui.components.form.ChipRow
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import com.sis.android.ui.viewmodel.ProductUiState
import com.sis.android.ui.viewmodel.ProductViewModel

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
            AppHeader(
                title = "Kho hàng",
                subtitle = "Theo dõi sản phẩm và tồn kho",
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
                        onClick = { /* Lọc nâng cao */ },
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
                                    // LazyColumn tự quản lý padding bên trong Card mà không cần tham số contentPadding của AppCard
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        itemsIndexed(state.products) { index, product ->
                                            ProductRowItem(product) { onProductClick(product) }
                                            if (index < state.products.lastIndex) {
                                                HorizontalDivider(color = CrudColors.BorderColor)
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
}