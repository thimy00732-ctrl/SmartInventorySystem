package com.sis.android.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Product
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onToggleActiveClick: (Product) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = CrudColors.DangerAction)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CrudColors.CardBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditClick(product) },
                containerColor = CrudColors.HeaderBackground,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header áp dụng hiệu ứng chuyển màu (Gradient) rêu đậm chuẩn mẫu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrudColors.PrimaryGradientBrush)
                    .padding(20.dp)
            ) {
                Column {
                    Text(text = "THỨ TƯ, 25/06/2025", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = product.productName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${product.sku} · ${product.warehouseName}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrudColors.CardBg),
                    shape = CrudStyles.CardShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Trạng thái kinh doanh", fontWeight = FontWeight.Medium, color = CrudColors.TextPrimary)
                            Surface(
                                color = if (product.isActive == 1) CrudColors.StatusActiveBg else CrudColors.StatusInactiveBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "● ${product.statusText}",
                                    color = if (product.isActive == 1) CrudColors.StatusActiveText else CrudColors.StatusInactiveText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = CrudColors.BorderColor)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProductDetailBox("Tồn thực tế", "${product.stockQuantity.toInt()}")
                            ProductDetailBox("Giữ chỗ", "${product.reservedQuantity.toInt()}")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProductDetailBox("Khả dụng", "${product.availableQuantity.toInt()}")
                            ProductDetailBox("Giá bán", "${product.salePrice.toInt()} đ")
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onToggleActiveClick(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CrudStyles.ButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrudColors.DangerAction)
                ) {
                    Text(text = if (product.isActive == 1) "Ngừng kinh doanh sản phẩm" else "Tiếp tục kinh doanh sản phẩm")
                }

                Text(
                    text = "Sản phẩm và lịch sử giao dịch không bị xóa.",
                    color = CrudColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm này không?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick(product)
                }) {
                    Text("Xóa", color = CrudColors.DangerAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ProductDetailBox(label: String, value: String) {
    Surface(
        color = CrudColors.SurfaceBackground,
        shape = CrudStyles.ItemCardShape,
        modifier = Modifier
            .width(160.dp)
            .height(70.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, color = CrudColors.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrudColors.TextPrimary)
        }
    }
}