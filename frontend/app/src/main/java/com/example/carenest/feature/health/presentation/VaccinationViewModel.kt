package com.example.carenest.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.health.data.remote.VaccinationApi
import com.example.carenest.feature.health.domain.model.AdministerDoseRequest
import com.example.carenest.feature.health.domain.model.CreateVaccinationRequest
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
                if (response.isSuccessful && response.body()?.data != null) {
                    val records = response.body()!!.data!!
                    
                    val groups = records.map { record ->
                        VaccinationTrackerGroup(
                            stageLabel = record.vaccineName,
                            description = "Tổng số ${record.totalDoses} mũi",
                            vaccinations = record.doses.map { dose ->
                                VaccinationDoseUiModel(
                                    doseId = dose.id,
                                    recordId = record.id,
                                    doseNumber = dose.doseNumber,
                                    dateGiven = dose.dateAdministered,
                                    plannedDate = dose.scheduledDate,
                                    clinicName = dose.location,
                                    status = dose.status
                                )
                            }
                        )
                    }

                    _uiState.update { it.copy(isLoading = false, vaccinationGroups = groups) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Lỗi khi tải dữ liệu") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Lỗi kết nối mạng") }
            }
        }
    }

    fun createVaccinationPlan(profileId: Long, request: CreateVaccinationRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, submitSuccess = false) }
            try {
                val response = vaccinationApi.createVaccinationPlan(profileId, request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, error = "Lỗi khi tạo lịch tiêm") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage ?: "Lỗi kết nối mạng") }
            }
        }
    }

    fun administerDose(doseId: Long, request: AdministerDoseRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, submitSuccess = false) }
            try {
                val response = vaccinationApi.administerDose(doseId, request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, error = "Lỗi khi cập nhật mũi tiêm") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage ?: "Lỗi kết nối mạng") }
            }
        }
    }

    fun resetSubmitState() {
        _uiState.update { it.copy(submitSuccess = false, error = null) }
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
