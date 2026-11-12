package com.sis.android.ui.suppliers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Supplier
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.form.AppTextField
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

@Composable
fun SupplierFormScreen(
    supplierToEdit: Supplier? = null,
    isSaving: Boolean = false,
    serverError: String? = null,
    onSave: (Supplier) -> Unit,
    onBackClick: () -> Unit
) {
    val isNew = supplierToEdit == null

    var supplierName by remember { mutableStateOf(supplierToEdit?.supplierName ?: "") }
    var phone by remember { mutableStateOf(supplierToEdit?.phone ?: "") }
    var email by remember { mutableStateOf(supplierToEdit?.email ?: "") }
    var address by remember { mutableStateOf(supplierToEdit?.address ?: "") }
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = if (isNew) "Thêm nhà cung cấp" else "Cập nhật nhà cung cấp",
                subtitle = if (isNew) "Nhập thông tin nhà cung cấp mới" else "Chỉnh sửa thông tin nhà cung cấp",
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

                AppCard(title = "Thông tin chính") {
                    AppTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = "Tên nhà cung cấp *"
                    )
                }

                AppCard(title = "Thông tin liên hệ") {
                    AppTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Số điện thoại",
                        keyboardType = KeyboardType.Phone
                    )
                    AppTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        keyboardType = KeyboardType.Email
                    )
                    AppTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Địa chỉ",
                        singleLine = false,
                        minLines = 2
                    )
                }

                Button(
                    enabled = !isSaving,
                    onClick = {
                        when {
                            supplierName.isBlank() ->
                                localError = "Vui lòng nhập tên nhà cung cấp!"
                            else -> {
                                localError = null
                                onSave(
                                    (supplierToEdit ?: Supplier(supplierName = "")).copy(
                                        supplierName = supplierName.trim(),
                                        phone = phone.trim().ifBlank { null },
                                        email = email.trim().ifBlank { null },
                                        address = address.trim().ifBlank { null },
                                        isActive = supplierToEdit?.isActive ?: true
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
                            isNew -> "Lưu nhà cung cấp mới"
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
