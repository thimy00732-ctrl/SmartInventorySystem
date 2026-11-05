package com.sis.android.ui.staffmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sis.android.data.model.ManagedStaff
import com.sis.android.data.repository.StaffManagementRepository
import com.sis.android.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffManagementUiState(
    val staff: List<ManagedStaff> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
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
    private val repository: StaffManagementRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState

    init {
        loadStaff()
    }

    /** Tải danh sách nhân viên từ API */
    fun loadStaff() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getBearerToken()
                    ?: throw Exception("Chưa đăng nhập")
                val staffList = repository.getStaff(token)
                _uiState.update {
                    it.copy(isLoading = false, staff = staffList)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Tạo nhân viên mới qua API */
    fun addStaff(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getBearerToken()
                    ?: throw Exception("Chưa đăng nhập")
                val newStaff = repository.addStaff(token, fullName, email, phone, password, role)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        staff = it.staff + newStaff,
                        successMessage = "Tạo nhân viên thành công"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    /** Cập nhật thông tin nhân viên qua API */
    fun updateStaff(staff: ManagedStaff) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getBearerToken()
                    ?: throw Exception("Chưa đăng nhập")
                val updated = repository.updateStaff(token, staff)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        staff = state.staff.map { if (it.id == updated.id) updated else it },
                        successMessage = "Cập nhật thành công"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    /** Đổi trạng thái active/inactive nhân viên */
    fun toggleActive(staff: ManagedStaff) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getBearerToken()
                    ?: throw Exception("Chưa đăng nhập")
                val updated = repository.setActive(token, staff.id, !staff.isActive)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        staff = state.staff.map { if (it.id == updated.id) updated else it }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    class Factory(
        private val repository: StaffManagementRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StaffManagementViewModel(repository, sessionManager) as T
        }
    }
}
