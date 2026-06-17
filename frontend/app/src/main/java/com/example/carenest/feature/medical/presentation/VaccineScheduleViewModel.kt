package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.medical.domain.model.VaccinationTrackerGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaccineScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val groups: List<VaccinationTrackerGroup> = emptyList(),
    val currentMemberId: Long? = null
)

class VaccineScheduleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VaccineScheduleUiState())
    val uiState: StateFlow<VaccineScheduleUiState> = _uiState.asStateFlow()

    fun loadData(memberId: Long?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    groups = emptyList(),
                    currentMemberId = memberId
                )
            }
        }
    }
}
