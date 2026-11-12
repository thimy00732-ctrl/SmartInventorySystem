package com.sis.android.ui.suppliers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Supplier
import com.sis.android.ui.components.display.AppCard
import com.sis.android.ui.components.layout.AppHeader
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles
import com.sis.android.ui.viewmodel.SupplierViewModel
import com.sis.android.ui.components.form.ChipRow

@Composable
fun SupplierListScreen(
    viewModel: SupplierViewModel,
    onSupplierClick: (Supplier) -> Unit,
    onAddClick: () -> Unit
) {
    val supplierList by viewModel.supplierList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf<Boolean?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }
    val pageSize = 10

    val filteredSuppliers = remember(supplierList, searchQuery, activeFilter) {
        supplierList
            .filter { supplier ->
                val matchSearch = searchQuery.isBlank() ||
                        supplier.supplierName.contains(searchQuery, ignoreCase = true) ||
                        supplier.phone?.contains(searchQuery, ignoreCase = true) == true ||
                        supplier.email?.contains(searchQuery, ignoreCase = true) == true ||
                        supplier.address?.contains(searchQuery, ignoreCase = true) == true

                val matchActive = when(activeFilter) {
                    null -> true
                    true -> supplier.isActive
                    false -> !supplier.isActive
                }

                matchSearch && matchActive
            }
    }

    // Pagination
    val totalPages = remember(filteredSuppliers) {
        ((filteredSuppliers.size + pageSize - 1) / pageSize).coerceAtLeast(1)
    }

    // Reset về page 1 khi filter thay đổi
    LaunchedEffect(searchQuery, activeFilter) {
        currentPage = 1
    }

    val paginatedSuppliers = remember(filteredSuppliers, currentPage) {
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(filteredSuppliers.size)
        if (startIndex < filteredSuppliers.size) {
            filteredSuppliers.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    Scaffold(
        containerColor = CrudColors.LightGreenBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = CrudColors.Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AppHeader(
                title = "Nhà cung cấp",
                subtitle = "Quản lý đối tác và nguồn hàng"
            )


            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm tên, SĐT, email nhà cung cấp...", fontSize = 14.sp, color = CrudColors.TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrudColors.TextSecondary) },
                    singleLine = true,
                    shape = CrudStyles.InputShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = CrudColors.BorderColor,
                        focusedBorderColor = CrudColors.Primary,
                        cursorColor = CrudColors.Primary
                    )
                )

                // Filter chips
                ChipRow {
                    SupplierFilterChip(activeFilter == null, "Tất cả (${supplierList.size})") { activeFilter = null }
                    SupplierFilterChip(activeFilter == true, "Hoạt động (${supplierList.count { it.isActive }})") { activeFilter = true }
                    SupplierFilterChip(activeFilter == false, "Ngừng (${supplierList.count { !it.isActive }})") { activeFilter = false }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().clip(CrudStyles.CardShape)
                        .background(CrudColors.StatBrush).padding(vertical = 16.dp, horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SupplierStatItem("Tổng số", "${supplierList.size}")
                        VerticalDivider(Modifier.height(36.dp), color = Color.White.copy(alpha = 0.25f))
                        SupplierStatItem("Hoạt động", "${supplierList.count { it.isActive }}")
                        VerticalDivider(Modifier.height(36.dp), color = Color.White.copy(alpha = 0.25f))
                        SupplierStatItem("Ngừng", "${supplierList.count { !it.isActive }}")
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isLoading -> CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center), color = CrudColors.Primary
                            )
                            errorMessage != null -> Column(
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(errorMessage ?: "Lỗi", color = CrudColors.DangerAction)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.loadSuppliers() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrudColors.Primary)
                                ) { Text("Thử lại") }
                            }
                            filteredSuppliers.isEmpty() -> Text(
                                if (searchQuery.isBlank()) "Chưa có nhà cung cấp" else "Không tìm thấy",
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                color = CrudColors.TextSecondary
                            )
                            else -> Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    items(items = paginatedSuppliers, key = { it.supplierId ?: 0L }) { supplier ->
                                        SupplierRowItem(
                                            supplier = supplier,
                                            onClick = { onSupplierClick(supplier) }
                                        )
                                    }
                                }

                                // Pagination controls
                                if (totalPages > 1) {
                                    SupplierPaginationRow(
                                        currentPage = currentPage,
                                        totalPages = totalPages,
                                        onPageChange = { newPage -> currentPage = newPage }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SupplierRowItem(
    supplier: Supplier,
    onClick: () -> Unit
) {
    val initials = supplier.supplierName.split(" ").take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(CrudColors.AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = CrudColors.AvatarText, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(supplier.supplierName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CrudColors.TextPrimary)
            if (!supplier.phone.isNullOrBlank()) {
                Text("📞 ${supplier.phone}", fontSize = 12.sp, color = CrudColors.TextSecondary)
            }
            if (!supplier.email.isNullOrBlank()) {
                Text("✉️ ${supplier.email}", fontSize = 11.sp, color = CrudColors.TextSecondary, maxLines = 1)
            }
        }

        Surface(
            color = if (supplier.isActive) CrudColors.OkBg else CrudColors.WarnBg,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                if (supplier.isActive) "● Hoạt động" else "● Ngừng",
                color = if (supplier.isActive) CrudColors.OkText else CrudColors.WarnText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SupplierFilterChip(selected: Boolean, text: String, onClick: () -> Unit) {
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

@Composable
private fun SupplierPaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        IconButton(
            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Text(
                "<",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentPage > 1) CrudColors.Primary else CrudColors.TextSecondary
            )
        }

        Spacer(Modifier.width(8.dp))

        // Page numbers
        val visiblePages = getVisiblePages(currentPage, totalPages)
        visiblePages.forEach { page ->
            if (page == -1) {
                Text("...", modifier = Modifier.padding(horizontal = 8.dp), color = CrudColors.TextSecondary)
            } else {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onPageChange(page) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (page == currentPage) CrudColors.Primary else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$page",
                            fontSize = 14.sp,
                            fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                            color = if (page == currentPage) Color.White else CrudColors.TextPrimary
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
            }
        }

        Spacer(Modifier.width(8.dp))

        // Next button
        IconButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Text(
                ">",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentPage < totalPages) CrudColors.Primary else CrudColors.TextSecondary
            )
        }
    }
}

private fun getVisiblePages(currentPage: Int, totalPages: Int): List<Int> {
    if (totalPages <= 7) {
        return (1..totalPages).toList()
    }

    return buildList {
        add(1)
        
        when {
            currentPage <= 4 -> {
                addAll(2..5)
                add(-1) // dots
                add(totalPages)
            }
            currentPage >= totalPages - 3 -> {
                add(-1)
                addAll((totalPages - 4) until totalPages)
                add(totalPages)
            }
            else -> {
                add(-1)
                addAll((currentPage - 1)..(currentPage + 1))
                add(-1)
                add(totalPages)
            }
        }
    }
}
