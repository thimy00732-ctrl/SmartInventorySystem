package com.sis.android.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.ValuationDetail
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.viewmodel.InventoryViewModel
import com.sis.android.ui.viewmodel.ValuationUiState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * UC39: Màn hình Định giá Tồn kho
 * Hiển thị giá trị tồn kho theo giá vốn (chỉ ADMIN)
 */
@Composable
fun InventoryValuationScreen(
    viewModel: InventoryViewModel,
    userRole: InventoryUserRole,
    onBack: () -> Unit
) {
    val valuationUiState by viewModel.valuationUiState.collectAsState()

    // Check permission
    if (!InventoryRolePolicy.canViewValuation(userRole)) {
        Scaffold(
            containerColor = CrudColors.LightGreenBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Bạn không có quyền xem giá trị tồn kho.",
                    color = CrudColors.DangerAction,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    // Load valuation on first composition
    LaunchedEffect(Unit) {
        viewModel.loadValuation()
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
                title = "Giá trị tồn kho",
                subtitle = "Tổng hợp theo giá vốn hiện tại",
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
                // Warning card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrudColors.WarnBg),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "⚠️ Giá trị tính theo giá vốn hiện tại, không phản ánh giá vốn lịch sử.",
                        modifier = Modifier.padding(12.dp),
                        color = CrudColors.WarnText,
                        fontSize = 13.sp
                    )
                }

                // Content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = valuationUiState) {
                        is ValuationUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = CrudColors.Primary
                            )
                        }
                        is ValuationUiState.Error -> {
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
                                    onClick = { viewModel.loadValuation() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrudColors.Primary
                                    )
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                        is ValuationUiState.Success -> {
                            state.summary?.let { summary ->
                                ValuationContent(
                                    viewModel = viewModel,
                                    totalQuantity = summary.totalQuantity,
                                    totalValue = state.total,
                                    details = state.items
                                )
                            } ?: run {
                                // Show items without summary
                                ValuationContent(
                                    viewModel = viewModel,
                                    totalQuantity = state.items.sumOf { it.totalQuantity },
                                    totalValue = state.total,
                                    details = state.items
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValuationContent(
    viewModel: InventoryViewModel,
    totalQuantity: Int,
    totalValue: BigDecimal,
    details: List<ValuationDetail>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = "Tổng số lượng",
                value = formatNumber(totalQuantity),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Tổng giá trị",
                value = formatCurrency(totalValue),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Sản phẩm",
                value = details.distinctBy { it.productId }.size.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Details list
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium
        ) {
            if (details.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Không có dữ liệu!",
                        color = CrudColors.TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(details) { detail ->
                        ValuationDetailCard(detail)
                    }
                    
                    // Pagination at bottom of list
                    item {
                        val currentPageValue by viewModel.currentValuationPageState.collectAsState()
                        val totalPagesValue by viewModel.totalValuationPagesState.collectAsState()
                        
                        Pagination(
                            currentPage = currentPageValue,
                            totalPages = totalPagesValue,
                            onPageClick = { page -> viewModel.goToValuationPage(page) },
                            onPreviousClick = { viewModel.previousValuationPage() },
                            onNextClick = { viewModel.nextValuationPage() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CrudColors.Primary
            )
            Text(
                label,
                fontSize = 12.sp,
                color = CrudColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ValuationDetailCard(detail: ValuationDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Product info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        detail.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CrudColors.TextPrimary
                    )
                    Text(
                        detail.sku,
                        fontSize = 12.sp,
                        color = CrudColors.TextSecondary
                    )
                }
            }

            Divider(color = CrudColors.BorderColor)

            // Warehouse and quantities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Kho", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        detail.warehouseName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = CrudColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Số lượng", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        formatNumber(detail.totalQuantity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = CrudColors.TextPrimary
                    )
                }
            }

            // Pricing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Giá vốn hiện tại", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        formatCurrency(detail.currentCostPrice),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = CrudColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Giá trị tồn", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        formatCurrency(detail.stockValue),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CrudColors.Primary
                    )
                }
            }
        }
    }
}

// Helper formatting functions
private fun formatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(value)
}

private fun formatCurrency(value: BigDecimal): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(value)
}
