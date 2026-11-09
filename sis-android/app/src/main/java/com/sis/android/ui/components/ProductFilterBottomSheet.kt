package com.sis.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.WarehouseOption
import com.sis.android.data.model.CategoryOption
import com.sis.android.data.model.SupplierOption
import com.sis.android.data.model.ProductSortOption
import com.sis.android.data.model.SortDirectionOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterBottomSheet(
    warehouses: List<WarehouseOption> = emptyList(),
    categories: List<CategoryOption> = emptyList(),
    suppliers: List<SupplierOption> = emptyList(),
    sortByOptions: List<ProductSortOption> = emptyList(),
    sortDirectionOptions: List<SortDirectionOption> = emptyList(),
    initialWarehouseId: Long? = null,
    initialCategoryId: Long? = null,
    initialSupplierId: Long? = null,
    initialIsActive: Boolean? = null,
    initialSortBy: String = "created_at",
    initialSortDirection: String = "DESC",
    onApplyFilter: (
        warehouseId: Long?,
        categoryId: Long?,
        supplierId: Long?,
        isActive: Boolean?,
        sortBy: String,
        sortDirection: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedWarehouseId by remember { mutableStateOf(initialWarehouseId) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var selectedSupplierId by remember { mutableStateOf(initialSupplierId) }
    var selectedIsActive by remember { mutableStateOf(initialIsActive) }
    var selectedSortBy by remember { mutableStateOf(initialSortBy) }
    var selectedSortDirection by remember { mutableStateOf(initialSortDirection) }

    // Dropdown expansion states
    var warehouseExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var sortExp by remember { mutableStateOf(false) }
    var directionExp by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bộ lọc sản phẩm",
                fontSize = 18.sp,
                color = Color.Black
            )

            // 1. Dropdown Kho hàng (ĐẶT Ở ĐẦU)
            ExposedDropdownMenuBox(
                expanded = warehouseExpanded,
                onExpandedChange = { warehouseExpanded = !warehouseExpanded }
            ) {
                val selectedWhLabel = warehouses.find { wh ->
                    wh.warehouseId == selectedWarehouseId
                }?.warehouseName ?: "Tất cả kho"

                OutlinedTextField(
                    value = selectedWhLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kho hàng") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = warehouseExpanded,
                    onDismissRequest = { warehouseExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tất cả kho") },
                        onClick = {
                            selectedWarehouseId = null
                            warehouseExpanded = false
                        }
                    )
                    warehouses.forEach { wh ->
                        DropdownMenuItem(
                            text = { Text(wh.warehouseName) },
                            onClick = {
                                selectedWarehouseId = wh.warehouseId
                                warehouseExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. Dropdown Danh mục
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                val selectedCatLabel = categories.find { cat ->
                    cat.categoryId == selectedCategoryId
                }?.categoryName ?: "Tất cả danh mục"

                OutlinedTextField(
                    value = selectedCatLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục sản phẩm") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tất cả danh mục") },
                        onClick = {
                            selectedCategoryId = null
                            categoryExpanded = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.categoryName) },
                            onClick = {
                                selectedCategoryId = cat.categoryId
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // 3. Dropdown Nhà cung cấp
            ExposedDropdownMenuBox(
                expanded = supplierExpanded,
                onExpandedChange = { supplierExpanded = !supplierExpanded }
            ) {
                val selectedSupLabel = suppliers.find { sup ->
                    sup.supplierId == selectedSupplierId
                }?.supplierName ?: "Tất cả nhà cung cấp"

                OutlinedTextField(
                    value = selectedSupLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nhà cung cấp") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = supplierExpanded,
                    onDismissRequest = { supplierExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tất cả nhà cung cấp") },
                        onClick = {
                            selectedSupplierId = null
                            supplierExpanded = false
                        }
                    )
                    suppliers.forEach { sup ->
                        DropdownMenuItem(
                            text = { Text(sup.supplierName) },
                            onClick = {
                                selectedSupplierId = sup.supplierId
                                supplierExpanded = false
                            }
                        )
                    }
                }
            }

            // 4. Dropdown Trạng thái hoạt động
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                val statusLabel = when (selectedIsActive) {
                    true -> "Đang kinh doanh"
                    false -> "Ngừng kinh doanh"
                    null -> "Tất cả trạng thái"
                }
                OutlinedTextField(
                    value = statusLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trạng thái") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("Tất cả trạng thái") }, onClick = { selectedIsActive = null; statusExpanded = false })
                    DropdownMenuItem(text = { Text("Đang kinh doanh") }, onClick = { selectedIsActive = true; statusExpanded = false })
                    DropdownMenuItem(text = { Text("Ngừng kinh doanh") }, onClick = { selectedIsActive = false; statusExpanded = false })
                }
            }

            // 5. Dropdown Sắp xếp theo
            ExposedDropdownMenuBox(
                expanded = sortExp,
                onExpandedChange = { sortExp = !sortExp }
            ) {
                val sortLabel = sortByOptions.find { it.value == selectedSortBy }?.label ?: "Ngày tạo"
                OutlinedTextField(
                    value = sortLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sắp xếp theo") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = sortExp,
                    onDismissRequest = { sortExp = false }
                ) {
                    sortByOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.label) },
                            onClick = {
                                selectedSortBy = opt.value
                                sortExp = false
                            }
                        )
                    }
                }
            }

            // 6. Dropdown Chiều sắp xếp
            ExposedDropdownMenuBox(
                expanded = directionExp,
                onExpandedChange = { directionExp = !directionExp }
            ) {
                val dirLabel = sortDirectionOptions.find { it.value == selectedSortDirection }?.label ?: "Giảm dần"
                OutlinedTextField(
                    value = dirLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Thứ tự") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = directionExp,
                    onDismissRequest = { directionExp = false }
                ) {
                    sortDirectionOptions.forEach { dir ->
                        DropdownMenuItem(
                            text = { Text(dir.label) },
                            onClick = {
                                selectedSortDirection = dir.value
                                directionExp = false
                            }
                        )
                    }
                }
            }

            // Nút áp dụng bộ lọc
            Button(
                onClick = {
                    onApplyFilter(
                        selectedWarehouseId,
                        selectedCategoryId,
                        selectedSupplierId,
                        selectedIsActive,
                        selectedSortBy,
                        selectedSortDirection
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "Áp dụng bộ lọc", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
