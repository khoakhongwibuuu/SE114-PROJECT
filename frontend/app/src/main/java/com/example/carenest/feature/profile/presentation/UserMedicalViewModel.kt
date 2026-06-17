package com.example.carenest.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.feature.health.data.remote.GrowthApi
import com.example.carenest.feature.health.domain.model.GrowthChartPointResponse
import com.example.carenest.feature.health.domain.model.GrowthRecordCreateRequest
import com.example.carenest.feature.health.domain.model.GrowthRecordResponse
import com.example.carenest.feature.profile.domain.port.MedicalProfileDataSource
import com.example.carenest.model.HealthProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UserMedicalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isGrowthLoading: Boolean = false,
    val isGrowthSaving: Boolean = false,
    val profileData: HealthProfile? = null,
    val growthRecords: List<GrowthRecordResponse> = emptyList(),
    val growthChart: List<GrowthChartPointResponse> = emptyList(),
    val growthRecordDate: String = LocalDate.now().toString(),
    val growthWeight: String = "",
    val growthHeight: String = "",
    val growthHeadCircumference: String = "",
    val growthNotes: String = "",
    val growthError: String? = null,
    val fullName: String = "",
    val relationship: String = "",
    val birthday: String? = null,
    val gender: String? = null,
    val bloodType: String = "UNKNOWN",
    val allergies: String = "",
    val height: String = "",
    val weight: String = "",
    val chronicDiseases: String = "",
    val emergencyName: String = "",
    val emergencyPhone: String = "",
    val error: String? = null,
    val successMessage: String? = null,
)

class UserMedicalViewModel(
    private val repository: MedicalProfileDataSource,
    private val growthApi: GrowthApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserMedicalUiState())
    val uiState: StateFlow<UserMedicalUiState> = _uiState.asStateFlow()

    fun loadProfile(profileId: Long, clearSuccessMessage: Boolean = true) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    successMessage = if (clearSuccessMessage) null else it.successMessage,
                )
            }
            val result = repository.getFamilyProfile(profileId)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileData = profile,
                        fullName = profile.name,
                        relationship = profile.role,
                        birthday = profile.dateOfBirth,
                        gender = profile.gender,
                        bloodType = profile.bloodType ?: "UNKNOWN",
                        allergies = profile.allergies.joinToString(", "),
                        height = profile.height?.toString() ?: "",
                        weight = profile.weight?.toString() ?: "",
                        chronicDiseases = profile.medicalHistory.joinToString("; ") { cond -> cond.name },
                        emergencyName = profile.emergencyContact?.name ?: "",
                        emergencyPhone = profile.emergencyContact?.phone ?: "",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.userMessage("Không thể tải hồ sơ sức khỏe"),
                    )
                }
            }
        }
    }

    fun loadGrowthData(profileId: Long, clearSuccessMessage: Boolean = true) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGrowthLoading = true,
                    growthError = null,
                    successMessage = if (clearSuccessMessage) null else it.successMessage,
                )
            }
            try {
                val recordsResponse = growthApi.getGrowthRecords(profileId)
                val chartResponse = growthApi.getGrowthChart(profileId)

                if (recordsResponse.isSuccessful && chartResponse.isSuccessful) {
                    val records = recordsResponse.requireList("Không thể tải dữ liệu tăng trưởng")
                    val chart = chartResponse.requireList("Không thể tải biểu đồ tăng trưởng")
                    _uiState.update {
                        it.copy(
                            isGrowthLoading = false,
                            growthRecords = records,
                            growthChart = chart,
                            growthError = null,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isGrowthLoading = false,
                            growthError = recordsResponse.errorMessage("Không thể tải dữ liệu tăng trưởng"),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGrowthLoading = false,
                        growthError = e.userMessage("Không thể tải dữ liệu tăng trưởng"),
                    )
                }
            }
        }
    }

    fun onBloodTypeChange(value: String) {
        _uiState.update { it.copy(bloodType = value) }
    }

    fun onAllergiesChange(value: String) {
        _uiState.update { it.copy(allergies = value) }
    }

    fun onHeightChange(value: String) {
        _uiState.update { it.copy(height = value) }
    }

    fun onWeightChange(value: String) {
        _uiState.update { it.copy(weight = value) }
    }

    fun onChronicDiseasesChange(value: String) {
        _uiState.update { it.copy(chronicDiseases = value) }
    }

    fun onEmergencyNameChange(value: String) {
        _uiState.update { it.copy(emergencyName = value) }
    }

    fun onEmergencyPhoneChange(value: String) {
        _uiState.update { it.copy(emergencyPhone = value) }
    }

    fun onGrowthRecordDateChange(value: String) {
        _uiState.update { it.copy(growthRecordDate = value, growthError = null) }
    }

    fun onGrowthWeightChange(value: String) {
        _uiState.update { it.copy(growthWeight = value.filterNumericInput(), growthError = null) }
    }

    fun onGrowthHeightChange(value: String) {
        _uiState.update { it.copy(growthHeight = value.filterNumericInput(), growthError = null) }
    }

    fun onGrowthHeadCircumferenceChange(value: String) {
        _uiState.update { it.copy(growthHeadCircumference = value.filterNumericInput(), growthError = null) }
    }

    fun onGrowthNotesChange(value: String) {
        _uiState.update { it.copy(growthNotes = value, growthError = null) }
    }

    fun saveGrowthRecord(profileId: Long) {
        val state = _uiState.value
        val recordDate = runCatching { LocalDate.parse(state.growthRecordDate) }.getOrNull()
        val weight = state.growthWeight.toDoubleOrNull()
        val height = state.growthHeight.toDoubleOrNull()
        val headCircumference = state.growthHeadCircumference.toDoubleOrNull()

        val validationError = when {
            recordDate == null -> "Vui lòng chọn ngày ghi nhận"
            recordDate.isAfter(LocalDate.now()) -> "Ngày ghi nhận không được ở tương lai"
            weight == null -> "Vui lòng nhập cân nặng"
            weight < 1.0 || weight > 150.0 -> "Cân nặng phải trong khoảng 1 - 150 kg"
            height == null -> "Vui lòng nhập chiều cao"
            height < 30.0 || height > 250.0 -> "Chiều cao phải trong khoảng 30 - 250 cm"
            headCircumference != null && (headCircumference < 20.0 || headCircumference > 70.0) ->
                "Vòng đầu phải trong khoảng 20 - 70 cm"
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(growthError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGrowthSaving = true, growthError = null, successMessage = null) }
            try {
                val response = growthApi.addGrowthRecord(
                    profileId,
                    GrowthRecordCreateRequest(
                        recordDate = state.growthRecordDate,
                        weightKg = weight!!,
                        heightCm = height!!,
                        headCircumferenceCm = headCircumference,
                        notes = state.growthNotes.trim().ifBlank { null },
                    ),
                )
                if (response.isSuccessful) {
                    response.requireData("Không thể lưu chỉ số tăng trưởng")
                    _uiState.update {
                        it.copy(
                            isGrowthSaving = false,
                            successMessage = "Đã lưu chỉ số tăng trưởng",
                            growthWeight = "",
                            growthHeight = "",
                            growthHeadCircumference = "",
                            growthNotes = "",
                            growthRecordDate = LocalDate.now().toString(),
                            growthError = null,
                        )
                    }
                    loadGrowthData(profileId, clearSuccessMessage = false)
                    loadProfile(profileId, clearSuccessMessage = false)
                } else {
                    _uiState.update {
                        it.copy(
                            isGrowthSaving = false,
                            growthError = response.errorMessage("Không thể lưu chỉ số tăng trưởng"),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGrowthSaving = false,
                        growthError = e.userMessage("Không thể lưu chỉ số tăng trưởng"),
                    )
                }
            }
        }
    }

    fun saveMedicalProfile(profileId: Long) {
        val state = _uiState.value
        val heightD = state.height.toDoubleOrNull()
        val weightD = state.weight.toDoubleOrNull()
        val fullName = state.fullName.trim()
        val birthday = state.birthday?.trim().orEmpty()
        val gender = state.gender?.trim().orEmpty()
        val relationship = state.relationship.trim()

        if (fullName.isEmpty() || birthday.isEmpty() || gender.isEmpty()) {
            _uiState.update {
                it.copy(error = "Vui lòng nhập đầy đủ họ tên, ngày sinh và giới tính")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            val result = repository.updateProfile(
                profileId = profileId,
                fullName = fullName,
                birthday = birthday,
                gender = gender,
                relationship = relationship,
                height = heightD,
                weight = weightD,
                bloodType = state.bloodType,
                allergy = state.allergies,
                medicalHistory = state.chronicDiseases,
            )

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Cập nhật hồ sơ sức khỏe thành công.",
                    )
                }
                loadProfile(profileId, clearSuccessMessage = false)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.userMessage("Cập nhật hồ sơ sức khỏe thất bại"),
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

class UserMedicalViewModelFactory(
    private val repository: MedicalProfileDataSource,
    private val growthApi: GrowthApi,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserMedicalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserMedicalViewModel(repository, growthApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun String.filterNumericInput(): String {
    var hasDot = false
    return filter { char ->
        when {
            char.isDigit() -> true
            char == '.' && !hasDot -> {
                hasDot = true
                true
            }
            else -> false
        }
    }
}
