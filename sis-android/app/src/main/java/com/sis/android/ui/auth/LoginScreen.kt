
package com.sis.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sis.android.data.remote.MockAuthApi
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val api = remember { MockAuthApi() }
    val scope = rememberCoroutineScope()

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
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                // Buoc 1: Kiem tra du lieu dau vao
                usernameError = when {
                    username.isBlank() ->
                        "Vui lòng nhập tên đăng nhập"
                    else -> ""
                }

                passwordError = when {
                    password.isBlank() ->
                        "Vui lòng nhập mật khẩu"
                    else -> ""
                }

                message = ""
                isSuccess = false

                // Buoc 2: Chi goi API khi du lieu hop le
                if (usernameError.isEmpty() &&
                    passwordError.isEmpty()
                ) {
                    scope.launch {
                        loading = true

                        try {
                            val result = api.login(
                                username.trim(),
                                password
                            )

                            if (result) {
                                message = "Đăng nhập thành công!"
                                isSuccess = true
                            } else {
                                message =
                                    "Tên đăng nhập hoặc mật khẩu không đúng"
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: IOException) {
                            message =
                                "Lỗi mạng: Không thể kết nối đến máy chủ"
                        } catch (e: Exception) {
                            message =
                                "Đã xảy ra lỗi. Vui lòng thử lại"
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
