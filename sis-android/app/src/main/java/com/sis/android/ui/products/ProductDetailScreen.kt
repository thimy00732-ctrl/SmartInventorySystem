package com.sis.android.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Product
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.display.InfoTile
import com.sis.android.ui.components.display.StatusBadge
import com.sis.android.ui.components.feedback.ConfirmDialog
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.components.layout.HeaderIconButton
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onToggleActiveClick: (Product) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isActive = product.status == "ACTIVE"

    // Hiển thị Snackbar khi có thông báo lỗi/nhắc nhở
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
                snackbarMessage = null
            }
        }
    }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditClick(product) },
                containerColor = CrudColors.Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppHeader(
                title = product.productName,
                subtitle = product.sku,
                onBack = onBackClick,
                actions = {
                    HeaderIconButton(Icons.Default.Delete, "Xóa") {
                        // 🔒 Nghiệp vụ: Chỉ cho phép xóa khi đã NGỪNG KINH DOANH
                        if (isActive) {
                            snackbarMessage = "Vui lòng 'Ngừng kinh doanh' sản phẩm trước khi xóa!"
                        } else {
                            showDeleteDialog = true
                        }
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                AppCard(title = "Thông tin chung") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Trạng thái kinh doanh", color = CrudColors.TextPrimary)
                        StatusBadge(
                            text = if (isActive) "Đang kinh doanh" else "Ngừng kinh doanh",
                            ok = isActive
                        )
                    }
                    HorizontalDivider(color = CrudColors.BorderColor)

                    TileRow(
                        "Mã SKU",
                        product.sku.takeIf { it.isNotBlank() } ?: "Chưa có",
                        "Mã vạch",
                        product.barcode?.takeIf { it.isNotBlank() } ?: "Chưa có"
                    )
                    TileRow(
                        "Danh mục",
                        product.categoryName.takeIf { it.isNotBlank() } ?: "Chưa phân loại",
                        "Đơn vị tính",
                        product.unit
                    )
                    TileRow(
                        "Tồn kho",
                        "${product.stockQuantity} ${product.unit}",
                        "Tồn tối thiểu",
                        "${product.minStock} ${product.unit}"
                    )
                    TileRow(
                        "Tồn tối đa",
                        "${product.maxStock} ${product.unit}",
                        "Khả dụng",
                        "${product.availableQuantity} ${product.unit}"
                    )
                }

                AppCard(title = "Giá cả") {
                    TileRow(
                        "Giá vốn",
                        "${String.format("%,.0f", product.costPrice)} đ",
                        "Giá bán",
                        "${String.format("%,.0f", product.salePrice)} đ"
                    )

                    // 🛠️ Tính toán lợi nhuận chính xác
                    val profit = product.salePrice - product.costPrice
                    val profitPercent = if (product.costPrice > 0) (profit / product.costPrice * 100) else 0.0

                    TileRow(
                        "Lợi nhuận/sp",
                        "${String.format("%,.0f", profit)} đ",
                        "Tỷ suất LN",
                        "${String.format("%.1f", profitPercent)}%"
                    )
                }

                if (!product.description.isNullOrBlank()) {
                    AppCard(title = "Mô tả") {
                        Text(product.description!!, color = CrudColors.TextSecondary, fontSize = 14.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onToggleActiveClick(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CrudStyles.ButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isActive) CrudColors.DangerAction else CrudColors.Primary
                    )
                ) {
                    Text(if (isActive) "Ngừng kinh doanh sản phẩm" else "Tiếp tục kinh doanh sản phẩm")
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Xác nhận xóa",
            message = "Bạn có chắc chắn muốn xóa sản phẩm \"${product.productName}\" không?",
            confirmText = "Xóa",
            danger = true,
            onConfirm = {
                showDeleteDialog = false
                // Gọi callback onDeleteClick truyền từ NavHost/ViewModel xuống
                onDeleteClick(product)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/** Một hàng gồm 2 ô thông tin chia đều. Chỉ dùng trong màn này. */
@Composable
private fun TileRow(label1: String, value1: String, label2: String, value2: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
    ) {
        InfoTile(label1, value1, Modifier.weight(1f))
        InfoTile(label2, value2, Modifier.weight(1f))
    }
}