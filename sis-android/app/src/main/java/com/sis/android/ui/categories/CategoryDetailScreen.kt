package com.sis.android.ui.categories

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
import com.sis.android.data.model.Category
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.display.StatusBadge
import com.sis.android.ui.components.feedback.ConfirmDialog
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.components.layout.HeaderIconButton
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import kotlinx.coroutines.launch

@Composable
fun CategoryDetailScreen(
    category: Category,
    onBackClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onToggleActiveClick: (Category) -> Unit
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
                onClick = { onEditClick(category) },
                containerColor = CrudColors.Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = category.categoryName,
                subtitle = "Chi tiết danh mục",
                onBack = onBackClick,
                actions = {
                    HeaderIconButton(Icons.Default.Delete, "Xóa") {
                        if (category.isActive) {
                            snackbarMessage = "Vui lòng ngừng hoạt động danh mục trước khi xóa!"
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
                            text = if (category.isActive) "Hoạt động" else "Ngừng hoạt động",
                            ok = category.isActive
                        )
                    }
                    HorizontalDivider(color = CrudColors.BorderColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tên danh mục", color = CrudColors.TextSecondary, fontSize = 13.sp)
                        Text(category.categoryName, color = CrudColors.TextPrimary, fontSize = 14.sp)
                    }
                }

                if (!category.description.isNullOrBlank()) {
                    AppCard(title = "Mô tả") {
                        Text(category.description!!, color = CrudColors.TextSecondary, fontSize = 14.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onToggleActiveClick(category) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CrudStyles.ButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (category.isActive) CrudColors.DangerAction else CrudColors.Primary
                    )
                ) {
                    Text(if (category.isActive) "Ngừng hoạt động danh mục" else "Kích hoạt lại danh mục")
                }
            }

        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Xác nhận xóa",
            message = "Bạn có chắc chắn muốn xóa danh mục \"${category.categoryName}\" không?",
            confirmText = "Xóa",
            danger = true,
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick(category)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
