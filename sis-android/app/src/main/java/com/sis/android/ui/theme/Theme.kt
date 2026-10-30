package com.sis.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sis.android.ui.theme.color.CrudColors

private val LightColorScheme = lightColorScheme(
    primary = CrudColors.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EDD5),
    onPrimaryContainer = CrudColors.Primary,
    background = CrudColors.LightGreenBg,
    surface = Color.White,
    error = CrudColors.DangerAction
)

@Composable
fun SISTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}