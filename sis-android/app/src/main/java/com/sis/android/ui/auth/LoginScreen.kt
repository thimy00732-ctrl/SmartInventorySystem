
package com.sis.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sis.android.data.repository.AuthRepository
import com.sis.android.data.session.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    val authRepository = remember {
        AuthRepository(SessionManager(context))
    }

    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ĐĂNG NHẬP HỆ THỐNG",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = ""
                message = ""
            },
            label = { Text("Tên đăng nhập") },
            isError = usernameError.isNotEmpty(),
            supportingText = {
                if (usernameError.isNotEmpty()) {
                    Text(usernameError)
                }
            },
            singleLine = true,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
                message = ""
            },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = passwordError.isNotEmpty(),
            supportingText = {
                if (passwordError.isNotEmpty()) {
                    Text(passwordError)
                }
            },
            singleLine = true,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (isSuccess) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                usernameError = if (username.isBlank()) {
                    "Vui lòng nhập tên đăng nhập"
                } else {
                    ""
                }

                passwordError = if (password.isBlank()) {
                    "Vui lòng nhập mật khẩu"
                } else {
                    ""
                }

                message = ""
                isSuccess = false

                if (usernameError.isEmpty() &&
                    passwordError.isEmpty()
                ) {
                    scope.launch {
                        loading = true

                        try {
                            // Repository xử lý đăng nhập và lưu phiên
                            val role = authRepository.login(
                                username.trim(),
                                password
                            )

                            isSuccess = true
                            message = "Đăng nhập thành công!"

                            // Chuyển sang giao diện theo quyền
                            onLoginSuccess(role)

                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: IOException) {
                            message =
                                "Lỗi mạng: Không thể kết nối đến máy chủ"
                        } catch (e: IllegalArgumentException) {
                            message = e.message
                                ?: "Thông tin đăng nhập không đúng"
                        } catch (e: Exception) {
                            message = "Đã xảy ra lỗi. Vui lòng thử lại"
                        } finally {
                            loading = false
                        }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang đăng nhập...")
            } else {
                Text("Đăng nhập")
            }
        }
    }
}
