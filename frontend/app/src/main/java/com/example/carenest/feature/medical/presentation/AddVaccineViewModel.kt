package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.medical.domain.model.CreateVaccinationRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddVaccineUiState(
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AddVaccineViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddVaccineUiState())
    val uiState: StateFlow<AddVaccineUiState> = _uiState.asStateFlow()

    fun saveVaccination(profileId: Long, request: CreateVaccinationRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            // Simulate network request
            delay(1000)
            
            // Success
            _uiState.update { it.copy(isSaving = false, isSuccess = true) }
        }
    }
    
    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
