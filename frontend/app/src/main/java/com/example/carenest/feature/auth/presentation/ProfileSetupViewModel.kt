package com.example.carenest.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.family.data.remote.FamilyApi
import com.example.carenest.feature.family.domain.model.UpdateProfileDetailsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ProfileSetupState {
    data object Idle : ProfileSetupState()
    data object Loading : ProfileSetupState()
    data object Success : ProfileSetupState()
    data class Error(val error: String) : ProfileSetupState()
}

class ProfileSetupViewModel(
    private val familyApi: FamilyApi,
    private val secureSessionManager: SecureSessionManager,
    private val profileId: Long
) : ViewModel() {

    private val _setupState = MutableStateFlow<ProfileSetupState>(ProfileSetupState.Idle)
    val setupState: StateFlow<ProfileSetupState> = _setupState.asStateFlow()

    fun completeProfile(fullName: String, dateOfBirth: String, gender: String) {
        viewModelScope.launch {
            _setupState.value = ProfileSetupState.Loading
            try {
                val request = UpdateProfileDetailsRequest(
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    relationship = "Self",
                    isChild = false,
                    height = null,
                    weight = null
                )

                val response = withContext(Dispatchers.IO) {
                    familyApi.updateProfileDetails(profileId, request)
                }

                if (response.isSuccessful) {
                    val envelope = response.body()
                    runCatching { envelope.requireSuccess("Cập nhật hồ sơ thất bại") }
                        .onSuccess {
                            withContext(Dispatchers.IO) {
                                secureSessionManager.saveIsProfileCompleteSync(true)
                            }
                            _setupState.value = ProfileSetupState.Success
                        }
                        .onFailure { error ->
                            _setupState.value = ProfileSetupState.Error(error.localizedMessage ?: "Cập nhật hồ sơ thất bại")
                        }
                } else {
                    _setupState.value = ProfileSetupState.Error(response.errorMessage("Cập nhật hồ sơ thất bại"))
                }
            } catch (e: Exception) {
                _setupState.value = ProfileSetupState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }
}

class ProfileSetupViewModelFactory(
    private val familyApi: FamilyApi,
    private val secureSessionManager: SecureSessionManager,
    private val profileId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileSetupViewModel::class.java)) {
            return ProfileSetupViewModel(familyApi, secureSessionManager, profileId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
