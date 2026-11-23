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
import com.sis.android.data.model.StockLedgerEntry
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.viewmodel.InventoryViewModel
import com.sis.android.ui.viewmodel.StockLedgerUiState

/**
 * UC36: Màn hình Sổ Ghi nhận Tồn kho
 * Hiển thị tổng hợp nhập/xuất/chuyển/điều chỉnh theo sản phẩm và kho
 */
@Composable
fun StockLedgerScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val stockLedgerUiState by viewModel.stockLedgerUiState.collectAsState()
    
    // Date range for ledger - default last 30 days
    var fromDate by remember { 
        val date = java.time.LocalDate.now().minusDays(30)
        mutableStateOf(date.toString()) // yyyy-MM-dd
    }
    var toDate by remember { 
        val date = java.time.LocalDate.now()
        mutableStateOf(date.toString()) // yyyy-MM-dd
    }

    // Load stock ledger on first composition
    LaunchedEffect(fromDate, toDate) {
        viewModel.loadStockLedger(
            fromDate = fromDate,
            toDate = toDate
        )
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
                title = "Sổ ghi nhận tồn kho",
                subtitle = "Lịch sử nhập xuất và điều chỉnh",
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
                        when (val state = stockLedgerUiState) {
                            is StockLedgerUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CrudColors.Primary
                                )
                            }
                            is StockLedgerUiState.Error -> {
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
                                        onClick = { 
                                            viewModel.loadStockLedger(
                                                fromDate = fromDate,
                                                toDate = toDate
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CrudColors.Primary
                                        )
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                            is StockLedgerUiState.Success -> {
                                if (state.entries.isEmpty()) {
                                    Text(
                                        "Chưa có giao dịch nào!",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        color = CrudColors.TextSecondary
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.entries) { entry ->
                                            StockLedgerEntryCard(entry)
                                        }
                                        
                                        // Pagination at bottom of list
                                        item {
                                            val currentPageValue by viewModel.currentStockLedgerPageState.collectAsState()
                                            val totalPagesValue by viewModel.totalStockLedgerPagesState.collectAsState()
                                            
                                            Pagination(
                                                currentPage = currentPageValue,
                                                totalPages = totalPagesValue,
                                                onPageClick = { page -> 
                                                    viewModel.goToStockLedgerPage(page, fromDate, toDate)
                                                },
                                                onPreviousClick = { 
                                                    viewModel.previousStockLedgerPage(fromDate, toDate)
                                                },
                                                onNextClick = { 
                                                    viewModel.nextStockLedgerPage(fromDate, toDate)
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
private fun StockLedgerEntryCard(entry: StockLedgerEntry) {
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
                        entry.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CrudColors.TextPrimary
                    )
                    Text(
                        "${entry.sku} • ${entry.warehouseName}",
                        fontSize = 12.sp,
                        color = CrudColors.TextSecondary
                    )
                }
            }

            Divider(color = CrudColors.BorderColor)

            // Stock movements summary
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StockMovementRow("Tồn đầu kỳ", entry.openingQty, CrudColors.TextSecondary)
                if (entry.importQty.toInt() > 0) {
                    StockMovementRow("Nhập kho", entry.importQty, CrudColors.OkText)
                }
                if (entry.exportQty.toInt() > 0) {
                    StockMovementRow("Xuất kho", entry.exportQty, CrudColors.DangerAction)
                }
                if (entry.transferInQty.toInt() > 0) {
                    StockMovementRow("Chuyển đến", entry.transferInQty, CrudColors.Primary)
                }
                if (entry.transferOutQty.toInt() > 0) {
                    StockMovementRow("Chuyển đi", entry.transferOutQty, CrudColors.WarnText)
                }
                if (entry.adjustNet.toInt() != 0) {
                    StockMovementRow(
                        "Điều chỉnh",
                        entry.adjustNet,
                        if (entry.adjustNet.toInt() >= 0) CrudColors.OkText else CrudColors.DangerAction
                    )
                }
                Divider(color = CrudColors.BorderColor.copy(alpha = 0.5f))
                StockMovementRow("Tồn cuối kỳ", entry.closingQty, CrudColors.Primary, isBold = true)
            }
        }
    }
}

@Composable
private fun StockMovementRow(
    label: String,
    quantity: java.math.BigDecimal,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = CrudColors.TextSecondary,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            quantity.toString(),
            fontSize = 13.sp,
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
