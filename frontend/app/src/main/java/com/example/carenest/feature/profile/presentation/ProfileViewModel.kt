package com.example.carenest.feature.profile.presentation

import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.auth.domain.model.UpdateCurrentUserRequest
import com.example.carenest.feature.auth.domain.model.UserInfo
import com.example.carenest.feature.family.data.repository.FamilyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileState(
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val emergencyPhone: String = "",
    val birthday: String = "",
    val age: String = "",
    val gender: String = "OTHER",
    val bloodType: String = "UNKNOWN",
    val avatarUri: Uri? = null,
    val medReminder: Boolean = true,
    val apptReminder: Boolean = true,
    val role: String = "USER",
    val memberRole: String = "Chủ gia đình",
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val authApi: AuthApi,
    private val sessionManager: SecureSessionManager,
    private val repository: FamilyRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { role ->
                _state.update { current ->
                    current.copy(role = role?.normalizedRole() ?: current.role)
                }
            }
        }
        loadCurrentUser()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.EditClicked -> {
                _state.update { it.copy(isEditing = true) }
            }

            is ProfileEvent.SaveClicked -> {
                viewModelScope.launch {
                    saveCurrentUser()
                }
            }

            is ProfileEvent.FullNameChanged -> _state.update { it.copy(fullName = event.name) }
            is ProfileEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is ProfileEvent.PhoneChanged -> _state.update { it.copy(phone = event.phone) }
            is ProfileEvent.EmergencyPhoneChanged -> _state.update { it.copy(emergencyPhone = event.phone) }
            is ProfileEvent.BirthdayChanged -> {
                val year = event.birthday.takeLast(4).toIntOrNull()
                val age = year?.let { (2026 - it).coerceAtLeast(0).toString() }.orEmpty()
                _state.update { it.copy(birthday = event.birthday, age = age) }
            }

            is ProfileEvent.GenderChanged -> _state.update { it.copy(gender = event.gender) }
            is ProfileEvent.BloodTypeChanged -> _state.update { it.copy(bloodType = event.bloodType) }
            is ProfileEvent.MedReminderChanged -> _state.update { it.copy(medReminder = event.enabled) }
            is ProfileEvent.ApptReminderChanged -> _state.update { it.copy(apptReminder = event.enabled) }
            is ProfileEvent.AvatarSelected -> {
                _state.update {
                    it.copy(
                        isUploadingAvatar = false,
                        avatarUri = event.uri,
                        successMessage = "Ảnh đại diện đã được cập nhật."
                    )
                }
            }

            is ProfileEvent.ClearMessage -> _state.update { it.copy(error = null, successMessage = null) }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { authApi.getMe() }
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    runCatching {
                        response.requireData("Không thể tải thông tin tài khoản")
                    }.onSuccess { user ->
                        applyUserInfo(user)
                    }.onFailure { error ->
                        _state.update {
                            it.copy(error = error.localizedMessage ?: "Không thể tải thông tin tài khoản")
                        }
                    }
                } else {
                    _state.update {
                        it.copy(error = response.errorMessage("Không thể tải thông tin tài khoản"))
                    }
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(error = error.localizedMessage ?: "Không thể tải thông tin tài khoản")
                }
            }
        }
    }

    private suspend fun saveCurrentUser() {
        val current = _state.value
        _state.update { it.copy(isSaving = true, error = null, successMessage = null) }

        runCatching {
            withContext(Dispatchers.IO) {
                authApi.updateCurrentUser(
                    UpdateCurrentUserRequest(
                        fullName = current.fullName.ifBlank { "Người dùng CareNest" },
                        phone = current.phone.ifBlank { null },
                        dateOfBirth = current.birthday.toApiDateOrNull(),
                        gender = current.gender.takeIf { it.isNotBlank() },
                        avatarUrl = current.avatarUri?.toString()
                    )
                )
            }
        }.onSuccess { response ->
            if (response.isSuccessful) {
                runCatching {
                    response.requireData(
                        fallback = "Không thể cập nhật thông tin tài khoản",
                        missingDataMessage = "Không nhận được thông tin tài khoản đã cập nhật"
                    )
                }.onSuccess { user ->
                    applyUserInfo(user)
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            successMessage = "Thông tin của bạn đã được cập nhật."
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = error.localizedMessage ?: "Không thể cập nhật thông tin tài khoản"
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = response.errorMessage("Không thể cập nhật thông tin tài khoản")
                    )
                }
            }
        }.onFailure { error ->
            _state.update {
                it.copy(
                    isSaving = false,
                    error = error.localizedMessage ?: "Không thể cập nhật thông tin tài khoản"
                )
            }
        }
    }

    private fun applyUserInfo(user: UserInfo) {
        sessionManager.saveUserIdSync(user.id)
        sessionManager.saveUserRoleSync(user.role.normalizedRole())
        _state.update {
            it.copy(
                fullName = user.fullName ?: "",
                email = user.email,
                phone = user.phone ?: "",
                birthday = user.dateOfBirth?.toDisplayDate() ?: "",
                age = user.dateOfBirth?.extractAge().orEmpty(),
                gender = user.gender ?: "OTHER",
                avatarUri = user.avatarUrl?.let(Uri::parse),
                role = user.role.normalizedRole()
            )
        }
    }

    private fun String.toApiDateOrNull(): String? {
        val parts = split("/")
        if (parts.size != 3) return null
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2]
        return if (year.length == 4) "$year-$month-$day" else null
    }

    private fun String.toDisplayDate(): String {
        val parts = split("-")
        return if (parts.size == 3) {
            "${parts[2].padStart(2, '0')}/${parts[1].padStart(2, '0')}/${parts[0]}"
        } else {
            this
        }
    }

    private fun String.extractAge(): String {
        val year = substringBefore("-").toIntOrNull() ?: return ""
        return (2026 - year).coerceAtLeast(0).toString()
    }
}

private fun String.normalizedRole(): String = removePrefix("ROLE_").uppercase()

sealed interface ProfileEvent {
    data object EditClicked : ProfileEvent
    data object SaveClicked : ProfileEvent
    data class FullNameChanged(val name: String) : ProfileEvent
    data class EmailChanged(val email: String) : ProfileEvent
    data class PhoneChanged(val phone: String) : ProfileEvent
    data class EmergencyPhoneChanged(val phone: String) : ProfileEvent
    data class BirthdayChanged(val birthday: String) : ProfileEvent
    data class GenderChanged(val gender: String) : ProfileEvent
    data class BloodTypeChanged(val bloodType: String) : ProfileEvent
    data class MedReminderChanged(val enabled: Boolean) : ProfileEvent
    data class ApptReminderChanged(val enabled: Boolean) : ProfileEvent
    data class AvatarSelected(val uri: Uri?) : ProfileEvent
    data object ClearMessage : ProfileEvent
}

class ProfileViewModelFactory(
    private val authApi: AuthApi,
    private val sessionManager: SecureSessionManager,
    private val repository: FamilyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authApi, sessionManager, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
