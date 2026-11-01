
package com.sis.android.ui.staffmanagement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.ManagedStaff

@Composable
fun StaffManagementDetailScreen(
    staff: ManagedStaff,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Scaffold(
        containerColor = StaffManagementColors.Background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StaffManagementColors.Background)
        ) {

            StaffManagementHeader(
                title = "Chi tiết nhân viên",
                subtitle = "Thông tin và trạng thái tài khoản",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(top = 22.dp, bottom = 18.dp)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        StaffManagementColors.Border
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        // Avatar và tên nhân viên
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .background(
                                        Color(0xFFEEF2E6),
                                        RoundedCornerShape(17.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = staff.fullName
                                        .split(" ")
                                        .takeLast(2)
                                        .mapNotNull {
                                            it.firstOrNull()?.uppercase()
                                        }
                                        .joinToString(""),
                                    color = StaffManagementColors.Button,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = staff.fullName,
                                    color = StaffManagementColors.MainText,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = staff.email,
                                    color = StaffManagementColors.SecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        HorizontalDivider(
                            color = StaffManagementColors.Border
                        )

                        Spacer(Modifier.height(10.dp))

                        DetailInformationRow(
                            label = "Họ và tên",
                            value = staff.fullName
                        )

                        DetailInformationRow(
                            label = "Email",
                            value = staff.email
                        )

                        DetailInformationRow(
                            label = "Chi nhánh",
                            value = staff.branch
                        )

                        DetailInformationRow(
                            label = "Vai trò",
                            value = when (staff.role) {
                                "ADMIN" -> "Quản trị viên"
                                "MANAGER" -> "Quản lý"
                                else -> "Nhân viên"
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trạng thái",
                                color = StaffManagementColors.SecondaryText,
                                fontSize = 13.sp
                            )

                            Surface(
                                color = if (staff.isActive) {
                                    Color(0xFFEBF7EF)
                                } else {
                                    Color(0xFFF0F2F6)
                                },
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (staff.isActive) {
                                        "● Đang hoạt động"
                                    } else {
                                        "● Đã khóa"
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 11.dp,
                                        vertical = 7.dp
                                    ),
                                    color = if (staff.isActive) {
                                        Color(0xFF238747)
                                    } else {
                                        Color(0xFF687386)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (isAdmin) {
                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StaffManagementColors.Button
                        )
                    ) {
                        Text(
                            text = "Chỉnh sửa thông tin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onToggleActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StaffManagementColors.Button
                        ),
                        border = BorderStroke(
                            1.dp,
                            StaffManagementColors.Button
                        )
                    ) {
                        Text(
                            text = if (staff.isActive) {
                                "Khóa tài khoản"
                            } else {
                                "Mở khóa tài khoản"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInformationRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = StaffManagementColors.SecondaryText,
            fontSize = 13.sp
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = value,
            color = StaffManagementColors.MainText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    HorizontalDivider(
        color = Color(0xFFF0F2ED)
    )
}
