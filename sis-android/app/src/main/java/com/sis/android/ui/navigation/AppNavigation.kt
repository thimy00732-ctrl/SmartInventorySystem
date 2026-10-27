
package com.sis.android.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.sis.android.data.repository.AuthRepository
import com.sis.android.data.session.SessionManager
import com.sis.android.ui.auth.LoginScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    val authRepository = remember {
        AuthRepository(sessionManager)
    }

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
                        authRepository.logout()
                        role = null
                    }
                )
            }

            "STAFF" -> {
                SimpleHomeScreen(
                    title = "Trang nhân viên",
                    role = "STAFF",
                    onLogout = {
                        authRepository.logout()
                        role = null
                    }
                )
            }

            else -> {
                authRepository.logout()
                role = null
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

        Button(onClick = onLogout) {
            Text("Đăng xuất")
        }
    }
}
