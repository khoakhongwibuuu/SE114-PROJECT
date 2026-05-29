package com.example.carenest.feature.ekyc.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.ekyc.data.repository.EkycRepository
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminVerificationUiState(
    val isLoadingPending: Boolean = false,
    val isLoadingDoctors: Boolean = false,
    val pendingList: List<DoctorVerificationResponse> = emptyList(),
    val doctorList: List<DoctorSummary> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

class AdminVerificationViewModel(
    private val repository: EkycRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminVerificationUiState())
    val uiState: StateFlow<AdminVerificationUiState> = _uiState.asStateFlow()

    init {
        loadPending()
        loadDoctors()
    }

    fun loadPending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPending = true, error = null) }
            try {
                val pending = withContext(Dispatchers.IO) {
                    repository.getPendingVerifications()
                }
                _uiState.update {
                    it.copy(isLoadingPending = false, pendingList = pending)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingPending = false,
                        error = e.localizedMessage ?: "Không thể tải danh sách chờ duyệt"
                    )
                }
            }
        }
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDoctors = true, error = null) }
            try {
                val doctors = withContext(Dispatchers.IO) {
                    repository.getAllDoctors()
                }
                _uiState.update {
                    it.copy(isLoadingDoctors = false, doctorList = doctors)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingDoctors = false,
                        error = e.localizedMessage ?: "Không thể tải danh sách bác sĩ"
                    )
                }
            }
        }
    }

    fun approveVerification(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null, message = "Đang phê duyệt...") }
            try {
                withContext(Dispatchers.IO) {
                    repository.approveVerification(id)
                }
                _uiState.update { it.copy(message = "Đã phê duyệt hồ sơ bác sĩ thành công") }
                loadPending()
                loadDoctors()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = null,
                        error = e.localizedMessage ?: "Phê duyệt hồ sơ thất bại"
                    )
                }
            }
        }
    }

    fun rejectVerification(id: Long, reason: String) {
        if (reason.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập lý do từ chối") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(error = null, message = "Đang xử lý từ chối...") }
            try {
                withContext(Dispatchers.IO) {
                    repository.rejectVerification(id, reason.trim())
                }
                _uiState.update { it.copy(message = "Đã từ chối hồ sơ bác sĩ") }
                loadPending()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = null,
                        error = e.localizedMessage ?: "Từ chối hồ sơ thất bại"
                    )
                }
            }
        }
    }

    fun revokeDoctor(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null, message = "Đang thu hồi quyền...") }
            try {
                withContext(Dispatchers.IO) {
                    repository.revokeDoctor(userId)
                }
                _uiState.update { it.copy(message = "Đã thu hồi quyền bác sĩ thành công") }
                loadDoctors()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = null,
                        error = e.localizedMessage ?: "Thu hồi quyền bác sĩ thất bại"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, message = null) }
    }
}

class AdminVerificationViewModelFactory(
    private val repository: EkycRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminVerificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
