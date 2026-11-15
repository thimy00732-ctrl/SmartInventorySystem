
package com.sis.android.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sis.android.di.AppModule
import com.sis.android.ui.auth.LoginScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val authRepository = remember {
        AppModule.provideAuthRepository(context)
    }

    // Kiểm tra phiên đăng nhập đã lưu khi mở ứng dụng.
    var role by remember {
        mutableStateOf(authRepository.getSavedRole())
    }

    if (role == null) {
        LoginScreen(
            onLoginSuccess = { loggedInRole ->
                role = loggedInRole
            }
        )
    } else {
        when (role) {
            "ADMIN" -> {
                SimpleHomeScreen(
                    title = "Trang quản trị viên",
                    role = "ADMIN",
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                            role = null
                        }
                    }
                )
            }

            "STAFF" -> {
                SimpleHomeScreen(
                    title = "Trang nhân viên",
                    role = "STAFF",
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                            role = null
                        }
                    }
                )
            }

            else -> {
                LaunchedEffect(Unit) {
                    authRepository.logout()
                    role = null
                }
            }
        }
    }
}

@Composable
private fun SimpleHomeScreen(
    title: String,
    role: String,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Vai trò: $role")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout
            ) {
                Text(text = "Đăng xuất")
            }
        }
    }
}
