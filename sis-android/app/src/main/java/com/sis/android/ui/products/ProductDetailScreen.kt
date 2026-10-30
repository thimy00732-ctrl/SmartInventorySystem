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

@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onToggleActiveClick: (Product) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isActive = product.status == "ACTIVE"

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    HeaderIconButton(Icons.Default.Delete, "Xóa") { showDeleteDialog = true }
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

                    TileRow("Tồn kho", "${product.stockQuantity.toInt()} ${product.unit}",
                        "Tồn tối thiểu", "${product.minStock.toInt()} ${product.unit}")
                    TileRow("Mã vạch", product.barcode ?: "Chưa có",
                        "Danh mục", product.categoryName ?: "Chưa phân loại")
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