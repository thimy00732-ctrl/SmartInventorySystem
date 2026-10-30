package com.sis.android.ui.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Product
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import androidx.compose.foundation.horizontalScroll
@Composable
fun ProductFormScreen(
    productToEdit: Product? = null, // null = Thêm mới, có dữ liệu = Sửa
    onSave: (Product) -> Unit,
    onBackClick: () -> Unit
) {
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var productName by remember { mutableStateOf(productToEdit?.productName ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "piece") }
    var minStock by remember { mutableStateOf(productToEdit?.minStock?.toString() ?: "0.0") }
    var status by remember { mutableStateOf(productToEdit?.status ?: "ACTIVE") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isNew = productToEdit == null

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ===== Header gradient =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CrudColors.HeaderBrush,
                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isNew) "Thêm sản phẩm" else "Cập nhật sản phẩm",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isNew) "Nhập thông tin sản phẩm mới" else "Chỉnh sửa thông tin sản phẩm",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ===== Nội dung cuộn =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thông báo lỗi
                errorMessage?.let { msg ->
                    Surface(
                        color = CrudColors.StatusInactiveBg,
                        shape = CrudStyles.ItemCardShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = CrudColors.DangerAction,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ----- Thẻ 1: Thông tin chính -----
                FormSection(title = "Thông tin chính") {
                    FormField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = "Mã SKU *"
                    )
                    FormField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Tên sản phẩm *"
                    )
                    FormField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = "Mã vạch (Barcode)"
                    )
                }

                // ----- Thẻ 2: Kho -----
                FormSection(title = "Đơn vị & tồn kho") {
                    FormField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = "Đơn vị tính *"
                    )
                    // Gợi ý nhanh đơn vị
                    Row(
                        modifier = Modifier.horizontalScrollSafe(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("chai", "thùng", "hộp", "cái", "kg").forEach { suggestion ->
                            FormChip(
                                selected = unit.equals(suggestion, ignoreCase = true),
                                text = suggestion,
                                onClick = { unit = suggestion }
                            )
                        }
                    }
                    FormField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = "Định mức tồn tối thiểu *",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                // ----- Thẻ 3: Trạng thái -----
                FormSection(title = "Trạng thái kinh doanh") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormChip(
                            selected = status == "ACTIVE",
                            text = "Đang kinh doanh",
                            onClick = { status = "ACTIVE" }
                        )
                        FormChip(
                            selected = status == "INACTIVE",
                            text = "Ngừng kinh doanh",
                            onClick = { status = "INACTIVE" }
                        )
                    }
                }

                // ----- Thẻ 4: Mô tả -----
                FormSection(title = "Mô tả") {
                    FormField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Mô tả sản phẩm chi tiết",
                        singleLine = false,
                        minLines = 3
                    )
                }

                // ----- Nút lưu -----
                Button(
                    onClick = {
                        if (sku.isBlank() || productName.isBlank() || unit.isBlank() || minStock.isBlank()) {
                            errorMessage = "Vui lòng điền đầy đủ các trường có dấu (*)"
                            return@Button
                        }

                        val stockDouble = minStock.toDoubleOrNull()
                        if (stockDouble == null || stockDouble < 0.0) {
                            errorMessage = "Định mức tồn tối thiểu phải là số hợp lệ >= 0"
                            return@Button
                        }

                        errorMessage = null

                        val product = Product(
                            productId = productToEdit?.productId ?: System.currentTimeMillis(),
                            sku = sku.trim(),
                            barcode = barcode.ifBlank { null },
                            productName = productName.trim(),
                            description = description.ifBlank { null },
                            unit = unit.trim(),
                            minStock = stockDouble,
                            categoryId = productToEdit?.categoryId,
                            status = status.trim(),
                            stockQuantity = productToEdit?.stockQuantity ?: 0.0,
                            categoryName = productToEdit?.categoryName ?: "Chưa phân loại"
                        )

                        onSave(product)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CrudStyles.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrudColors.Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (isNew) "Lưu sản phẩm mới" else "Lưu thay đổi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Thẻ trắng bao quanh một nhóm ô nhập */
@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, CrudColors.BorderColor),
        shape = CrudStyles.CardShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CrudColors.TextPrimary
            )
            content()
        }
    }
}

/** Ô nhập đã đồng bộ màu với theme */
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = CrudStyles.InputShape,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = CrudColors.SurfaceBackground,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = CrudColors.BorderColor,
            focusedBorderColor = CrudColors.Primary,
            focusedLabelColor = CrudColors.Primary,
            cursorColor = CrudColors.Primary,
            unfocusedLabelColor = CrudColors.TextSecondary
        )
    )
}

/** Chip chọn nhanh, cùng kiểu với màn danh sách */
@Composable
private fun FormChip(selected: Boolean, text: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = CrudColors.TextPrimary,
            selectedContainerColor = CrudColors.Primary,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = CrudColors.BorderColor,
            selectedBorderColor = Color.Transparent
        )
    )
}

/** Cho hàng chip cuộn ngang nếu màn hình hẹp */
@Composable
private fun Modifier.horizontalScrollSafe(): Modifier =
    this.then(Modifier.horizontalScroll(rememberScrollState()))