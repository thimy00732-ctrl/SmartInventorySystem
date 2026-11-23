package com.sis.android.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sis.android.ui.theme.color.CrudColors

@Composable
internal fun InventoryProductThumbnail(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier.clip(shape).background(CrudColors.LightGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = contentDescription,
                tint = CrudColors.TextSecondary
            )
        }
    } else {
        val fallbackPainter = rememberVectorPainter(image = Icons.Default.Inventory2)
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(shape).background(CrudColors.LightGreenBg),
            contentScale = ContentScale.Crop,
            placeholder = fallbackPainter,
            error = fallbackPainter
        )
    }
}
