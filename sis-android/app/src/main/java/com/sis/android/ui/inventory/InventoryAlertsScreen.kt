package com.sis.android.ui.inventory

import androidx.compose.foundation.background
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
import com.sis.android.data.model.LowStockAlert
import com.sis.android.data.model.OverStockAlert
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.display.Pagination
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.viewmodel.AlertUiState
import com.sis.android.ui.viewmodel.InventoryViewModel

/**
 * UC34-35: Màn hình Cảnh báo Tồn kho
 * Tabs: Tồn thấp / Vượt mức
 */
@Composable
fun InventoryAlertsScreen(
    viewModel: InventoryViewModel,
    canManageThresholds: Boolean,
    onBack: () -> Unit,
    onGoToThresholds: () -> Unit
) {
    val alertUiState by viewModel.alertUiState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) } // 0=Low, 1=Over
    
    // Client-side pagination state
    var currentLowStockPage by remember { mutableStateOf(1) }
    var currentOverStockPage by remember { mutableStateOf(1) }
    val pageSize = 10

    // Load alerts on first composition
    LaunchedEffect(Unit) {
        viewModel.loadAlerts()
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
                title = "Cảnh báo tồn kho",
                subtitle = "Theo dõi sản phẩm tồn thấp và vượt mức",
                actions = {
                    if (canManageThresholds) {
                        TextButton(onClick = onGoToThresholds) {
                            Text("Ngưỡng", color = Color.White, fontSize = 13.sp)
                        }
                    }
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
                // Summary card
                when (val state = alertUiState) {
                    is AlertUiState.Success -> {
                        state.summary?.let { summary ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${summary.lowStockCount}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CrudColors.WarnText
                                        )
                                        Text("Tồn thấp", fontSize = 12.sp, color = CrudColors.TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${summary.outOfStockCount}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CrudColors.DangerAction
                                        )
                                        Text("Hết hàng", fontSize = 12.sp, color = CrudColors.TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${summary.overStockCount}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CrudColors.Primary
                                        )
                                        Text("Vượt mức", fontSize = 12.sp, color = CrudColors.TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                    else -> { /* No summary when loading/error */ }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = CrudColors.Primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Tồn thấp") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Vượt mức") }
                    )
                }

                // Content list
                AppCard(
                    modifier = Modifier.fillMaxSize(),
                    title = if (selectedTab == 0) "Danh sách tồn thấp" else "Danh sách vượt mức"
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = alertUiState) {
                            is AlertUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CrudColors.Primary
                                )
                            }
                            is AlertUiState.Error -> {
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
                                        onClick = { viewModel.loadAlerts() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CrudColors.Primary
                                        )
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                            is AlertUiState.Success -> {
                                if (selectedTab == 0) {
                                    // Low stock tab with client-side pagination
                                    val totalLowStockPages = (state.lowStockAlerts.size + pageSize - 1) / pageSize
                                    val startIndex = (currentLowStockPage - 1) * pageSize
                                    val endIndex = minOf(startIndex + pageSize, state.lowStockAlerts.size)
                                    val currentPageItems = state.lowStockAlerts.subList(startIndex, endIndex)
                                    
                                    if (state.lowStockAlerts.isEmpty()) {
                                        Text(
                                            "Không có sản phẩm tồn thấp!",
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
                                            items(currentPageItems) { alert ->
                                                LowStockAlertCard(alert)
                                            }
                                            
                                            // Client-side pagination
                                            item {
                                                Pagination(
                                                    currentPage = currentLowStockPage,
                                                    totalPages = totalLowStockPages,
                                                    onPageClick = { page -> currentLowStockPage = page },
                                                    onPreviousClick = { 
                                                        if (currentLowStockPage > 1) currentLowStockPage--
                                                    },
                                                    onNextClick = { 
                                                        if (currentLowStockPage < totalLowStockPages) currentLowStockPage++
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Over stock tab with client-side pagination
                                    val totalOverStockPages = (state.overStockAlerts.size + pageSize - 1) / pageSize
                                    val startIndex = (currentOverStockPage - 1) * pageSize
                                    val endIndex = minOf(startIndex + pageSize, state.overStockAlerts.size)
                                    val currentPageItems = state.overStockAlerts.subList(startIndex, endIndex)
                                    
                                    if (state.overStockAlerts.isEmpty()) {
                                        Text(
                                            "Không có sản phẩm vượt mức!",
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
                                            items(currentPageItems) { alert ->
                                                OverStockAlertCard(alert)
                                            }
                                            
                                            // Client-side pagination
                                            item {
                                                Pagination(
                                                    currentPage = currentOverStockPage,
                                                    totalPages = totalOverStockPages,
                                                    onPageClick = { page -> currentOverStockPage = page },
                                                    onPreviousClick = { 
                                                        if (currentOverStockPage > 1) currentOverStockPage--
                                                    },
                                                    onNextClick = { 
                                                        if (currentOverStockPage < totalOverStockPages) currentOverStockPage++
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
}

@Composable
private fun LowStockAlertCard(alert: LowStockAlert) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        alert.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CrudColors.TextPrimary
                    )
                    Text(
                        alert.sku,
                        fontSize = 12.sp,
                        color = CrudColors.TextSecondary
                    )
                }
                Surface(
                    color = if (alert.totalStock == 0) 
                        CrudColors.DangerAction.copy(alpha = 0.1f)
                    else 
                        CrudColors.WarnBg,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (alert.totalStock == 0) "Hết hàng" else "Tồn thấp",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (alert.totalStock == 0) 
                            CrudColors.DangerAction 
                        else 
                            CrudColors.WarnText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(color = CrudColors.BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tồn hiện tại", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        "${alert.totalStock} ${alert.unit}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (alert.totalStock == 0) CrudColors.DangerAction else CrudColors.TextPrimary
                    )
                }
                Column {
                    Text("Tồn tối thiểu", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        "${alert.minStock} ${alert.unit}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text("Số lượng thiếu", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        "${alert.missingQuantity} ${alert.unit}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = CrudColors.WarnText
                    )
                }
            }

            // Show supplier info for ordering
            alert.supplierName?.let { supplier ->
                Text(
                    "Nhà cung cấp: $supplier",
                    fontSize = 12.sp,
                    color = CrudColors.TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrudColors.WarnBg.copy(alpha = 0.3f))
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun OverStockAlertCard(alert: OverStockAlert) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        alert.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CrudColors.TextPrimary
                    )
                    Text(
                        alert.sku,
                        fontSize = 12.sp,
                        color = CrudColors.TextSecondary
                    )
                }
                Surface(
                    color = CrudColors.Primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "Vượt mức",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = CrudColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(color = CrudColors.BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tồn hiện tại", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        "${alert.totalStock} ${alert.unit}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = CrudColors.Primary
                    )
                }
                Column {
                    Text("Tồn tối đa", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        "${alert.maxStock} ${alert.unit}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text("Số lượng vượt", fontSize = 11.sp, color = CrudColors.TextSecondary)
                    Text(
                        "${alert.excessQuantity} ${alert.unit}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = CrudColors.Primary
                    )
                }
            }

            // Show excess percentage if available
            alert.excessPercent?.let { percent ->
                Text(
                    "Vượt mức: $percent%",
                    fontSize = 12.sp,
                    color = CrudColors.TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrudColors.Primary.copy(alpha = 0.1f))
                        .padding(8.dp)
                )
            }
        }
    }
}
