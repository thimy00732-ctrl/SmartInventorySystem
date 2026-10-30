package com.sis.android.ui.components.display

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.ui.theme.color.CrudColors
import com.sis.android.ui.theme.style.CrudStyles

/** Ô hiển thị 1 cặp "nhãn - giá trị". Dùng weight để chia đều hàng. */
@Composable
fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = CrudColors.SurfaceBackground,
        shape = CrudStyles.ItemCardShape,
        modifier = modifier.heightIn(min = 70.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = CrudColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CrudColors.TextPrimary
            )
        }
    }
}