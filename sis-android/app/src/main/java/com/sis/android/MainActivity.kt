
package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.model.ManagedStaff
import com.sis.android.ui.staffmanagement.StaffManagementDetailScreen
import com.sis.android.ui.staffmanagement.StaffManagementFormScreen
import com.sis.android.ui.staffmanagement.StaffManagementListScreen
import com.sis.android.ui.staffmanagement.StaffManagementViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                val staffViewModel: StaffManagementViewModel = viewModel()

                var selectedStaff by remember {
                    mutableStateOf<ManagedStaff?>(null)
                }

                var showAddForm by remember {
                    mutableStateOf(false)
                }

                var showEditForm by remember {
                    mutableStateOf(false)
                }

                // Màn hình thêm nhân viên
                if (showAddForm) {

                    StaffManagementFormScreen(
                        staff = null,
                        onBack = {
                            showAddForm = false
                        },
                        onSave = { newStaff ->
                            staffViewModel.addStaff(newStaff)
                            showAddForm = false
                        }
                    )

                    // Màn hình chỉnh sửa nhân viên
                } else if (showEditForm && selectedStaff != null) {

                    StaffManagementFormScreen(
                        staff = selectedStaff,
                        onBack = {
                            showEditForm = false
                        },
                        onSave = { updatedStaff ->
                            staffViewModel.updateStaff(updatedStaff)
                            selectedStaff = updatedStaff
                            showEditForm = false
                        }
                    )

                    // Màn hình chi tiết nhân viên
                } else if (selectedStaff != null) {

                    StaffManagementDetailScreen(
                        staff = selectedStaff!!,
                        isAdmin = true,

                        onBack = {
                            selectedStaff = null
                        },

                        onEdit = {
                            showEditForm = true
                        },

                        onToggleActive = {
                            staffViewModel.toggleActive(selectedStaff!!)

                            selectedStaff = selectedStaff!!.copy(
                                isActive = !selectedStaff!!.isActive
                            )
                        }
                    )

                    // Màn hình danh sách nhân viên
                } else {

                    StaffManagementListScreen(
                        isAdmin = true,
                        viewModel = staffViewModel,

                        onAddStaff = {
                            showAddForm = true
                        },

                        onOpenStaff = { staff ->
                            selectedStaff = staff
                        }
                    )
                }
            }
        }
    }
}
