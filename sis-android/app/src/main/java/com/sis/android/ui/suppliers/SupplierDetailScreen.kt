package com.sis.android.ui.suppliers

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
import com.sis.android.data.model.Supplier
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
fun SupplierDetailScreen(
    supplier: Supplier,
    onBackClick: () -> Unit,
    onEditClick: (Supplier) -> Unit,
    onDeleteClick: (Supplier) -> Unit,
    onToggleActiveClick: (Supplier) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
                onClick = { onEditClick(supplier) },
                containerColor = CrudColors.Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = supplier.supplierName,
                subtitle = "Chi tiết nhà cung cấp",
                onBack = onBackClick,
                actions = {
                    HeaderIconButton(Icons.Default.Delete, "Xóa") {
                        if (supplier.isActive) {
                            snackbarMessage = "Vui lòng ngừng hoạt động nhà cung cấp trước khi xóa!"
                        } else {
                            showDeleteDialog = true
                        }
                    }
                }
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                AppCard(title = "Thông tin chung") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Trạng thái", color = CrudColors.TextPrimary)
                        StatusBadge(
                            text = if (supplier.isActive) "Hoạt động" else "Ngừng hoạt động",
                            ok = supplier.isActive
                        )
                    }
                    HorizontalDivider(color = CrudColors.BorderColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tên nhà cung cấp", color = CrudColors.TextSecondary, fontSize = 13.sp)
                        Text(supplier.supplierName, color = CrudColors.TextPrimary, fontSize = 14.sp)
                    }
                }

                AppCard(title = "Thông tin liên hệ") {
                    TileRow(
                        "Số điện thoại",
                        supplier.phone?.takeIf { it.isNotBlank() } ?: "Chưa có",
                        "Email",
                        supplier.email?.takeIf { it.isNotBlank() } ?: "Chưa có"
                    )
                }

                if (!supplier.address.isNullOrBlank()) {
                    AppCard(title = "Địa chỉ") {
                        Text(supplier.address!!, color = CrudColors.TextSecondary, fontSize = 14.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onToggleActiveClick(supplier) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CrudStyles.ButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (supplier.isActive) CrudColors.DangerAction else CrudColors.Primary
                    )
                ) {
                    Text(if (supplier.isActive) "Ngừng hợp tác" else "Kích hoạt lại")
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Xác nhận xóa",
            message = "Bạn có chắc chắn muốn xóa nhà cung cấp \"${supplier.supplierName}\" không?",
            confirmText = "Xóa",
            danger = true,
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick(supplier)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

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
