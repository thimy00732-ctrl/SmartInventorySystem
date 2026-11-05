package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.model.ManagedStaff
import com.sis.android.di.AppModule
import com.sis.android.ui.auth.LoginScreen
import com.sis.android.ui.staffmanagement.StaffManagementDetailScreen
import com.sis.android.ui.staffmanagement.StaffManagementFormScreen
import com.sis.android.ui.staffmanagement.StaffManagementListScreen
import com.sis.android.ui.staffmanagement.StaffManagementViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppNavigator()
            }
        }
    }

    @Composable
    private fun AppNavigator() {
        val sessionManager = remember { AppModule.provideSessionManager(this) }
        val staffRepository = remember { AppModule.provideStaffRepository() }

        // Kiểm tra session đã lưu khi mở app
        var currentRole by remember {
            mutableStateOf(
                AppModule.provideAuthRepository(this).getSavedRole()
            )
        }

        if (currentRole == null) {
            // Chưa đăng nhập → hiện màn hình Login
            LoginScreen(
                onLoginSuccess = { role ->
                    currentRole = role
                }
            )
        } else {
            // Đã đăng nhập → hiện Staff Management
            val isAdmin = currentRole == "ADMIN"

            val staffViewModel: StaffManagementViewModel = viewModel(
                factory = StaffManagementViewModel.Factory(
                    repository = staffRepository,
                    sessionManager = sessionManager
                )
            )

            val uiState by staffViewModel.uiState.collectAsStateWithLifecycle()

            var selectedStaff by remember { mutableStateOf<ManagedStaff?>(null) }
            var showAddForm by remember { mutableStateOf(false) }
            var showEditForm by remember { mutableStateOf(false) }

            when {
                // Màn hình thêm nhân viên
                showAddForm -> {
                    StaffManagementFormScreen(
                        staff = null,
                        isLoading = uiState.isLoading,
                        onBack = { showAddForm = false },
                        onSave = { fullName, email, phone, password, role ->
                            staffViewModel.addStaff(fullName, email, phone, password, role)
                            showAddForm = false
                        }
                    )
                }

                // Màn hình chỉnh sửa nhân viên
                showEditForm && selectedStaff != null -> {
                    StaffManagementFormScreen(
                        staff = selectedStaff,
                        isLoading = uiState.isLoading,
                        onBack = { showEditForm = false },
                        onSave = { fullName, email, phone, _, role ->
                            val updated = selectedStaff!!.copy(
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                role = role
                            )
                            staffViewModel.updateStaff(updated)
                            selectedStaff = updated
                            showEditForm = false
                        }
                    )
                }

                // Màn hình chi tiết nhân viên
                selectedStaff != null -> {
                    // Đồng bộ selectedStaff với dữ liệu mới nhất từ ViewModel
                    val latestStaff = uiState.staff.find { it.id == selectedStaff!!.id }
                        ?: selectedStaff!!

                    StaffManagementDetailScreen(
                        staff = latestStaff,
                        isAdmin = isAdmin,
                        onBack = { selectedStaff = null },
                        onEdit = { showEditForm = true },
                        onToggleActive = {
                            staffViewModel.toggleActive(latestStaff)
                        }
                    )
                }

                // Màn hình danh sách nhân viên
                else -> {
                    StaffManagementListScreen(
                        isAdmin = isAdmin,
                        viewModel = staffViewModel,
                        onAddStaff = { showAddForm = true },
                        onOpenStaff = { staff -> selectedStaff = staff }
                    )
                }
            }
        }
    }
}
