package com.sis.android.ui.theme.color

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object CrudColors {
    // Chuẩn màu rêu đậm sang trọng từ giao diện Web Login
    val HeaderBackground = Color(0xFF0F381E)       // Xanh rêu đậm sâu ở đỉnh
    val HeaderBottomGradient = Color(0xFF265C38)   // Xanh rêu trầm ấm loan xuống dưới

    val LightGreenBg = Color(0xFFF4F7F5)         // Màu nền tổng thể sáng
    val CardBg = Color(0xFFFFFFFF)
    val BorderColor = Color(0xFFE0E0E0)
    val DangerAction = Color(0xFFD32F2F)
    val StatusActiveText = Color(0xFF388E3C)
    val StatusActiveBg = Color(0xFFE8F5E9)
    val StatusInactiveText = Color(0xFFD32F2F)
    val StatusInactiveBg = Color(0xFFFFECEC)
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val SurfaceBackground = Color(0xFFF8F9FA)

    // Hiệu ứng chuyển màu dọc (Vertical Gradient) y hệt mẫu Web
    val PrimaryGradientBrush = Brush.verticalGradient(
        colors = listOf(
            HeaderBackground,      // Đậm ở trên
            HeaderBottomGradient   // Nhạt dần xuống dưới
        )
    )
}