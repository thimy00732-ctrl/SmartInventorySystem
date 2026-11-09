package com.sis.android.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.ui.theme.color.CrudColors

/**
 * Component phân trang với nút Previous/Next và số trang
 * 
 * @param currentPage Trang hiện tại (1-indexed)
 * @param totalPages Tổng số trang
 * @param onPageClick Callback khi nhấn vào số trang
 * @param onPreviousClick Callback khi nhấn Previous
 * @param onNextClick Callback khi nhấn Next
 */
@Composable
fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return  // Không hiển thị nếu chỉ có 1 trang

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Previous
        PaginationButton(
            text = "",
            icon = Icons.Default.ChevronLeft,
            enabled = currentPage > 1,
            onClick = onPreviousClick
        )

        Spacer(Modifier.width(8.dp))

        // Hiển thị số trang (tối đa 5 số)
        val pagesToShow = getPageNumbers(currentPage, totalPages)
        
        pagesToShow.forEach { page ->
            when (page) {
                -1 -> {
                    // Dấu ...
                    Text(
                        "...",
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = CrudColors.TextSecondary
                    )
                }
                else -> {
                    PageNumberButton(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageClick(page) }
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
        }

        Spacer(Modifier.width(4.dp))

        // Nút Next
        PaginationButton(
            text = "",
            icon = Icons.Default.ChevronRight,
            enabled = currentPage < totalPages,
            onClick = onNextClick
        )
    }
}

@Composable
private fun PaginationButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) CrudColors.Primary else Color.Gray.copy(alpha = 0.3f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = text,
                tint = if (enabled) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text,
                color = if (enabled) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PageNumberButton(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) CrudColors.Primary
                else Color.White
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = page.toString(),
            color = if (isSelected) Color.White else CrudColors.TextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

/**
 * Tính toán danh sách số trang cần hiển thị
 * Ví dụ: [1, 2, 3, 4, 5] hoặc [1, -1, 5, 6, 7] (-1 = dấu ...)
 */
private fun getPageNumbers(currentPage: Int, totalPages: Int): List<Int> {
    if (totalPages <= 7) {
        // Nếu ít hơn 7 trang, hiển thị tất cả
        return (1..totalPages).toList()
    }

    // Logic hiển thị: 1 ... 5 6 7 ... 10
    val pages = mutableListOf<Int>()

    when {
        currentPage <= 3 -> {
            // Đầu danh sách: [1, 2, 3, 4, 5, ..., totalPages]
            pages.addAll(1..5)
            pages.add(-1)
            pages.add(totalPages)
        }
        currentPage >= totalPages - 2 -> {
            // Cuối danh sách: [1, ..., totalPages-4, totalPages-3, totalPages-2, totalPages-1, totalPages]
            pages.add(1)
            pages.add(-1)
            pages.addAll((totalPages - 4)..totalPages)
        }
        else -> {
            // Giữa: [1, ..., currentPage-1, currentPage, currentPage+1, ..., totalPages]
            pages.add(1)
            pages.add(-1)
            pages.addAll((currentPage - 1)..(currentPage + 1))
            pages.add(-1)
            pages.add(totalPages)
        }
    }

    return pages
}
