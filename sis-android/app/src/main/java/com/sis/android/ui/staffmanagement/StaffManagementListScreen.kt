
package com.sis.android.ui.staffmanagement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sis.android.data.model.ManagedStaff
@Composable
fun StaffManagementListScreen(
    isAdmin: Boolean,
    onAddStaff: () -> Unit,
    onOpenStaff: (ManagedStaff) -> Unit,
    viewModel: StaffManagementViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = StaffManagementColors.Background,
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onAddStaff,
                    containerColor = StaffManagementColors.Button,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm nhân viên"
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Thêm nhân viên",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StaffManagementColors.Background)
        ) {

            StaffManagementHeader(
                title = "Quản lý nhân viên",
                subtitle = "${state.staff.size} tài khoản · ${state.activeCount} đang hoạt động"
            )

            // Ô tìm kiếm
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 18.dp,
                        end = 18.dp,
                        top = 24.dp,
                        bottom = 14.dp
                    ),
                placeholder = {
                    Text(
                        "Tìm tên hoặc email...",
                        color = Color(0xFFB8BED0),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm",
                        tint = Color(0xFF9AA4B8)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E4EC),
                    focusedBorderColor = StaffManagementColors.OliveLight
                )
            )

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = StaffManagementColors.Button)
                }
            }

            // Error message
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            // Danh sách nhân viên
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
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
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    state.filteredStaff.forEachIndexed { index, staff ->

                        StaffManagementItem(
                            staff = staff,
                            onClick = {
                                onOpenStaff(staff)
                            }
                        )

                        if (index != state.filteredStaff.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFFE9EDE6),
                                thickness = 1.dp
                            )
                        }
                    }

                    if (state.filteredStaff.isEmpty() && !state.isLoading) {
                        Text(
                            text = "Không tìm thấy nhân viên",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffManagementItem(
    staff: ManagedStaff,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFFEEF2E6),
                    RoundedCornerShape(15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = staff.fullName
                    .split(" ")
                    .takeLast(2)
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .joinToString(""),
                color = StaffManagementColors.Button,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        // Thông tin nhân viên
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = staff.fullName,
                color = StaffManagementColors.MainText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${staff.email} · ${if (staff.phone.isNotBlank()) staff.phone else staff.role}",
                color = StaffManagementColors.SecondaryText,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        // Trạng thái
        Surface(
            shape = CircleShape,
            color = if (staff.isActive) {
                Color(0xFFEBF7EF)
            } else {
                Color(0xFFF0F2F6)
            }
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 6.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            if (staff.isActive) {
                                Color(0xFF238747)
                            } else {
                                Color(0xFF7D8799)
                            },
                            CircleShape
                        )
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = if (staff.isActive) {
                        "Hoạt động"
                    } else {
                        "Ngừng hoạt động"
                    },
                    fontSize = 9.sp,
                    color = if (staff.isActive) {
                        Color(0xFF238747)
                    } else {
                        Color(0xFF687386)
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Chi tiết",
            tint = StaffManagementColors.MainText,
            modifier = Modifier.size(23.dp)
        )
    }
}
