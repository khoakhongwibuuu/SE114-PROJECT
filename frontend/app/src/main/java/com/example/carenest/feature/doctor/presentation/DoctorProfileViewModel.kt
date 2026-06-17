package com.example.carenest.feature.doctor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.feature.doctor.data.repository.DoctorRepository
import com.example.carenest.feature.doctor.domain.model.DoctorPublicProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DoctorProfileState(
    val isLoading: Boolean = true,
    val profile: DoctorPublicProfile? = null,
    val error: String? = null
)

class DoctorProfileViewModel(
    private val doctorId: Long,
    private val repository: DoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorProfileState())
    val uiState: StateFlow<DoctorProfileState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val profile = repository.getDoctorProfile(doctorId)
                _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.userMessage("Không thể tải hồ sơ bác sĩ")
                )
            }
        }
    }

    class Factory(
        private val doctorId: Long,
        private val repository: DoctorRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DoctorProfileViewModel::class.java)) {
                return DoctorProfileViewModel(doctorId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
