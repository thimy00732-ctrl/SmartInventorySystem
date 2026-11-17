package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sis.android.data.model.Account
import com.sis.android.data.model.CreateUserRequest
import com.sis.android.data.model.UpdateUserRequest
import com.sis.android.data.remote.RetrofitClient
import com.sis.android.data.repository.AuthRepository
import com.sis.android.data.repository.AuthValidation
import com.sis.android.data.session.SessionManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessions = SessionManager(applicationContext)
        val repository = AuthRepository(RetrofitClient.create(sessions), sessions)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    SmartInventoryApp(repository, sessions)
                }
            }
        }
    }
}

@Composable
private fun SmartInventoryApp(repository: AuthRepository, sessions: SessionManager) {
    var user by remember { mutableStateOf(sessions.get()?.user) }
    var checkingSession by remember { mutableStateOf(sessions.get() != null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (user != null) {
            try {
                user = repository.refreshAccount()
            } catch (_: Exception) {
                if (sessions.get() == null) user = null
            } finally {
                checkingSession = false
            }
        }
    }

    when {
        checkingSession -> CenteredProgress()
        user == null -> LoginScreen(
            onLogin = { email, password ->
                val loggedIn = repository.login(email, password)
                user = loggedIn
            }
        )
        user!!.mustChangePassword -> ChangePasswordScreen(
            forced = true,
            onSubmit = { current, new, confirm ->
                repository.changePassword(current, new, confirm)
                user = null
            },
            onLogout = {
                scope.launch {
                    try {
                        repository.logout()
                    } catch (_: Exception) {
                    } finally {
                        user = null
                    }
                }
            }
        )
        else -> HomeScreen(
            user = user!!,
            repository = repository,
            onUserUpdated = { user = it },
            onLogout = {
                scope.launch {
                    try {
                        repository.logout()
                    } catch (_: Exception) {
                    } finally {
                        user = null
                    }
                }
            }
        )
    }
}

@Composable
private fun LoginScreen(onLogin: suspend (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    FormPage(title = "Smart Inventory", subtitle = "Sign in to your account") {
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        ErrorText(error)
        Spacer(Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            onClick = {
                error = AuthValidation.validateLogin(email, password)
                if (error == null) scope.launch {
                    loading = true
                    try {
                        onLogin(email, password)
                    } catch (e: Exception) {
                        error = AuthRepository.errorMessage(e)
                    } finally {
                        loading = false
                    }
                }
            }
        ) { Text(if (loading) "Signing in…" else "Sign in") }
    }
}

@Composable
private fun HomeScreen(
    user: Account,
    repository: AuthRepository,
    onUserUpdated: (Account) -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableStateOf("Profile") }
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Smart Inventory", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onLogout) { Text("Log out") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tab == "Profile") Button(onClick = { tab = "Profile" }) {
                    Text("My profile")
                } else OutlinedButton(onClick = { tab = "Profile" }) {
                    Text("My profile")
                }
                if (user.role == "ADMIN") {
                    if (tab == "Users") Button(onClick = { tab = "Users" }) {
                        Text("Users")
                    } else OutlinedButton(onClick = { tab = "Users" }) {
                        Text("Users")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (tab == "Users" && user.role == "ADMIN") {
                AdminUsersScreen(repository)
            } else {
                ProfileScreen(
                    user = user,
                    onSave = { name, phone ->
                        onUserUpdated(repository.updateProfile(name, phone))
                    },
                    onChangePassword = { current, new, confirm ->
                        repository.changePassword(current, new, confirm)
                    },
                    onPasswordChanged = onLogout
                )
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    user: Account,
    onSave: suspend (String, String?) -> Unit,
    onChangePassword: suspend (String, String, String) -> Unit,
    onPasswordChanged: () -> Unit
) {
    var name by remember(user.userId, user.fullName) { mutableStateOf(user.fullName) }
    var phone by remember(user.userId, user.phone) { mutableStateOf(user.phone.orEmpty()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("My profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = user.email, onValueChange = {}, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), enabled = false, singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = phone, onValueChange = { phone = it }, label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Text("Role: ${user.role}  •  Status: ${user.status}")
        ErrorText(error)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(
            enabled = !loading,
            onClick = {
                if (name.isBlank()) error = "Full name is required."
                else scope.launch {
                    loading = true
                    error = null
                    message = null
                    try {
                        onSave(name, phone)
                        message = "Profile updated."
                    } catch (e: Exception) {
                        error = AuthRepository.errorMessage(e)
                    } finally {
                        loading = false
                    }
                }
            }
        ) { Text(if (loading) "Saving…" else "Save profile") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showPassword = true }) { Text("Change password") }
    }
    if (showPassword) {
        ChangePasswordDialog(
            onDismiss = { showPassword = false },
            onSubmit = onChangePassword,
            onSuccess = {
                showPassword = false
                onPasswordChanged()
            }
        )
    }
}

@Composable
private fun ChangePasswordScreen(
    forced: Boolean,
    onSubmit: suspend (String, String, String) -> Unit,
    onLogout: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    FormPage(
        title = if (forced) "Change your password" else "Change password",
        subtitle = if (forced) "You must choose a new password before continuing." else ""
    ) {
        PasswordField(current, { current = it }, "Current password")
        Spacer(Modifier.height(10.dp))
        PasswordField(newPassword, { newPassword = it }, "New password")
        Spacer(Modifier.height(10.dp))
        PasswordField(confirm, { confirm = it }, "Confirm new password")
        ErrorText(error)
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                error = AuthValidation.validatePasswordChange(current, newPassword, confirm)
                if (error == null) scope.launch {
                    loading = true
                    try {
                        onSubmit(current, newPassword, confirm)
                    } catch (e: Exception) {
                        error = AuthRepository.errorMessage(e)
                    } finally {
                        loading = false
                    }
                }
            }
        ) { Text(if (loading) "Updating…" else "Update password") }
        if (forced) TextButton(onClick = onLogout) { Text("Log out") }
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: suspend (String, String, String) -> Unit,
    onSuccess: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column {
                PasswordField(current, { current = it }, "Current password")
                Spacer(Modifier.height(8.dp))
                PasswordField(newPassword, { newPassword = it }, "New password")
                Spacer(Modifier.height(8.dp))
                PasswordField(confirm, { confirm = it }, "Confirm new password")
                ErrorText(error)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = {
                    error = AuthValidation.validatePasswordChange(current, newPassword, confirm)
                    if (error == null) scope.launch {
                        loading = true
                        try {
                            onSubmit(current, newPassword, confirm)
                            onSuccess()
                        } catch (e: Exception) {
                            error = AuthRepository.errorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                }
            ) { Text(if (loading) "Updating…" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminUsersScreen(repository: AuthRepository) {
    var users by remember { mutableStateOf<List<Account>>(emptyList()) }
    var role by remember { mutableStateOf("All roles") }
    var status by remember { mutableStateOf("All statuses") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Account?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        loading = true
        error = null
        try {
            users = repository.listUsers(role.takeUnless { it.startsWith("All") }, status.takeUnless { it.startsWith("All") })
        } catch (e: Exception) {
            error = AuthRepository.errorMessage(e)
        } finally {
            loading = false
        }
    }

    LaunchedEffect(role, status) { reload() }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("User management", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showCreate = true }) { Text("Create user") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterMenu("Role: $role", listOf("All roles", "ADMIN", "WAREHOUSE_STAFF")) { role = it }
            FilterMenu("Status: $status", listOf("All statuses", "ACTIVE", "INACTIVE")) { status = it }
        }
        ErrorText(error)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        if (loading) LinearProgress()
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            users.forEach { account ->
                UserCard(
                    account = account,
                    onEdit = { editing = account },
                    onChangeStatus = { newStatus ->
                        scope.launch {
                            try {
                                repository.setStatus(account.userId, newStatus)
                                message = "Account status updated."
                                reload()
                            } catch (e: Exception) {
                                error = AuthRepository.errorMessage(e)
                            }
                        }
                    },
                    onChangeRole = { newRole ->
                        scope.launch {
                            try {
                                repository.setRole(account.userId, newRole)
                                message = "Account role updated."
                                reload()
                            } catch (e: Exception) {
                                error = AuthRepository.errorMessage(e)
                            }
                        }
                    }
                )
            }
            if (!loading && users.isEmpty() && error == null) {
                Text("No users match these filters.", Modifier.padding(12.dp))
            }
        }
    }

    if (showCreate) UserFormDialog(
        title = "Create user",
        submitLabel = "Create",
        allowRoleSelection = true,
        onDismiss = { showCreate = false }
    ) { name, email, phone, role ->
        repository.createUser(CreateUserRequest(name, email, phone, role))
        showCreate = false
        message = "User created. A temporary password was sent by email."
        reload()
    }
    editing?.let { account ->
        UserFormDialog(
            title = "Edit user",
            submitLabel = "Save",
            initialName = account.fullName,
            initialEmail = account.email,
            initialPhone = account.phone.orEmpty(),
            onDismiss = { editing = null }
        ) { name, email, phone, _ ->
            repository.updateUser(account.userId, UpdateUserRequest(name, email, phone))
            editing = null
            message = "User updated."
            reload()
        }
    }
}

@Composable
private fun UserCard(
    account: Account,
    onEdit: () -> Unit,
    onChangeStatus: (String) -> Unit,
    onChangeRole: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(account.fullName, style = MaterialTheme.typography.titleMedium)
            Text(account.email)
            Text("Phone: ${account.phone ?: "—"}")
            Text("${account.role}  •  ${account.status}")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = {
                    onChangeStatus(if (account.status == "ACTIVE") "INACTIVE" else "ACTIVE")
                }) { Text(if (account.status == "ACTIVE") "Deactivate" else "Activate") }
                TextButton(onClick = {
                    onChangeRole(if (account.role == "ADMIN") "WAREHOUSE_STAFF" else "ADMIN")
                }) { Text(if (account.role == "ADMIN") "Make staff" else "Make admin") }
            }
        }
    }
}

@Composable
private fun UserFormDialog(
    title: String,
    submitLabel: String,
    initialName: String = "",
    initialEmail: String = "",
    initialPhone: String = "",
    allowRoleSelection: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: suspend (String, String, String?, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(initialPhone) }
    var role by remember { mutableStateOf("WAREHOUSE_STAFF") }
    var roleExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Full name") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true)
                if (allowRoleSelection) {
                    Spacer(Modifier.height(8.dp))
                    Box {
                        OutlinedButton(onClick = { roleExpanded = true }) {
                            Text("Role: $role")
                        }
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            listOf("ADMIN", "WAREHOUSE_STAFF").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        role = option
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                ErrorText(error)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = {
                    error = AuthValidation.validateUser(name, email)
                    if (error == null) scope.launch {
                        loading = true
                        try {
                            onSubmit(name.trim(), email.trim(), phone.trim().ifBlank { null }, role)
                        } catch (e: Exception) {
                            error = AuthRepository.errorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                }
            ) { Text(if (loading) "Saving…" else submitLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FilterMenu(label: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) { Text(label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun FormPage(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle)
        }
        Spacer(Modifier.height(24.dp))
        content()
    }
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        visualTransformation = PasswordVisualTransformation()
    )
}

@Composable
private fun ErrorText(message: String?) {
    message?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun CenteredProgress() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(8.dp))
        Text("Loading…")
    }
}

@Composable
private fun LinearProgress() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator(Modifier.width(24.dp).height(24.dp), strokeWidth = 2.dp)
    }
    Spacer(Modifier.height(8.dp))
}