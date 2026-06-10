package com.example.carenest.feature.booking.presentation.doctorworkspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorWorkspaceUiState(
    val bookings: List<BookingResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DoctorWorkspaceViewModel(
    private val repository: BookingRepository
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
                // Deduplicate: for each patient, keep only the most actionable booking.
                // Priority: ACTIVE > APPROVED > PENDING > COMPLETED > REJECTED > CANCELLED
                val statusPriority = mapOf(
                    com.example.carenest.feature.booking.domain.model.BookingStatus.ACTIVE to 6,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.APPROVED to 5,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.PENDING to 4,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.COMPLETED to 3,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.REJECTED to 2,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.CANCELLED to 1
                )
                val deduped = bookings
                    .groupBy { it.patientId }
                    .values
                    .map { perPatient ->
                        perPatient.maxByOrNull { statusPriority[it.status] ?: 0 }!!
                    }
                    .sortedByDescending { statusPriority[it.status] ?: 0 }
                _uiState.update { it.copy(isLoading = false, bookings = deduped) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveBooking(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.approveBooking(id)
            if (result.isSuccess) {
                // Update local list
                val updatedBookings = _uiState.value.bookings.map {
                    if (it.id == id) result.getOrNull()!! else it
                }
                _uiState.update { it.copy(bookings = updatedBookings) }
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Có lỗi xảy ra")
            }
        }
    }

    fun rejectBooking(id: Long, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.rejectBooking(id, reason)
                // Update local list
                val updatedBookings = _uiState.value.bookings.map {
                    if (it.id == id) result else it
                }
                _uiState.update { it.copy(bookings = updatedBookings) }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Có lỗi xảy ra")
            }
        }
    }

    class Factory(private val repository: BookingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DoctorWorkspaceViewModel(repository) as T
        }
    }
}
