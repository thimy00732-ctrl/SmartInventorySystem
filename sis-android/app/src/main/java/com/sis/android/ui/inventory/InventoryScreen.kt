
package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sis.android.ui.inventory.threshold.UpdateThresholdDialog

// Màu sắc giao diện theo hình mẫu
private val DarkGreen = Color(0xFF304B17)
private val BackgroundColor = Color(0xFFEAF0F5)
private val CardWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF263238)
private val TextGray = Color(0xFF78838B)
private val LightGreen = Color(0xFFDCE8D5)

// Model dùng riêng cho dữ liệu hiển thị trên màn hình
data class InventoryUiItem(
    val inventoryId: Int,
    val productId: Int,
    val sku: String,
    val productName: String,
    val categoryName: String,
    val stockQuantity: Int,
    val minStock: Int,
    val maxStock: Int
)

@Composable
fun InventoryScreen(
    isAdmin: Boolean = true,
    inventoryList: List<InventoryUiItem>,
    onUpdateThreshold: (inventoryId: Int, min: Int, max: Int) -> Unit = { _, _, _ -> }
) {
    // Tạo danh sách có thể cập nhật khi xem thử giao diện
    val itemsState = remember {
        mutableStateListOf<InventoryUiItem>().apply {
            addAll(inventoryList)
        }
    }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedItem by remember { mutableStateOf<InventoryUiItem?>(null) }

    val categories = listOf("Tất cả") +
            itemsState.map { it.categoryName }.distinct()

    val filteredItems = itemsState.filter { item ->
        val matchesSearch =
            item.sku.contains(searchText, ignoreCase = true) ||
                    item.productName.contains(searchText, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "Tất cả" ||
                    item.categoryName == selectedCategory

        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Phần tiêu đề màu xanh đậm
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkGreen,
                    shape = RoundedCornerShape(
                        bottomStart = 22.dp,
                        bottomEnd = 22.dp
                    )
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp,
                    bottom = 22.dp
                )
        ) {


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ngưỡng tồn kho",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = if (isAdmin) {
                    "Quản lý số lượng tồn tối thiểu và tối đa"
                } else {
                    "Theo dõi tình trạng tồn kho sản phẩm"
                },
                color = Color(0xFFE2E8DC),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Thẻ thống kê
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    title = "Sản phẩm",
                    value = itemsState.size.toString(),
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Tồn thấp",
                    value = itemsState.count {
                        it.stockQuantity < it.minStock
                    }.toString(),
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Vượt mức",
                    value = itemsState.count {
                        it.stockQuantity > it.maxStock
                    }.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tìm kiếm
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text("Tìm theo SKU hoặc tên sản phẩm...")
                },
                leadingIcon = {
                    Text("⌕", color = DarkGreen)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Danh mục sản phẩm",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bộ lọc danh mục lấy từ danh sách dữ liệu
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                        },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Danh sách tồn kho",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${filteredItems.size} sản phẩm",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredItems.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CardWhite,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Không tìm thấy sản phẩm phù hợp",
                        modifier = Modifier.padding(20.dp),
                        color = TextGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.inventoryId }
                    ) { item ->
                        InventoryProductCard(
                            item = item,
                            isAdmin = isAdmin,
                            onClick = {
                                selectedItem = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Hộp thoại chỉnh sửa Min/Max
    selectedItem?.let { item ->
        UpdateThresholdDialog(
            currentMin = item.minStock,
            currentMax = item.maxStock,
            onDismiss = {
                selectedItem = null
            },
            onSave = { min, max ->
                val index = itemsState.indexOfFirst {
                    it.inventoryId == item.inventoryId
                }

                if (index >= 0) {
                    itemsState[index] = itemsState[index].copy(
                        minStock = min,
                        maxStock = max
                    )
                }

                onUpdateThreshold(item.inventoryId, min, max)
                selectedItem = null
            }
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                color = TextGray,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun InventoryProductCard(
    item: InventoryUiItem,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    val isLow = item.stockQuantity < item.minStock
    val isHigh = item.stockQuantity > item.maxStock

    val statusText = when {
        isLow -> "Tồn kho thấp"
        isHigh -> "Vượt ngưỡng"
        else -> "Bình thường"
    }

    val statusBackground = when {
        isLow -> Color(0xFFFFE4E2)
        isHigh -> Color(0xFFFFF0D5)
        else -> LightGreen
    }

    val statusTextColor = when {
        isLow -> Color(0xFFB3261E)
        isHigh -> Color(0xFF946200)
        else -> DarkGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isAdmin,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8EEE3)
                ) {
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "箱",
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.productName,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "${item.sku}  •  ${item.categoryName}",
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(
                    color = statusBackground,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(13.dp))
            HorizontalDivider(color = Color(0xFFE8ECEF))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProductValue(
                    label = "Tổng tồn",
                    value = item.stockQuantity.toString()
                )

                ProductValue(
                    label = "Tối thiểu",
                    value = item.minStock.toString()
                )

                ProductValue(
                    label = "Tối đa",
                    value = item.maxStock.toString()
                )
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Chạm để thiết lập ngưỡng  →",
                    color = DarkGreen,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProductValue(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = TextGray,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
