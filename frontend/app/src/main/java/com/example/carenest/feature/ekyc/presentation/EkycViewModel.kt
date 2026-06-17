package com.example.carenest.feature.ekyc.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.feature.ekyc.data.repository.EkycRepository
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse
import com.example.carenest.feature.ekyc.domain.model.VerificationStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EkycUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val verification: DoctorVerificationResponse? = null,
    val certificationNumber: String = "",
    val specialty: String = "",
    val hospitalName: String = "",
    val selectedCertificateUri: Uri? = null,
    val uploadedDocumentUrl: String? = null,
    val error: String? = null,
    val message: String? = null,
) {
    val status: VerificationStatus? = verification?.status
    val isLocked: Boolean = status == VerificationStatus.PENDING || status == VerificationStatus.APPROVED || isSubmitting
    val canSubmit: Boolean =
        !isLocked &&
            certificationNumber.isNotBlank() &&
            specialty.isNotBlank() &&
            hospitalName.isNotBlank() &&
            (selectedCertificateUri != null || !uploadedDocumentUrl.isNullOrBlank())
}

class EkycViewModel(
    private val repository: EkycRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EkycUiState())
    val uiState: StateFlow<EkycUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            try {
                val verification = withContext(ioDispatcher) {
                    repository.getMyVerification()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verification = verification,
                        certificationNumber = verification?.certificationNumber.orEmpty(),
                        specialty = verification?.specialty.orEmpty(),
                        hospitalName = verification?.hospitalName.orEmpty(),
                        uploadedDocumentUrl = verification?.documentUrl,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.userMessage("Không thể tải trạng thái hồ sơ"))
                }
            }
        }
    }

    fun onCertificationNumberChange(value: String) {
        _uiState.update { it.copy(certificationNumber = value, error = null, message = null) }
    }

    fun onSpecialtyChange(value: String) {
        _uiState.update { it.copy(specialty = value, error = null, message = null) }
    }

    fun onHospitalNameChange(value: String) {
        _uiState.update { it.copy(hospitalName = value, error = null, message = null) }
    }

    fun onCertificateSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                selectedCertificateUri = uri,
                uploadedDocumentUrl = if (uri == null) it.uploadedDocumentUrl else null,
                error = null,
                message = null,
            )
        }
    }

    fun submit(context: Context) {
        val snapshot = _uiState.value
        if (!snapshot.canSubmit) {
            _uiState.update { it.copy(error = "Vui lòng nhập đủ thông tin và chọn ảnh chứng chỉ") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = "Đang tải ảnh chứng chỉ...") }
            try {
                val documentUrl = withContext(ioDispatcher) {
                    snapshot.selectedCertificateUri?.let { uri ->
                        repository.uploadCertificate(context.applicationContext, uri)
                    } ?: snapshot.uploadedDocumentUrl.orEmpty()
                }

                _uiState.update { it.copy(uploadedDocumentUrl = documentUrl, message = "Đang gửi hồ sơ xác thực...") }

                val verification = withContext(ioDispatcher) {
                    repository.submitVerification(
                        certificationNumber = snapshot.certificationNumber.trim(),
                        specialty = snapshot.specialty.trim(),
                        hospitalName = snapshot.hospitalName.trim(),
                        documentUrl = documentUrl,
                    )
                }

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        verification = verification,
                        selectedCertificateUri = null,
                        uploadedDocumentUrl = verification.documentUrl,
                        message = "Hồ sơ của bạn đã được gửi và đang chờ admin duyệt",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        message = null,
                        error = e.userMessage("Không thể gửi hồ sơ xác thực bác sĩ"),
                    )
                }
            }
        }
    }
}

class EkycViewModelFactory(
    private val repository: EkycRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EkycViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EkycViewModel(repository) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}
