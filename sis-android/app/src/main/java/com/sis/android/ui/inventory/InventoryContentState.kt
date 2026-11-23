package com.sis.android.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.ui.theme.color.CrudColors

/*
 * Trình bày trạng thái tải, lỗi, rỗng hoặc dữ liệu thành công dùng chung
 * cho các bảng trong module tồn kho.
 */
@Composable
internal fun InventoryContentState(
    isLoading: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
    emptyMessage: String,
    onRetry: () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(color = CrudColors.Primary)
                Text("Đang tải dữ liệu...", color = CrudColors.TextSecondary)
            }
        }

        errorMessage != null -> Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(errorMessage, color = CrudColors.DangerAction)
                Button(onClick = onRetry) {
                    Text("Thử lại")
                }
            }
        }

        isEmpty -> Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                color = CrudColors.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }

        else -> content()
    }
}
