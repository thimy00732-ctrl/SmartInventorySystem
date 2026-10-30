package com.sis.android.ui.theme.color

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object CrudColors {
    // Header: xanh rêu ngả olive
    val HeaderStart = Color(0xFF1E4620)
    val HeaderMid = Color(0xFF2F5F10)
    val HeaderEnd = Color(0xFF4A7A2E)

    val Primary = Color(0xFF2D5A00)
    val StatStart = Color(0xFF1B4332)

    val LightGreenBg = Color(0xFFF6F7FB)
    val CardBg = Color.White
    val BorderColor = Color(0xFFE6E8F0)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF8A90A6)
    val SurfaceBackground = Color(0xFFF8F9FA)

    // Avatar
    val AvatarBg = Color(0xFFE0E6F8)
    val AvatarText = Color(0xFF5B6BA8)

    // Badge
    val WarnBg = Color(0xFFFFF3DC)
    val WarnText = Color(0xFFC26A00)
    val OkBg = Color(0xFFE3F6EA)
    val OkText = Color(0xFF1B8A4B)

    val DangerAction = Color(0xFFD32F2F)

    // Gradient chéo cho header
    val HeaderBrush: Brush = Brush.linearGradient(
        colors = listOf(HeaderStart, HeaderMid, HeaderEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Gradient ngang cho thẻ thống kê
    val StatBrush: Brush = Brush.horizontalGradient(listOf(StatStart, Primary))

    // ---- Tên cũ (giữ lại để các file khác không lỗi) ----
    val HeaderBackground: Color = Primary
    val PrimaryGradientBrush: Brush = HeaderBrush
    val StatusActiveText: Color = OkText
    val StatusActiveBg: Color = OkBg
    val StatusInactiveText: Color = WarnText
    val StatusInactiveBg: Color = WarnBg
}