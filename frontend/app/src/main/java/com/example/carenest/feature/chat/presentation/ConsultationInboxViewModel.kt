package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.booking.data.repository.BookingRepository
import com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConsultationInboxUiState(
    val threads: List<ConsultationThreadInboxResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ConsultationInboxViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultationInboxUiState())
    val uiState: StateFlow<ConsultationInboxUiState> = _uiState.asStateFlow()

    fun loadInbox() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.getConsultationInbox()
            result.onSuccess { threads ->
                _uiState.update { it.copy(isLoading = false, threads = threads, error = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Không thể tải danh sách tư vấn") }
            }
        }
    }

    fun refresh() = loadInbox()

    class Factory(private val repository: BookingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConsultationInboxViewModel(repository) as T
        }
    }
}
