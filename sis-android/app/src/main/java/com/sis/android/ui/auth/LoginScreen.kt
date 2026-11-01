
package com.sis.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.repository.AuthRepository
import com.sis.android.data.session.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

private val DarkGreen = Color(0xFF38530D)
private val BackgroundGreen = Color(0xFF263D16)
private val LightGreen = Color(0xFFEDF1E8)
private val BorderGreen = Color(0xFFD8E0D0)
private val TextGray = Color(0xFF718096)

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        AuthRepository(SessionManager(context))
    }

    val scope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf("ADMIN") }

    // Khôi phục email đã ghi nhớ.
    var email by remember {
        mutableStateOf(repository.getRememberedEmail())
    }

    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Khôi phục trạng thái checkbox.
    var rememberAccount by remember {
        mutableStateOf(repository.shouldRememberLogin())
    }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGreen,
                        Color(0xFF344E19),
                        Color(0xFF60783B)
                    )
                )
            )
    ) {
        val screenWidth = this.maxWidth
        val compact = this.maxHeight < 750.dp
        val horizontalPadding =
            if (screenWidth < 400.dp) 18.dp else 30.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = horizontalPadding,
                    vertical = if (compact) 20.dp else 36.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Warehouse
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        Color.White.copy(alpha = 0.10f),
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.25f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = "Warehouse",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Warehouse",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Quản lý kho",
                color = Color.White,
                fontSize = 12.sp
            )

            Spacer(
                Modifier.height(if (compact) 24.dp else 36.dp)
            )

            // Khung đăng nhập
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(28.dp)
                    )
                    .padding(
                        horizontal = if (screenWidth < 400.dp) {
                            20.dp
                        } else {
                            30.dp
                        },
                        vertical = 30.dp
                    )
            ) {
                Text(
                    text = "Chào mừng trở lại",
                    color = Color(0xFF10213D),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Đăng nhập để tiếp tục quản lý kho",
                    color = TextGray,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(28.dp))

                // Chọn vai trò
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            LightGreen,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp)
                ) {
                    RoleTab(
                        text = "ADMIN",
                        selected = selectedRole == "ADMIN",
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedRole = "ADMIN"
                        errorMessage = ""
                    }

                    RoleTab(
                        text = "NHÂN VIÊN",
                        selected = selectedRole == "STAFF",
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedRole = "STAFF"
                        errorMessage = ""
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Email
                Text(
                    text = "Email đăng nhập",
                    color = Color(0xFF243047),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Nhập email",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextGray
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    shape = RoundedCornerShape(17.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        unfocusedBorderColor = BorderGreen,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Mật khẩu
                Text(
                    text = "Mật khẩu",
                    color = Color(0xFF243047),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Nhập mật khẩu",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextGray
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = if (showPassword) "Ẩn" else "Hiện",
                            color = DarkGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    showPassword = !showPassword
                                }
                                .padding(horizontal = 12.dp)
                        )
                    },
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    shape = RoundedCornerShape(17.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        unfocusedBorderColor = BorderGreen,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Ghi nhớ tài khoản và quên mật khẩu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberAccount,
                            onCheckedChange = { checked ->
                                rememberAccount = checked
                                errorMessage = ""

                                if (!checked) {
                                    // Bỏ ghi nhớ và xóa email đã lưu.
                                    repository.logoutAndForgetEmail()
                                    email = ""
                                }
                            },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = DarkGreen
                            )
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            text = "Ghi nhớ",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "Quên mật khẩu?",
                        color = DarkGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            errorMessage =
                                "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu."
                        }
                    )
                }

                // Hiển thị thông báo lỗi
                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Nút đăng nhập
                Button(
                    onClick = {
                        when {
                            email.isBlank() -> {
                                errorMessage = "Vui lòng nhập email."
                            }

                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email.trim())
                                .matches() -> {
                                errorMessage = "Email không hợp lệ."
                            }

                            password.isBlank() -> {
                                errorMessage = "Vui lòng nhập mật khẩu."
                            }

                            else -> {
                                scope.launch {
                                    loading = true
                                    errorMessage = ""

                                    try {
                                        val role = repository.login(
                                            email = email.trim(),
                                            password = password,
                                            rememberLogin = rememberAccount
                                        )

                                        // Kiểm tra email khớp vai trò đã chọn.
                                        if (role != selectedRole) {
                                            repository.logout()
                                            errorMessage =
                                                "Email không thuộc vai trò đã chọn."
                                        } else {
                                            onLoginSuccess(role)
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: IOException) {
                                        errorMessage =
                                            "Không thể kết nối đến máy chủ."
                                    } catch (e: IllegalArgumentException) {
                                        errorMessage = e.message
                                            ?: "Thông tin đăng nhập không đúng."
                                    } catch (e: Exception) {
                                        errorMessage =
                                            "Đã xảy ra lỗi. Vui lòng thử lại."
                                    } finally {
                                        loading = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        disabledContainerColor = DarkGreen.copy(alpha = 0.6f)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Đăng nhập",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.width(14.dp))

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))


            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RoleTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .background(
                color = if (selected) {
                    DarkGreen
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
