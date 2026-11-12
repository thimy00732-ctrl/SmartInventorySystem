package com.sis.android.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Category
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.form.AppTextField
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

@Composable
fun CategoryFormScreen(
    categoryToEdit: Category? = null,
    isSaving: Boolean = false,
    serverError: String? = null,
    onSave: (Category) -> Unit,
    onBackClick: () -> Unit
) {
    val isNew = categoryToEdit == null

    var categoryName by remember { mutableStateOf(categoryToEdit?.categoryName ?: "") }
    var description by remember { mutableStateOf(categoryToEdit?.description ?: "") }
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = if (isNew) "Thêm danh mục" else "Cập nhật danh mục",
                subtitle = if (isNew) "Nhập thông tin danh mục mới" else "Chỉnh sửa thông tin danh mục",
                onBack = onBackClick
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                (localError ?: serverError)?.let { msg ->
                    Surface(
                        color = CrudColors.StatusInactiveBg,
                        shape = CrudStyles.ItemCardShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            msg,
                            color = CrudColors.DangerAction,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                AppCard(title = "Thông tin danh mục") {
                    AppTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = "Tên danh mục *"
                    )
                    AppTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Mô tả",
                        singleLine = false,
                        minLines = 3
                    )
                }

                Button(
                    enabled = !isSaving,
                    onClick = {
                        when {
                            categoryName.isBlank() ->
                                localError = "Vui lòng nhập tên danh mục!"
                            else -> {
                                localError = null
                                onSave(
                                    (categoryToEdit ?: Category(categoryName = "")).copy(
                                        categoryName = categoryName.trim(),
                                        description = description.trim().ifBlank { null },
                                        isActive = categoryToEdit?.isActive ?: true
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CrudStyles.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrudColors.Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        when {
                            isSaving -> "Đang lưu..."
                            isNew -> "Lưu danh mục mới"
                            else -> "Lưu thay đổi"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
