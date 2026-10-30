package com.sis.android.ui.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ===== Header gradient chéo, phủ lên status bar =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CrudColors.HeaderBrush,
                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("THỨ TƯ, 25/06/2025", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Kho hàng", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text("Theo dõi sản phẩm và tồn kho", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm sản phẩm", tint = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== Tìm kiếm + lọc =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Tìm tên, SKU hoặc mã vạch...", fontSize = 14.sp, color = CrudColors.TextSecondary)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CrudColors.TextSecondary)
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
                        onClick = { /* Bộ lọc */ },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CrudStyles.InputShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Lọc", tint = CrudColors.Primary)
                    }
                }

                // ===== Chip + thẻ thống kê =====
                val current = uiState
                if (current is ProductUiState.Success) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StockChip(selectedTab == "ALL", "Tất cả ${current.products.size}") {
                            viewModel.setSelectedTab("ALL")
                        }
                        StockChip(selectedTab == "LOW_STOCK", "Tồn thấp ${current.lowStockCount}") {
                            viewModel.setSelectedTab("LOW_STOCK")
                        }
                        StockChip(selectedTab == "EXPIRING", "Cận hạn ${current.nearExpiryCount}") {
                            viewModel.setSelectedTab("EXPIRING")
                        }
                    }

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
                            ProductListStatItem("Tổng tồn", "${current.totalStock.toInt()}")
                            VerticalDivider(Modifier.height(36.dp), color = Color.White.copy(alpha = 0.25f))
                            ProductListStatItem("Giữ chỗ", "${current.totalReserved.toInt()}")
                            VerticalDivider(Modifier.height(36.dp), color = Color.White.copy(alpha = 0.25f))
                            ProductListStatItem("Khả dụng", "${current.totalAvailable.toInt()}")
                        }
                    }
                }

                // ===== Danh sách trong 1 thẻ trắng lớn =====
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, CrudColors.BorderColor),
                    shape = CrudStyles.CardShape,
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
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(state.message, color = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.loadProducts() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
                                    ) { Text("Thử lại") }
                                }
                            }

                            is ProductUiState.Success -> {
                                if (state.products.isEmpty()) {
                                    Text(
                                        "Không tìm thấy sản phẩm phù hợp!",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = CrudColors.TextSecondary
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp
                                        )
                                    ) {
                                        itemsIndexed(state.products) { index, product ->
                                            ProductRow(product) { onProductClick(product) }
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

@Composable
private fun StockChip(selected: Boolean, text: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = CrudColors.TextPrimary,
            selectedContainerColor = CrudColors.Primary,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = CrudColors.BorderColor,
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun ProductListStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProductRow(product: Product, onClick: () -> Unit) {
    val low = product.availableQuantity <= product.minStock
    val initials = product.productName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrudColors.AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = CrudColors.AvatarText, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.productName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CrudColors.TextPrimary
            )
            Text(
                "${product.sku} · ${product.warehouseName}",
                fontSize = 11.sp,
                color = CrudColors.TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${product.availableQuantity.toInt()} ${product.unit} khả dụng",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = CrudColors.TextPrimary
            )
        }

        Surface(
            color = if (low) CrudColors.WarnBg else CrudColors.OkBg,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                if (low) "● Tồn thấp" else "● Ổn định",
                color = if (low) CrudColors.WarnText else CrudColors.OkText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}