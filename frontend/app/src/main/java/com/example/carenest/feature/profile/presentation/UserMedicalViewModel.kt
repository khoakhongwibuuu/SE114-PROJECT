package com.example.carenest.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.family.data.repository.FamilyRepository
import com.example.carenest.model.HealthProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserMedicalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val profileData: HealthProfile? = null,
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
    val successMessage: String? = null
)

class UserMedicalViewModel(
    private val repository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserMedicalUiState())
    val uiState: StateFlow<UserMedicalUiState> = _uiState.asStateFlow()

    fun loadProfile(profileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = repository.getFamilyProfile(profileId)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileData = profile,
                        fullName = profile.name,
                        relationship = profile.role,
                        birthday = profile.age?.let { "1990-01-01" }, // mock or extract if needed
                        gender = "MALE",
                        bloodType = profile.bloodType ?: "UNKNOWN",
                        allergies = profile.allergies.joinToString(", "),
                        height = profile.height?.toString() ?: "",
                        weight = profile.weight?.toString() ?: "",
                        chronicDiseases = profile.medicalHistory.joinToString("; ") { cond -> cond.name },
                        emergencyName = profile.emergencyContact?.name ?: "",
                        emergencyPhone = profile.emergencyContact?.phone ?: ""
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Không thể tải hồ sơ sức khỏe"
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

    fun saveMedicalProfile(profileId: Long) {
        val state = _uiState.value
        val heightD = state.height.toDoubleOrNull()
        val weightD = state.weight.toDoubleOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            val result = repository.updateProfile(
                profileId = profileId,
                fullName = state.fullName.ifEmpty { "Nguyễn Văn A" },
                birthday = state.birthday ?: "1990-01-01",
                gender = state.gender ?: "MALE",
                relationship = state.relationship.ifEmpty { "Bản thân" },
                height = heightD,
                weight = weightD,
                bloodType = state.bloodType,
                allergy = state.allergies,
                medicalHistory = state.chronicDiseases
            )

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Cập nhật hồ sơ sức khỏe thành công."
                    )
                }
                loadProfile(profileId)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.localizedMessage ?: "Cập nhật thất bại"
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
    private val repository: FamilyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserMedicalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserMedicalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
