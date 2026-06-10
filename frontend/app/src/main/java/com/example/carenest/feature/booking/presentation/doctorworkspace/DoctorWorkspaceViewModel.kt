package com.example.carenest.feature.booking.presentation.doctorworkspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.repository.BookingRepository
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
            val result = repository.getDoctorBookings()
            result.onSuccess { bookings ->
                // Deduplicate: for each patient, keep only the most actionable booking.
                // Priority: ACTIVE > APPROVED > PENDING > COMPLETED > REJECTED
                val statusPriority = mapOf(
                    com.example.carenest.feature.booking.domain.model.BookingStatus.ACTIVE to 5,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.APPROVED to 4,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.PENDING to 3,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.COMPLETED to 2,
                    com.example.carenest.feature.booking.domain.model.BookingStatus.REJECTED to 1
                )
                val deduped = bookings
                    .groupBy { it.patientId }
                    .values
                    .map { perPatient ->
                        perPatient.maxByOrNull { statusPriority[it.status] ?: 0 }!!
                    }
                    .sortedByDescending { statusPriority[it.status] ?: 0 }
                _uiState.update { it.copy(isLoading = false, bookings = deduped) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
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
            val result = repository.rejectBooking(id, reason)
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

    class Factory(private val repository: BookingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DoctorWorkspaceViewModel(repository) as T
        }
    }
}
