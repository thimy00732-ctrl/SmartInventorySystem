
package com.sis.android.ui.staffmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.ManagedStaff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementFormScreen(
    staff: ManagedStaff? = null,
    onBack: () -> Unit,
    onSave: (ManagedStaff) -> Unit
) {
    var fullName by remember(staff?.id) {
        mutableStateOf(staff?.fullName ?: "")
    }

    var email by remember(staff?.id) {
        mutableStateOf(staff?.email ?: "")
    }

    var branch by remember(staff?.id) {
        mutableStateOf(staff?.branch ?: "")
    }

    var role by remember(staff?.id) {
        mutableStateOf(staff?.role ?: "STAFF")
    }

    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                title = if (staff == null) {
                    "Thêm nhân viên"
                } else {
                    "Chỉnh sửa nhân viên"
                },
                subtitle = "Nhập đầy đủ thông tin tài khoản nhân viên",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(top = 22.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Thông tin cơ bản",
                    color = StaffManagementColors.OliveDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                StaffInputField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = ""
                    },
                    label = "Họ và tên",
                    placeholder = "Nhập họ và tên nhân viên"
                )

                StaffInputField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = "Email",
                    placeholder = "example@email.com"
                )

                StaffInputField(
                    value = branch,
                    onValueChange = {
                        branch = it
                        errorMessage = ""
                    },
                    label = "Chi nhánh",
                    placeholder = "Nhập chi nhánh làm việc"
                )

                Column {
                    Text(
                        text = "Vai trò",
                        color = StaffManagementColors.OliveDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(7.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        }
                    ) {
                        OutlinedTextField(
                            value = when (role) {
                                "ADMIN" -> "Quản trị viên"
                                "MANAGER" -> "Quản lý"
                                else -> "Nhân viên"
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(13.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = StaffManagementColors.Border,
                                focusedBorderColor = StaffManagementColors.Button
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            listOf(
                                "STAFF",
                                "MANAGER",
                                "ADMIN"
                            ).forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (item) {
                                                "ADMIN" -> "Quản trị viên"
                                                "MANAGER" -> "Quản lý"
                                                else -> "Nhân viên"
                                            }
                                        )
                                    },
                                    onClick = {
                                        role = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        when {
                            fullName.isBlank() ||
                                    email.isBlank() ||
                                    branch.isBlank() -> {
                                errorMessage =
                                    "Vui lòng nhập đầy đủ thông tin."
                            }

                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email.trim()).matches() -> {
                                errorMessage =
                                    "Địa chỉ email không hợp lệ."
                            }

                            else -> {
                                onSave(
                                    ManagedStaff(
                                        id = staff?.id ?: 0,
                                        fullName = fullName.trim(),
                                        email = email.trim(),
                                        branch = branch.trim(),
                                        role = role,
                                        isActive = staff?.isActive ?: true
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StaffManagementColors.Button
                    )
                ) {
                    Text(
                        text = if (staff == null) {
                            "Tạo tài khoản nhân viên"
                        } else {
                            "Lưu thay đổi"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StaffInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            color = StaffManagementColors.OliveDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(7.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFB8BED0),
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = StaffManagementColors.Border,
                focusedBorderColor = StaffManagementColors.Button
            )
        )
    }
}
