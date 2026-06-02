package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.medical.domain.model.VaccinationItem
import com.example.carenest.feature.medical.domain.model.VaccinationTrackerGroup
import kotlinx.coroutines.delay
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Simulate network delay
            delay(500)
            
            val mockGroups = listOf(
                VaccinationTrackerGroup(
                    stageLabel = "Sơ sinh",
                    vaccinations = listOf(
                        VaccinationItem(
                            id = 1,
                            vaccineName = "Lao (BCG)",
                            doseNumber = 1,
                            status = "DONE",
                            dateGiven = "10/05/2026",
                            clinicName = "Bệnh viện Phụ sản",
                            notes = "Bé bình thường sau tiêm"
                        ),
                        VaccinationItem(
                            id = 2,
                            vaccineName = "Viêm gan B",
                            doseNumber = 1,
                            status = "DONE",
                            dateGiven = "10/05/2026",
                            clinicName = "Bệnh viện Phụ sản"
                        )
                    )
                ),
                VaccinationTrackerGroup(
                    stageLabel = "2 tháng tuổi",
                    vaccinations = listOf(
                        VaccinationItem(
                            id = 3,
                            vaccineName = "Vắc xin 6 trong 1 Hexaxim",
                            doseNumber = 1,
                            status = "PENDING",
                            plannedDate = "10/07/2026",
                            clinicName = "Trung tâm Tiêm chủng VNVC"
                        ),
                        VaccinationItem(
                            id = 4,
                            vaccineName = "Phế cầu (Synflorix)",
                            doseNumber = 1,
                            status = "PENDING",
                            plannedDate = "10/07/2026",
                            clinicName = "Trung tâm Tiêm chủng VNVC"
                        )
                    )
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    groups = mockGroups,
                    currentMemberId = memberId ?: 1L
                )
            }
        }
    }
}
