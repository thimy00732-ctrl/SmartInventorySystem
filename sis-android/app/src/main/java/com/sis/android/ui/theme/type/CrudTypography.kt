package com.sis.android.ui.theme.type

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sis.android.ui.theme.TextPrimary

object CrudTypography {
    val HeaderTitleStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TextPrimary
    )
    val CardTitleStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = TextPrimary
    )
}