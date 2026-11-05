package com.sis.android.ui.products

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
import com.sis.android.data.model.Category
import com.sis.android.data.model.Product
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.form.*
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

private val UNIT_SUGGESTIONS = listOf("chai", "thùng", "hộp", "cái", "kg")

@Composable
fun ProductFormScreen(
    productToEdit: Product? = null,          // null = thêm mới
    categories: List<Category> = emptyList(),
    isSaving: Boolean = false,
    serverError: String? = null,
    onSave: (Product) -> Unit,
    onBackClick: () -> Unit
) {
    val isNew = productToEdit == null

    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var productName by remember { mutableStateOf(productToEdit?.productName ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "") }
    var minStock by remember { mutableStateOf(productToEdit?.minStock?.toString() ?: "0") }
    var maxStock by remember { mutableStateOf(productToEdit?.maxStock?.toString() ?: "1000") }

    var costPrice by remember { mutableStateOf(productToEdit?.costPrice?.toString() ?: "") }
    var salePrice by remember { mutableStateOf(productToEdit?.salePrice?.toString() ?: "") }

    var status by remember { mutableStateOf(productToEdit?.status ?: "ACTIVE") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var categoryId by remember { mutableStateOf(productToEdit?.categoryId) }
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = if (isNew) "Thêm sản phẩm" else "Cập nhật sản phẩm",
                subtitle = if (isNew) "Nhập thông tin sản phẩm mới" else "Chỉnh sửa thông tin sản phẩm",
                onBack = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                    AppTextField(value = sku, onValueChange = { sku = it }, label = "Mã SKU (Tự sinh nếu trống)")
                    AppTextField(productName, { productName = it }, "Tên sản phẩm *")
                    AppTextField(barcode, { barcode = it }, "Mã vạch (Barcode)")
                    AppDropdown(
                        label = "Danh mục",
                        options = categories,
                        selected = categories.firstOrNull { it.categoryId == categoryId },
                        optionLabel = { it.categoryName },
                        onSelected = { categoryId = it.categoryId }
                    )
                }

                AppCard(title = "Đơn vị & định mức tồn kho") {
                    AppTextField(unit, { unit = it }, "Đơn vị tính *")
                    ChipRow {
                        UNIT_SUGGESTIONS.forEach { s ->
                            AppChip(unit.equals(s, ignoreCase = true), s) { unit = s }
                        }
                    }
                    AppTextField(
                        minStock, { minStock = it }, "Định mức tồn tối thiểu *",
                        keyboardType = KeyboardType.Number
                    )
                    AppTextField(
                        maxStock, { maxStock = it }, "Định mức tồn tối đa *",
                        keyboardType = KeyboardType.Number
                    )
                }

                AppCard(title = "Giá cả") {
                    AppTextField(
                        costPrice, { costPrice = it }, "Giá vốn (đ) *",
                        keyboardType = KeyboardType.Decimal
                    )
                    AppTextField(
                        salePrice, { salePrice = it }, "Giá bán (đ) *",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                AppCard(title = "Trạng thái kinh doanh") {
                    ChipRow {
                        AppChip(status == "ACTIVE", "Đang kinh doanh") { status = "ACTIVE" }
                        AppChip(status == "INACTIVE", "Ngừng kinh doanh") { status = "INACTIVE" }
                    }
                }

                AppCard(title = "Mô tả") {
                    AppTextField(
                        description, { description = it }, "Mô tả sản phẩm chi tiết",
                        singleLine = false, minLines = 3
                    )
                }

                Button(
                    enabled = !isSaving,
                    onClick = {
                        val stockMin = minStock.toIntOrNull()
                        val stockMax = maxStock.toIntOrNull()
                        val priceCost = costPrice.toDoubleOrNull()
                        val priceSale = salePrice.toDoubleOrNull()

                        when {
                            productName.isBlank() || unit.isBlank() ->
                                localError = "Vui lòng điền Tên sản phẩm và Đơn vị tính!"
                            stockMin == null || stockMin < 0 ->
                                localError = "Định mức tồn tối thiểu phải là số nguyên hợp lệ >= 0"
                            stockMax == null || stockMax < 0 ->
                                localError = "Định mức tồn tối đa phải là số nguyên hợp lệ >= 0"
                            priceCost == null || priceCost < 0 ->
                                localError = "Giá vốn phải là số hợp lệ >= 0"
                            priceSale == null || priceSale < 0 ->
                                localError = "Giá bán phải là số hợp lệ >= 0"
                            else -> {
                                localError = null
                                onSave(
                                    (productToEdit ?: Product()).copy(
                                        productId = productToEdit?.productId ?: 0L,
                                        sku = sku.trim().takeIf { it.isNotBlank() } ?: "",
                                        barcode = barcode.trim().takeIf { it.isNotBlank() },
                                        productName = productName.trim(),
                                        unit = unit.trim(),
                                        minStock = stockMin,
                                        maxStock = stockMax,
                                        costPrice = priceCost,
                                        salePrice = priceSale,
                                        categoryId = categoryId,
                                        isActive = (status == "ACTIVE")
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
                            isNew -> "Lưu sản phẩm mới"
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