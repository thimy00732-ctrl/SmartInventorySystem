
package com.sis.android.ui.staffmanagement

import androidx.lifecycle.ViewModel
import com.sis.android.data.model.ManagedStaff
import com.sis.android.data.repository.InMemoryStaffManagementRepository
import com.sis.android.data.repository.StaffManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class StaffManagementUiState(
    val staff: List<ManagedStaff> = emptyList(),
    val searchQuery: String = ""
) {
    val filteredStaff: List<ManagedStaff>
        get() = staff.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }

    val activeCount: Int
        get() = staff.count { it.isActive }
}

class StaffManagementViewModel(
    private val repository: StaffManagementRepository =
        InMemoryStaffManagementRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StaffManagementUiState(
                staff = repository.getStaff()
            )
        )

    val uiState: StateFlow<StaffManagementUiState> = _uiState

    fun search(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
    }

    fun addStaff(staff: ManagedStaff) {
        repository.addStaff(staff)
        refresh()
    }

    fun updateStaff(staff: ManagedStaff) {
        repository.updateStaff(staff)
        refresh()
    }

    fun toggleActive(staff: ManagedStaff) {
        repository.setActive(staff.id, !staff.isActive)
        refresh()
    }

    private fun refresh() {
        _uiState.update {
            it.copy(staff = repository.getStaff())
        }
    }
}
