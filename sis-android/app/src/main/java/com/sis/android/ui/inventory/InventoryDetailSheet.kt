package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.InventoryItem
import com.sis.android.data.model.InventoryProductDetails
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

/*
 * Sheet chi tiết tồn kho của sản phẩm, gồm dòng tồn đang chọn,
 * các kho/lô liên quan và form cập nhật giữ chỗ khi người dùng có quyền.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailSheet(
    item: InventoryItem,
    relatedItems: List<InventoryItem>,
    productDetails: InventoryProductDetails,
    canUpdateReserved: Boolean,
    onDismiss: () -> Unit,
    onSaveReserved: (inventoryId: Long, reservedQuantity: Int) -> Unit
) {
    var showReservationForm by remember(item.inventoryId) { mutableStateOf(false) }
    var reservedInput by remember(item.inventoryId) { mutableStateOf(item.reservedQuantity.toString()) }
    var validationError by remember(item.inventoryId) { mutableStateOf<String?>(null) }
    var savedMessage by remember(item.inventoryId) { mutableStateOf<String?>(null) }

    val totalStock = relatedItems.sumOf { it.stockQuantity }
    val totalReserved = relatedItems.sumOf { it.reservedQuantity }
    val totalAvailable = relatedItems.sumOf { it.availableQuantity }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CrudColors.LightGreenBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chi tiết tồn kho",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrudColors.TextPrimary
                    )
                    Text(
                        text = "${item.sku} · ${item.productName}",
                        fontSize = 14.sp,
                        color = CrudColors.TextSecondary
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }

            DetailSection(title = "Thông tin sản phẩm") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .background(CrudColors.LightGreenBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    InventoryProductThumbnail(
                        imageUrl = item.imageUrl,
                        contentDescription = item.productName,
                        modifier = Modifier.heightIn(min = 72.dp)
                    )
                }
                DetailValue("SKU", item.sku)
                DetailValue("Mã vạch", productDetails.barcode ?: "Chưa có")
                DetailValue("Tên sản phẩm", item.productName)
                DetailValue("Danh mục", productDetails.categoryName ?: "Chưa phân loại")
                DetailValue("Nhà cung cấp", productDetails.supplierName ?: "Chưa có")
                DetailValue("Đơn vị", item.unit)
                DetailValue(
                    "Trạng thái",
                    if (productDetails.isActive) "Đang kinh doanh" else "Ngừng kinh doanh"
                )
                DetailValue("Ngưỡng tồn tối thiểu", InventoryFormatters.formatNumber(productDetails.minStock))
                DetailValue("Ngưỡng tồn tối đa", InventoryFormatters.formatNumber(productDetails.maxStock))
            }

            DetailSection(title = "Dòng tồn đang xem") {
                DetailValue("Kho", item.warehouseName)
                DetailValue("Mã lô", InventoryFormatters.formatBatch(item.batchCode))
                DetailValue("Ngày sản xuất", InventoryFormatters.formatDate(item.manufactureDate))
                DetailValue("Hạn sử dụng", InventoryFormatters.formatDate(item.expiryDate))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuantityCard("Tồn thực tế", item.stockQuantity, Modifier.weight(1f))
                    QuantityCard("Giữ chỗ", item.reservedQuantity, Modifier.weight(1f))
                    QuantityCard("Khả dụng", item.availableQuantity, Modifier.weight(1f))
                }
                Text(
                    text = "Khả dụng = Tồn thực tế − Giữ chỗ",
                    fontSize = 12.sp,
                    color = CrudColors.TextSecondary
                )
                DetailValue("Cập nhật lần cuối", InventoryFormatters.formatDateTime(item.updatedAt))

                if (canUpdateReserved) {
                    if (!showReservationForm) {
                        OutlinedButton(
                            onClick = {
                                reservedInput = item.reservedQuantity.toString()
                                validationError = null
                                savedMessage = null
                                showReservationForm = true
                            },
                            shape = CrudStyles.InputShape
                        ) {
                            Text("Cập nhật giữ chỗ")
                        }
                    } else {
                        Text(
                            text = "Cập nhật số lượng giữ chỗ",
                            fontWeight = FontWeight.SemiBold,
                            color = CrudColors.TextPrimary
                        )
                        OutlinedTextField(
                            value = reservedInput,
                            onValueChange = {
                                reservedInput = it
                                validationError = validateReservedQuantityInput(it, item.stockQuantity)
                                savedMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Số lượng giữ chỗ mới") },
                            supportingText = {
                                Text("Tối đa: ${InventoryFormatters.formatNumber(item.stockQuantity)} (bằng tồn thực tế)")
                            },
                            singleLine = true,
                            isError = validationError != null,
                            shape = CrudStyles.InputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrudColors.Primary,
                                unfocusedBorderColor = CrudColors.BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        validationError?.let {
                            Text(text = it, color = CrudColors.DangerAction, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val value = reservedInput.toIntOrNull()
                                    val error = validateReservedQuantityInput(reservedInput, item.stockQuantity)
                                    validationError = error
                                    if (value != null && error == null) {
                                        onSaveReserved(item.inventoryId, value)
                                        showReservationForm = false
                                        savedMessage = "Đã cập nhật số lượng giữ chỗ."
                                    }
                                },
                                enabled = validationError == null &&
                                    reservedInput.toIntOrNull() != null,
                                shape = CrudStyles.InputShape,
                                colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
                            ) {
                                Text("Lưu")
                            }
                            OutlinedButton(
                                onClick = {
                                    showReservationForm = false
                                    validationError = null
                                    reservedInput = item.reservedQuantity.toString()
                                },
                                shape = CrudStyles.InputShape
                            ) {
                                Text("Hủy")
                            }
                        }
                    }
                    savedMessage?.let {
                        Text(text = it, color = CrudColors.OkText, fontSize = 13.sp)
                    }
                }
            }

            DetailSection(title = "Các kho/lô khác của sản phẩm này") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.widthIn(min = 720.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CrudColors.LightGreenBg)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("Kho", 1.2f, true)
                            TableCell("Mã lô", 1.1f, true)
                            TableCell("Hạn dùng", 1f, true)
                            TableCell("Tồn thực tế", 1f, true)
                            TableCell("Giữ chỗ", 1f, true)
                            TableCell("Khả dụng", 1f, true)
                        }
                        Divider(color = CrudColors.BorderColor)
                        relatedItems.forEach { related ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(related.warehouseName, 1.2f)
                                TableCell(InventoryFormatters.formatBatch(related.batchCode), 1.1f)
                                TableCell(InventoryFormatters.formatDate(related.expiryDate), 1f)
                                TableCell(InventoryFormatters.formatNumber(related.stockQuantity), 1f)
                                TableCell(InventoryFormatters.formatNumber(related.reservedQuantity), 1f)
                                TableCell(InventoryFormatters.formatNumber(related.availableQuantity), 1f)
                            }
                        }
                        Divider(color = CrudColors.BorderColor)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("Tổng cộng", 3.3f, true)
                            TableCell(InventoryFormatters.formatNumber(totalStock), 1f, true)
                            TableCell(InventoryFormatters.formatNumber(totalReserved), 1f, true)
                            TableCell(InventoryFormatters.formatNumber(totalAvailable), 1f, true)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.heightIn(min = 8.dp))
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrudColors.BorderColor),
        shape = CrudStyles.CardShape
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = CrudColors.TextPrimary)
            content()
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = CrudColors.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            color = CrudColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuantityCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CrudColors.LightGreenBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                InventoryFormatters.formatNumber(value),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CrudColors.Primary
            )
            Text(label, fontSize = 11.sp, color = CrudColors.TextSecondary)
        }
    }
}

@Composable
private fun RowScope.TableCell(value: String, weight: Float, bold: Boolean = false) {
    Text(
        text = value,
        modifier = Modifier.weight(weight).padding(horizontal = 5.dp),
        color = CrudColors.TextPrimary,
        fontSize = 12.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
    )
}

internal fun validateReservedQuantityInput(value: String, stockQuantity: Int): String? {
    val reserved = value.toIntOrNull()
        ?: return "Vui lòng nhập số nguyên không âm."
    return when {
        reserved < 0 -> "Số lượng giữ chỗ không được âm."
        reserved > stockQuantity -> "Số lượng giữ chỗ không được vượt tồn thực tế."
        else -> null
    }
}
