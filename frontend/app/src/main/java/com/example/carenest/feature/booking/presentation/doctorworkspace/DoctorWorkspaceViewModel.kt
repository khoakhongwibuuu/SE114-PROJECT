package com.example.carenest.feature.booking.presentation.doctorworkspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.feature.booking.domain.port.BookingDataSource
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorWorkspaceUiState(
    val bookings: List<BookingResponse> = emptyList(),
    val isLoading: Boolean = false,
    val busyBookingIds: Set<Long> = emptySet(),
    val error: String? = null
)

class DoctorWorkspaceViewModel(
    private val repository: BookingDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorWorkspaceUiState())
    val uiState: StateFlow<DoctorWorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val bookings = repository.getDoctorBookings()
                val deduped = prioritizeDoctorWorkspaceBookings(bookings)
                _uiState.update { it.copy(isLoading = false, bookings = deduped) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.userMessage("Không thể tải phòng khám số")) }
            }
        }
    }

    fun approveBooking(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(busyBookingIds = it.busyBookingIds + id, error = null) }
            val result = repository.approveBooking(id)
            if (result.isSuccess) {
                val approved = result.getOrNull()
                    ?: run {
                        val message = "Thiếu dữ liệu phản hồi khi chấp nhận yêu cầu"
                        _uiState.update { it.copy(error = message, busyBookingIds = it.busyBookingIds - id) }
                        onError(message)
                        return@launch
                    }
                // Update local list
                val updatedBookings = _uiState.value.bookings.map {
                    if (it.id == id) approved else it
                }
                _uiState.update { it.copy(bookings = updatedBookings, busyBookingIds = it.busyBookingIds - id, error = null) }
                onSuccess()
            } else {
                val message = result.exceptionOrNull()?.userMessage("Không thể chấp nhận yêu cầu")
                    ?: "Không thể chấp nhận yêu cầu"
                _uiState.update { it.copy(error = message, busyBookingIds = it.busyBookingIds - id) }
                onError(message)
            }
        }
    }

    fun confirmSchedule(
        id: Long,
        scheduledAtIso: String,
        confirmedLocation: String?,
        confirmedNote: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(busyBookingIds = it.busyBookingIds + id, error = null) }
            try {
                val updated = repository.confirmSchedule(
                    bookingId = id,
                    scheduledAtIso = scheduledAtIso,
                    confirmedLocation = confirmedLocation,
                    confirmedNote = confirmedNote
                )
                val updatedBookings = _uiState.value.bookings.map {
                    if (it.id == id) updated else it
                }
                _uiState.update { it.copy(bookings = updatedBookings, busyBookingIds = it.busyBookingIds - id, error = null) }
                onSuccess()
            } catch (e: Exception) {
                val message = e.userMessage("Không thể xác nhận lịch")
                _uiState.update { it.copy(error = message, busyBookingIds = it.busyBookingIds - id) }
                onError(message)
            }
        }
    }

    fun rejectBooking(id: Long, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(busyBookingIds = it.busyBookingIds + id, error = null) }
            try {
                val result = repository.rejectBooking(id, reason)
                // Update local list
                val updatedBookings = _uiState.value.bookings.map {
                    if (it.id == id) result else it
                }
                _uiState.update { it.copy(bookings = updatedBookings, busyBookingIds = it.busyBookingIds - id, error = null) }
                onSuccess()
            } catch (e: Exception) {
                val message = e.userMessage("Không thể từ chối yêu cầu")
                _uiState.update { it.copy(error = message, busyBookingIds = it.busyBookingIds - id) }
                onError(message)
            }
        }
    }

    class Factory(private val repository: BookingDataSource) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DoctorWorkspaceViewModel(repository) as T
        }
    }
}

internal fun prioritizeDoctorWorkspaceBookings(bookings: List<BookingResponse>): List<BookingResponse> {
    // Keep one actionable item per patient and booking channel.
    val statusPriority = mapOf(
        BookingStatus.ACTIVE to 7,
        BookingStatus.APPROVED to 6,
        BookingStatus.PENDING to 5,
        BookingStatus.RESTRICTED to 4,
        BookingStatus.COMPLETED to 3,
        BookingStatus.REJECTED to 2,
        BookingStatus.CANCELLED to 1
    )

    return bookings
        .groupBy { it.patientId to it.requestType }
        .values
        .mapNotNull { perPatientChannel ->
            perPatientChannel.maxWithOrNull(
                compareBy<BookingResponse> { statusPriority[it.status] ?: 0 }
                    .thenBy { it.createdAt.orEmpty() }
            )
        }
        .sortedWith(
            compareByDescending<BookingResponse> { statusPriority[it.status] ?: 0 }
                .thenByDescending { it.createdAt.orEmpty() }
        )
}
