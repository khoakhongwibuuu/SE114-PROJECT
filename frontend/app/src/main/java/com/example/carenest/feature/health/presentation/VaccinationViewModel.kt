package com.example.carenest.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.feature.health.data.remote.VaccinationApi
import com.example.carenest.feature.health.domain.model.AdministerDoseRequest
import com.example.carenest.feature.health.domain.model.CreateVaccinationRequest
import com.example.carenest.feature.health.domain.model.VaccinationRecordResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaccinationDoseUiModel(
    val doseId: Long,
    val recordId: Long,
    val doseNumber: Int,
    val dateGiven: String?,
    val plannedDate: String?,
    val clinicName: String?,
    val notes: String?,
    val status: String
)

data class VaccinationTrackerGroup(
    val stageLabel: String,
    val description: String,
    val vaccinations: List<VaccinationDoseUiModel>
)

data class VaccinationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vaccinationGroups: List<VaccinationTrackerGroup> = emptyList(),
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false
)

class VaccinationViewModel(
    private val vaccinationApi: VaccinationApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaccinationUiState())
    val uiState: StateFlow<VaccinationUiState> = _uiState.asStateFlow()

    fun loadVaccinations(profileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = vaccinationApi.getVaccinations(profileId)
                if (response.isSuccessful) {
                    val records = response.requireList("Không thể tải dữ liệu tiêm chủng")
                    _uiState.update {
                        it.copy(isLoading = false, vaccinationGroups = records.toTrackerGroups())
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = response.errorMessage("Không thể tải dữ liệu tiêm chủng"))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.localizedMessage ?: "Lỗi kết nối mạng")
                }
            }
        }
    }

    fun createVaccinationPlan(profileId: Long, request: CreateVaccinationRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, submitSuccess = false) }
            try {
                val response = vaccinationApi.createVaccinationPlan(profileId, request)
                if (response.isSuccessful) {
                    val record = response.requireData("Không thể tạo lịch tiêm")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true,
                            vaccinationGroups = it.vaccinationGroups.upsert(record)
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(isSubmitting = false, error = response.errorMessage("Không thể tạo lịch tiêm"))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = e.localizedMessage ?: "Lỗi kết nối mạng")
                }
            }
        }
    }

    fun administerDose(doseId: Long, request: AdministerDoseRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, submitSuccess = false) }
            try {
                val response = vaccinationApi.administerDose(doseId, request)
                if (response.isSuccessful) {
                    val record = response.requireData("Không thể cập nhật mũi tiêm")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true,
                            vaccinationGroups = it.vaccinationGroups.upsert(record)
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(isSubmitting = false, error = response.errorMessage("Không thể cập nhật mũi tiêm"))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = e.localizedMessage ?: "Lỗi kết nối mạng")
                }
            }
        }
    }

    fun resetSubmitState() {
        _uiState.update { it.copy(submitSuccess = false, error = null) }
    }

    private fun List<VaccinationRecordResponse>.toTrackerGroups(): List<VaccinationTrackerGroup> {
        return map { record -> record.toTrackerGroup() }
    }

    private fun VaccinationRecordResponse.toTrackerGroup(): VaccinationTrackerGroup {
        return VaccinationTrackerGroup(
            stageLabel = vaccineName,
            description = "Tổng số $totalDoses mũi",
            vaccinations = doses.map { dose ->
                VaccinationDoseUiModel(
                    doseId = dose.id,
                    recordId = id,
                    doseNumber = dose.doseNumber,
                    dateGiven = dose.dateAdministered,
                    plannedDate = dose.scheduledDate,
                    clinicName = dose.location,
                    notes = dose.notes,
                    status = dose.status
                )
            }
        )
    }

    private fun List<VaccinationTrackerGroup>.upsert(record: VaccinationRecordResponse): List<VaccinationTrackerGroup> {
        val updated = record.toTrackerGroup()
        val current = toMutableList()
        val index = current.indexOfFirst { group ->
            group.vaccinations.any { it.recordId == record.id }
        }
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(updated)
        }
        return current
    }

}

class VaccinationViewModelFactory(
    private val vaccinationApi: VaccinationApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaccinationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VaccinationViewModel(vaccinationApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
