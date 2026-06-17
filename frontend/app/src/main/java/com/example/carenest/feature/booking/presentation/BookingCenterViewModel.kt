package com.example.carenest.feature.booking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.data.repository.BookingRepository
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingCenterUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val doctors: List<DoctorSummary> = emptyList(),
    val patientBookings: List<BookingResponse> = emptyList(),
    val doctorBookings: List<BookingResponse> = emptyList(),
    val selectedDoctorId: Long? = null,
    val requestType: BookingRequestType = BookingRequestType.OFFLINE_CLINIC,
    val preferredSchedule: String = "",
    val patientNote: String = ""
)

class BookingCenterViewModel(
    private val bookingRepository: BookingRepository,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingCenterUiState())
    val uiState: StateFlow<BookingCenterUiState> = _uiState.asStateFlow()

    fun refresh(canAccessDoctorWorkspace: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            try {
                val doctorsDeferred = async { bookingRepository.getDoctors() }
                val patientBookingsDeferred = async { bookingRepository.getMyBookings() }
                val doctorBookingsDeferred = if (canAccessDoctorWorkspace) {
                    async { bookingRepository.getDoctorBookings() }
                } else {
                    null
                }

                val doctors = doctorsDeferred.await()
                val selectedDoctorId = _uiState.value.selectedDoctorId
                    ?.takeIf { current -> doctors.any { it.id == current } }
                    ?: doctors.firstOrNull()?.id
                val patientBookings = patientBookingsDeferred.await()
                val doctorBookings = doctorBookingsDeferred?.await().orEmpty()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        doctors = doctors,
                        patientBookings = patientBookings,
                        doctorBookings = doctorBookings,
                        selectedDoctorId = selectedDoctorId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Không thể tải trung tâm đặt lịch"
                    )
                }
            }
        }
    }

    fun selectDoctor(doctorId: Long) {
        _uiState.update { it.copy(selectedDoctorId = doctorId, error = null, message = null) }
    }

    fun updateRequestType(type: BookingRequestType) {
        _uiState.update { it.copy(requestType = type, error = null, message = null) }
    }

    fun updatePreferredSchedule(value: String) {
        _uiState.update { it.copy(preferredSchedule = value, error = null, message = null) }
    }

    fun updatePatientNote(value: String) {
        _uiState.update { it.copy(patientNote = value, error = null, message = null) }
    }

    fun createBooking(profileId: Long?, canAccessDoctorWorkspace: Boolean) {
        val snapshot = _uiState.value
        val doctorId = snapshot.selectedDoctorId
        if (profileId == null) {
            _uiState.update { it.copy(error = "Hãy chọn đúng hồ sơ sức khỏe trước khi gửi yêu cầu") }
            return
        }
        if (doctorId == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn bác sĩ") }
            return
        }
        if (snapshot.patientNote.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng mô tả ngắn nhu cầu hoặc triệu chứng trước khi gửi") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                bookingRepository.createBooking(
                    doctorId = doctorId,
                    healthProfileId = profileId,
                    type = snapshot.requestType,
                    preferredSchedule = snapshot.preferredSchedule.trim(),
                    patientNote = snapshot.patientNote.trim()
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Đã gửi yêu cầu đặt lịch",
                        preferredSchedule = "",
                        patientNote = ""
                    )
                }
                refresh(canAccessDoctorWorkspace)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "Không thể gửi yêu cầu đặt lịch"
                    )
                }
            }
        }
    }

    fun confirmSchedule(
        bookingId: Long,
        scheduledAtIso: String,
        confirmedLocation: String,
        confirmedNote: String,
        canAccessDoctorWorkspace: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                bookingRepository.confirmSchedule(
                    bookingId = bookingId,
                    scheduledAtIso = scheduledAtIso,
                    confirmedLocation = confirmedLocation.trim(),
                    confirmedNote = confirmedNote.trim()
                )
                _uiState.update { it.copy(isSubmitting = false, message = "Đã xác nhận lịch cụ thể cho yêu cầu") }
                refresh(canAccessDoctorWorkspace)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "Không thể xác nhận lịch"
                    )
                }
            }
        }
    }

    fun rejectBooking(
        bookingId: Long,
        rejectionReason: String,
        canAccessDoctorWorkspace: Boolean
    ) {
        if (rejectionReason.isBlank()) {
            _uiState.update { it.copy(error = "Lý do từ chối không được để trống") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                bookingRepository.rejectBooking(bookingId, rejectionReason)
                _uiState.update { it.copy(isSubmitting = false, message = "Đã từ chối yêu cầu đặt lịch") }
                refresh(canAccessDoctorWorkspace)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "Không thể từ chối yêu cầu"
                    )
                }
            }
        }
    }

    fun cancelBooking(
        bookingId: Long,
        cancellationReason: String,
        canAccessDoctorWorkspace: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                bookingRepository.cancelBooking(bookingId, cancellationReason.trim())
                _uiState.update { it.copy(isSubmitting = false, message = "Đã hủy yêu cầu đặt lịch") }
                refresh(canAccessDoctorWorkspace)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "Không thể hủy yêu cầu"
                    )
                }
            }
        }
    }

    fun currentRole(): String? = secureSessionManager.getUserRole()
}

class BookingCenterViewModelFactory(
    private val bookingRepository: BookingRepository,
    private val secureSessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingCenterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingCenterViewModel(bookingRepository, secureSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
