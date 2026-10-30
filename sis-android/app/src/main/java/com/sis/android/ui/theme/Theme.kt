package com.sis.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryGreenContainer,
    background = LightGreenBg,
    surface = CardBackground,
    error = ErrorRed
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