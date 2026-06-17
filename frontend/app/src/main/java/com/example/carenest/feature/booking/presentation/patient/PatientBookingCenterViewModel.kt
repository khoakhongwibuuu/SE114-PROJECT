package com.example.carenest.feature.booking.presentation.patient

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

data class PatientBookingCenterUiState(
    val bookings: List<BookingResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PatientBookingCenterViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientBookingCenterUiState())
    val uiState: StateFlow<PatientBookingCenterUiState> = _uiState.asStateFlow()

    fun loadBookings() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val bookings = repository.getMyBookings()
                _uiState.update { it.copy(isLoading = false, bookings = bookings) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    class Factory(private val repository: BookingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientBookingCenterViewModel(repository) as T
        }
    }
}
