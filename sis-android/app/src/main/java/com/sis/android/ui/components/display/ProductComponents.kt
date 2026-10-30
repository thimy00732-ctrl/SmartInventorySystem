package com.sis.android.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.Product
import com.sis.android.ui.theme.color.CrudColors

/** Chip lọc trạng thái kho */
@Composable
fun StockFilterChip(selected: Boolean, text: String, onClick: () -> Unit) {
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

/** Thẻ thống kê nhỏ trên đầu danh sách */
@Composable
fun ProductStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** Dòng hiển thị thông tin 1 sản phẩm trong danh sách */
@Composable
fun ProductRowItem(product: Product, onClick: () -> Unit) {
    val low = product.availableQuantity <= product.minStock
    val initials = product.productName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrudColors.AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = CrudColors.AvatarText, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.productName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CrudColors.TextPrimary
            )
            Text(
                "${product.sku} · ${product.warehouseName ?: "Kho chính"}",
                fontSize = 11.sp,
                color = CrudColors.TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${product.availableQuantity.toInt()} ${product.unit} khả dụng",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = CrudColors.TextPrimary
            )
        }

        Surface(
            color = if (low) CrudColors.WarnBg else CrudColors.OkBg,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                if (low) "● Tồn thấp" else "● Ổn định",
                color = if (low) CrudColors.WarnText else CrudColors.OkText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}