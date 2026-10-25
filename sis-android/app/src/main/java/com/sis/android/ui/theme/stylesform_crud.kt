package com.sis.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CrudColorScheme = lightColorScheme(
    primary = CrudPrimaryBlue,
    onPrimary = Color.White,
    secondary = CrudSecondaryTeal,
    onSecondary = Color.White,
    background = CrudBackgroundLight,
    surface = CrudSurfaceCard,
    error = CrudErrorRed
)

@Composable
fun SISTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CrudColorScheme,
        typography = CrudTypography,
        content = content
    )
}