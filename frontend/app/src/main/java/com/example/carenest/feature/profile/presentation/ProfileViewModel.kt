package com.example.carenest.feature.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val fullName: String = "Nguyễn Văn A",
    val email: String = "nguyenvana@gmail.com",
    val phone: String = "0901234567",
    val emergencyPhone: String = "",
    val birthday: String = "01/01/1990",
    val age: String = "34",
    val gender: String = "MALE",
    val bloodType: String = "O_POSITIVE",
    val avatarUri: Uri? = null,
    val medReminder: Boolean = true,
    val apptReminder: Boolean = true,
    val role: String = "USER",
    val memberRole: String = "Chủ gia đình",
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.EditClicked -> {
                _state.update { it.copy(isEditing = true) }
            }
            is ProfileEvent.SaveClicked -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSaving = true) }
                    delay(1000) // Mock save
                    _state.update { 
                        it.copy(
                            isSaving = false, 
                            isEditing = false,
                            successMessage = "Thông tin của bạn đã được cập nhật."
                        ) 
                    }
                }
            }
            is ProfileEvent.FullNameChanged -> _state.update { it.copy(fullName = event.name) }
            is ProfileEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is ProfileEvent.PhoneChanged -> _state.update { it.copy(phone = event.phone) }
            is ProfileEvent.EmergencyPhoneChanged -> _state.update { it.copy(emergencyPhone = event.phone) }
            is ProfileEvent.BirthdayChanged -> {
                // Calculate age basic mock
                val year = event.birthday.takeLast(4).toIntOrNull() ?: 1990
                val age = (2026 - year).toString()
                _state.update { it.copy(birthday = event.birthday, age = age) }
            }
            is ProfileEvent.GenderChanged -> _state.update { it.copy(gender = event.gender) }
            is ProfileEvent.BloodTypeChanged -> _state.update { it.copy(bloodType = event.bloodType) }
            is ProfileEvent.MedReminderChanged -> _state.update { it.copy(medReminder = event.enabled) }
            is ProfileEvent.ApptReminderChanged -> _state.update { it.copy(apptReminder = event.enabled) }
            is ProfileEvent.AvatarSelected -> {
                viewModelScope.launch {
                    _state.update { it.copy(isUploadingAvatar = true) }
                    delay(1500) // Mock upload
                    _state.update { 
                        it.copy(
                            isUploadingAvatar = false, 
                            avatarUri = event.uri,
                            successMessage = "Ảnh đại diện đã được cập nhật."
                        ) 
                    }
                }
            }
            is ProfileEvent.ClearMessage -> _state.update { it.copy(error = null, successMessage = null) }
        }
    }
}

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
