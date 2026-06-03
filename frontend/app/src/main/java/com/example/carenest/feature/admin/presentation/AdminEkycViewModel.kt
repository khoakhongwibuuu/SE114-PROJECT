package com.example.carenest.feature.admin.presentation

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

data class AdminEkycUiState(
    val isLoadingPending: Boolean = false,
    val isLoadingDoctors: Boolean = false,
    val pendingList: List<DoctorVerificationResponse> = emptyList(),
    val doctorList: List<DoctorSummary> = emptyList(),
    val error: String? = null,
    val message: String? = null,
)

class AdminEkycViewModel(
    private val repository: EkycRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminEkycUiState())
    val uiState: StateFlow<AdminEkycUiState> = _uiState.asStateFlow()

    init {
        loadPending()
        loadDoctors()
    }

    fun loadPending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPending = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.getPendingVerifications()
                }
            }.onSuccess { pending ->
                _uiState.update { it.copy(isLoadingPending = false, pendingList = pending) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingPending = false,
                        error = error.localizedMessage ?: "Không thể tải danh sách chờ duyệt",
                    )
                }
            }
        }
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDoctors = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.getAllDoctors()
                }
            }.onSuccess { doctors ->
                _uiState.update { it.copy(isLoadingDoctors = false, doctorList = doctors) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingDoctors = false,
                        error = error.localizedMessage ?: "Không thể tải danh sách bác sĩ",
                    )
                }
            }
        }
    }

    fun approveVerification(id: Long) {
        viewModelScope.launch {
            val target = _uiState.value.pendingList.firstOrNull { it.id == id }
            _uiState.update { it.copy(error = null, message = "Đang phê duyệt hồ sơ...") }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.approveVerification(id)
                }
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        pendingList = current.pendingList.filterNot { it.id == id },
                        doctorList = target?.toDoctorSummary()?.let { approved ->
                            listOf(approved) + current.doctorList.filterNot { it.id == approved.id }
                        } ?: current.doctorList,
                        message = "Đã phê duyệt hồ sơ bác sĩ",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        message = null,
                        error = error.localizedMessage ?: "Phê duyệt hồ sơ thất bại",
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
            _uiState.update { it.copy(error = null, message = "Đang từ chối hồ sơ...") }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.rejectVerification(id, reason.trim())
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        pendingList = it.pendingList.filterNot { pending -> pending.id == id },
                        message = "Đã từ chối hồ sơ bác sĩ",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        message = null,
                        error = error.localizedMessage ?: "Từ chối hồ sơ thất bại",
                    )
                }
            }
        }
    }

    fun revokeDoctor(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null, message = "Đang thu hồi quyền bác sĩ...") }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.revokeDoctor(userId)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        doctorList = it.doctorList.filterNot { doctor -> doctor.id == userId },
                        message = "Đã thu hồi quyền bác sĩ",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        message = null,
                        error = error.localizedMessage ?: "Thu hồi quyền bác sĩ thất bại",
                    )
                }
            }
        }
    }

    fun clearTransientMessage() {
        _uiState.update { it.copy(error = null, message = null) }
    }

    private fun DoctorVerificationResponse.toDoctorSummary(): DoctorSummary {
        return DoctorSummary(
            id = userId ?: id,
            email = userEmail.orEmpty(),
            fullName = userFullName?.takeIf { it.isNotBlank() } ?: "Bác sĩ CareNest",
            avatarUrl = null,
            certificationNumber = certificationNumber,
            specialty = specialty,
            hospitalName = hospitalName,
            documentUrl = documentUrl,
            approvedAt = updatedAt,
        )
    }
}

class AdminEkycViewModelFactory(
    private val repository: EkycRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminEkycViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminEkycViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
