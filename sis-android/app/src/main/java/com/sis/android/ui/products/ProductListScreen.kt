package com.sis.android.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Product
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import com.sis.android.ui.viewmodel.ProductUiState
import com.sis.android.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = CrudColors.HeaderBackground,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sản phẩm")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header rêu đậm với hiệu ứng chuyển màu (Gradient) chuẩn như Web Login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrudColors.PrimaryGradientBrush)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "THỨ TƯ, 25/06/2025", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Kho hàng", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Theo dõi sản phẩm và tồn kho", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = onScanClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Quét mã", tint = Color.White)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thanh tìm kiếm & lọc
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tìm tên, SKU hoặc mã vạch...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = CrudStyles.InputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CrudColors.CardBg,
                            focusedContainerColor = CrudColors.CardBg,
                            unfocusedBorderColor = CrudColors.BorderColor,
                            focusedBorderColor = CrudColors.HeaderBackground
                        )
                    )
                    IconButton(
                        onClick = { /* Bộ lọc */ },
                        modifier = Modifier
                            .clip(CrudStyles.InputShape)
                            .background(CrudColors.CardBg)
                            .size(56.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Lọc", tint = CrudColors.HeaderBackground)
                    }
                }

                // Các nút Tab nhanh & Thẻ thống kê (Chỉ hiển thị khi Success)
                if (uiState is ProductUiState.Success) {
                    val state = uiState as ProductUiState.Success
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedTab == "ALL",
                            onClick = { viewModel.setSelectedTab("ALL") },
                            label = { Text("Tất cả (${state.products.size})") }
                        )
                        FilterChip(
                            selected = selectedTab == "LOW_STOCK",
                            onClick = { viewModel.setSelectedTab("LOW_STOCK") },
                            label = { Text("Tồn thấp (${state.lowStockCount})") }
                        )
                        FilterChip(
                            selected = selectedTab == "EXPIRING",
                            onClick = { viewModel.setSelectedTab("EXPIRING") },
                            label = { Text("Cận hạn (${state.nearExpiryCount})") }
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrudColors.HeaderBackground),
                        shape = CrudStyles.CardShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProductListStatItem("Tổng tồn", "${state.totalStock.toInt()}")
                            ProductListStatItem("Giữ chỗ", "${state.totalReserved.toInt()}")
                            ProductListStatItem("Khả dụng", "${state.totalAvailable.toInt()}")
                        }
                    }
                }

                // Danh sách / Trạng thái
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is ProductUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = CrudColors.HeaderBackground)
                        }
                        is ProductUiState.Error -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadProducts() }) {
                                    Text("Thử lại")
                                }
                            }
                        }
                        is ProductUiState.Success -> {
                            if (state.products.isEmpty()) {
                                Text(
                                    text = "Không tìm thấy sản phẩm phù hợp!",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CrudColors.TextSecondary
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.products) { product ->
                                        ProductListItemCard(product = product, onClick = { onProductClick(product) })
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
private fun ProductListStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProductListItemCard(product: Product, onClick: () -> Unit) {
    val isLowStock = product.availableQuantity <= product.minStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CrudColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = CrudStyles.ItemCardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CrudColors.HeaderBackground.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val initials = product.productName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                Text(text = initials, color = CrudColors.HeaderBackground, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CrudColors.TextPrimary)
                Text(
                    text = "${product.sku} · ${product.warehouseName}",
                    color = CrudColors.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${product.availableQuantity.toInt()} ${product.unit} khả dụng",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = CrudColors.HeaderBackground
                    )
                    Surface(
                        color = if (isLowStock) CrudColors.StatusInactiveBg else CrudColors.StatusActiveBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isLowStock) "● Tồn thấp" else "● Ổn định",
                            color = if (isLowStock) CrudColors.StatusInactiveText else CrudColors.StatusActiveText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}