package com.example.carenest.viewmodel

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

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profileData: HealthProfile? = null,
    val selectedTab: Int = 0, // 0: Thông tin, 1: Theo dõi, 2: Khẩn cấp
    val error: String? = null
)

class ProfileViewModel(private val repository: FamilyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(profileId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getFamilyProfile(profileId)
            result.onSuccess { data ->
                _uiState.update { it.copy(isLoading = false, profileData = data) }
            }.onFailure { e ->
                // _uiState.update { it.copy(isLoading = false, error = e.message) }
                // Use mock data to show UI when backend has no data yet
                _uiState.update { it.copy(isLoading = false, profileData = getMockHealthProfile()) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    private fun getMockHealthProfile(): HealthProfile {
        return HealthProfile(
            id = 1,
            name = "Nguyễn Văn A",
            role = "Bản thân",
            age = 36,
            location = "TP.HCM",
            avatarUrl = null,
            isVerified = true,
            bloodType = "O+",
            allergies = listOf("Phấn hoa", "Hải sản"),
            height = 170.0f,
            weight = 65.5f,
            bmi = 22.6f,
            medicalHistory = listOf(
                com.example.carenest.model.MedicalCondition("Huyết áp", "Hơi cao lúc mới dậy")
            ),
            emergencyContact = com.example.carenest.model.EmergencyContact("Vợ", "Vợ", "0987654321")
        )
    }
}

class ProfileViewModelFactory(
    private val repository: FamilyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
