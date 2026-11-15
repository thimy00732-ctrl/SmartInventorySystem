package com.sis.android.ui.staffmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.ManagedStaff

/**
 * Form tạo mới / chỉnh sửa nhân viên.
 *
 * - Tạo mới (staff == null): cần fullName, email, phone, password, role
 * - Chỉnh sửa (staff != null): fullName, email, phone, role (password để trống = không đổi)
 *
 * Role hợp lệ từ backend: "ADMIN" | "WAREHOUSE_STAFF"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementFormScreen(
    staff: ManagedStaff? = null,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onSave: (fullName: String, email: String, phone: String, password: String, role: String) -> Unit
) {
    val isEditMode = staff != null

    var fullName by remember(staff?.id) { mutableStateOf(staff?.fullName ?: "") }
    var email by remember(staff?.id) { mutableStateOf(staff?.email ?: "") }
    var phone by remember(staff?.id) { mutableStateOf(staff?.phone ?: "") }
    var password by remember(staff?.id) { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var role by remember(staff?.id) { mutableStateOf(staff?.role ?: "WAREHOUSE_STAFF") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val roleOptions = listOf("WAREHOUSE_STAFF" to "Nhân viên kho", "ADMIN" to "Quản trị viên")

    Scaffold(containerColor = StaffManagementColors.Background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StaffManagementColors.Background)
        ) {
            StaffManagementHeader(
                title = if (!isEditMode) "Thêm nhân viên" else "Chỉnh sửa nhân viên",
                subtitle = "Nhập đầy đủ thông tin tài khoản nhân viên",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                    onValueChange = { fullName = it; errorMessage = "" },
                    label = "Họ và tên",
                    placeholder = "Nhập họ và tên nhân viên"
                )

                StaffInputField(
                    value = email,
                    onValueChange = { email = it; errorMessage = "" },
                    label = "Email",
                    placeholder = "example@email.com",
                    keyboardType = KeyboardType.Email
                )

                StaffInputField(
                    value = phone,
                    onValueChange = { phone = it; errorMessage = "" },
                    label = "Số điện thoại",
                    placeholder = "0912345678",
                    keyboardType = KeyboardType.Phone
                )

                // Mật khẩu — bắt buộc khi tạo mới, tùy chọn khi chỉnh sửa
                Column {
                    Text(
                        text = if (!isEditMode) "Mật khẩu" else "Mật khẩu mới (để trống nếu không đổi)",
                        color = StaffManagementColors.OliveDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(7.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Tối thiểu 8 ký tự, gồm chữ hoa, số",
                                color = Color(0xFFB8BED0),
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = if (showPassword) "Ẩn" else "Hiện",
                                color = StaffManagementColors.Button,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { showPassword = !showPassword }
                                    .padding(horizontal = 12.dp)
                            )
                        },
                        visualTransformation = if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = StaffManagementColors.Border,
                            focusedBorderColor = StaffManagementColors.Button
                        )
                    )
                }

                // Dropdown vai trò
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
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = roleOptions.find { it.first == role }?.second ?: role,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
                            onDismissRequest = { expanded = false }
                        ) {
                            roleOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        role = value
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        when {
                            fullName.isBlank() -> {
                                errorMessage = "Vui lòng nhập họ và tên."
                            }
                            email.isBlank() -> {
                                errorMessage = "Vui lòng nhập email."
                            }
                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email.trim()).matches() -> {
                                errorMessage = "Địa chỉ email không hợp lệ."
                            }
                            phone.isBlank() -> {
                                errorMessage = "Vui lòng nhập số điện thoại."
                            }
                            !isEditMode && password.isBlank() -> {
                                errorMessage = "Vui lòng nhập mật khẩu."
                            }
                            else -> {
                                onSave(
                                    fullName.trim(),
                                    email.trim(),
                                    phone.trim(),
                                    password,
                                    role
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StaffManagementColors.Button,
                        disabledContainerColor = StaffManagementColors.Button.copy(alpha = 0.6f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (!isEditMode) "Tạo tài khoản nhân viên" else "Lưu thay đổi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
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
                Text(text = placeholder, color = Color(0xFFB8BED0), fontSize = 13.sp)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
