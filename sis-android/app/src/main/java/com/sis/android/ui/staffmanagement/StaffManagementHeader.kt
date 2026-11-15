
package com.sis.android.ui.staffmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object StaffManagementColors {
    val Background = Color(0xFFF4F6FA)
    val OliveDark = Color(0xFF30491B)
    val OliveLight = Color(0xFF526B2B)
    val Button = Color(0xFF365314)
    val Border = Color(0xFFDDE5D5)
    val MainText = Color(0xFF172033)
    val SecondaryText = Color(0xFF718096)
}

@Composable
fun StaffManagementHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Thanh điều hướng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(StaffManagementColors.Background)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable(enabled = onBack != null) {
                        onBack?.invoke()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹",
                    color = StaffManagementColors.Button,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(5.dp))

//                Text(
//                    text = "Tài khoản",
//                    color = StaffManagementColors.Button,
//                    fontSize = 14.sp
//                )
            }
        }

        // Header xanh olive
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            StaffManagementColors.OliveDark,
                            StaffManagementColors.OliveLight
                        )
                    )
                )
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
//                Text(
//                    text = "THỨ TƯ, 25/06/2025",
//                    color = Color(0xFFD0D4C7),
//                    fontSize = 13.sp
//                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    color = Color(0xFFD0D4C7),
                    fontSize = 14.sp
                )
            }

//            Box(
//                modifier = Modifier
//                    .size(54.dp)
//                    .background(
//                        Color.White.copy(alpha = 0.12f),
//                        RoundedCornerShape(17.dp)
//                    ),
//                contentAlignment = Alignment.Center
//            )
       //     {
//                Icon(
//                    imageVector = Icons.Default.NotificationsNone,
//                    contentDescription = "Thông báo",
//                    tint = Color.White,
//                    modifier = Modifier.size(28.dp)
//                )
         }
        }
    }

