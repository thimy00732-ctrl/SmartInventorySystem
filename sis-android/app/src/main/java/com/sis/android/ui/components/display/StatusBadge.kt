package com.sis.android.ui.components.display

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import com.sis.android.ui.theme.color.CrudColors

/** Badge trạng thái: ok = xanh, ngược lại = hổ phách. */
@Composable
fun StatusBadge(text: String, ok: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (ok) CrudColors.OkBg else CrudColors.WarnBg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = "● $text",
            color = if (ok) CrudColors.OkText else CrudColors.WarnText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}