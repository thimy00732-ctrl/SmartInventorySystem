package com.sis.android.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.ui.theme.Dimens
import com.sis.android.ui.theme.color.CrudColors

/** Header gradient dùng chung cho mọi màn hình. */
@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                CrudColors.HeaderBrush,
                RoundedCornerShape(bottomStart = Dimens.HeaderCorner, bottomEnd = Dimens.HeaderCorner)
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                HeaderIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", onBack)
                Spacer(Modifier.width(12.dp))
            } else {
                Spacer(Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        }
    }
}

/** Nút tròn mờ đặt trong header. */
@Composable
fun HeaderIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
    ) {
        Icon(icon, contentDescription = description, tint = Color.White)
    }
}