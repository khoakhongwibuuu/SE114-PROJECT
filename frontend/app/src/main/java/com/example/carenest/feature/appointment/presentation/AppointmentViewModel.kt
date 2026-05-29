package com.example.carenest.feature.appointment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.appointment.data.remote.AppointmentApi
import com.example.carenest.feature.appointment.data.remote.AppointmentResponse
import com.example.carenest.feature.appointment.data.remote.CreateAppointmentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

sealed class AppointmentState {
    object Loading : AppointmentState()
    data class Error(val message: String) : AppointmentState()
    object Empty : AppointmentState()
    data class Success(
        val upcomingAppointments: List<AppointmentItem.Upcoming>,
        val appointmentHistory: List<AppointmentItem.History>
    ) : AppointmentState()
}

sealed class AppointmentItem {
    abstract val id: Long
    abstract val title: String
    abstract val doctorName: String?
    abstract val appointmentDate: String // ISO string for sorting/filtering
    abstract val status: String

    data class Upcoming(
        override val id: Long,
        override val title: String,
        override val doctorName: String?,
        override val appointmentDate: String,
        override val status: String,
        val location: String?,
        val dayOfWeek: String,
        val dayOfMonth: String
    ) : AppointmentItem()

    data class History(
        override val id: Long,
        override val title: String,
        override val doctorName: String?,
        override val appointmentDate: String,
        override val status: String,
        val displayDate: String
    ) : AppointmentItem()
}

class AppointmentViewModel(
    private val api: AppointmentApi,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    private val _appointmentState = MutableStateFlow<AppointmentState>(AppointmentState.Loading)
    val appointmentState: StateFlow<AppointmentState> = _appointmentState.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    fun fetchAppointments(profileId: Long) {
        viewModelScope.launch {
            _appointmentState.value = AppointmentState.Loading
            try {
                val responses = api.getAppointments(profileId)
                if (responses.isEmpty()) {
                    _appointmentState.value = AppointmentState.Empty
                    return@launch
                }

                val now = ZonedDateTime.now()

                val upcoming = mutableListOf<AppointmentItem.Upcoming>()
                val history = mutableListOf<AppointmentItem.History>()

                for (res in responses) {
                    val date = try {
                        ZonedDateTime.parse(res.appointmentDate)
                    } catch (e: Exception) {
                        null
                    }

                    if (date != null && date.isAfter(now) && res.status != "CANCELLED") {
                        upcoming.add(
                            AppointmentItem.Upcoming(
                                id = res.id,
                                title = res.hospitalName.takeIf { !it.isBlank() } ?: "Khám bệnh",
                                doctorName = res.doctorName,
                                appointmentDate = res.appointmentDate,
                                status = res.status,
                                location = res.address,
                                dayOfWeek = mapDayOfWeek(date.dayOfWeek.value),
                                dayOfMonth = date.dayOfMonth.toString().padStart(2, '0')
                            )
                        )
                    } else if (date != null) {
                        history.add(
                            AppointmentItem.History(
                                id = res.id,
                                title = res.hospitalName.takeIf { !it.isBlank() } ?: "Khám bệnh",
                                doctorName = res.doctorName,
                                appointmentDate = res.appointmentDate,
                                status = res.status,
                                displayDate = "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
                            )
                        )
                    }
                }

                // Sort upcoming ASC
                upcoming.sortBy { it.appointmentDate }
                // Sort history DESC
                history.sortByDescending { it.appointmentDate }

                if (upcoming.isEmpty() && history.isEmpty()) {
                    _appointmentState.value = AppointmentState.Empty
                } else {
                    _appointmentState.value = AppointmentState.Success(
                        upcomingAppointments = upcoming,
                        appointmentHistory = history
                    )
                }

            } catch (e: Exception) {
                _appointmentState.value = AppointmentState.Error(e.message ?: "Failed to fetch appointments")
            }
        }
    }

    fun createAppointment(
        profileId: Long,
        hospitalName: String,
        doctorName: String,
        isoDate: String,
        address: String?,
        notes: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                api.createAppointment(
                    CreateAppointmentRequest(
                        healthProfileId = profileId,
                        hospitalName = hospitalName,
                        doctorName = doctorName,
                        appointmentDate = isoDate,
                        address = address,
                        notes = notes
                    )
                )
                onSuccess()
                fetchAppointments(profileId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create appointment")
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun cancelAppointment(appointmentId: Long, profileId: Long) {
        viewModelScope.launch {
            try {
                api.cancelAppointment(appointmentId)
                fetchAppointments(profileId)
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    private fun mapDayOfWeek(dayValue: Int): String {
        return when(dayValue) {
            1 -> "T2"
            2 -> "T3"
            3 -> "T4"
            4 -> "T5"
            5 -> "T6"
            6 -> "T7"
            7 -> "CN"
            else -> "--"
        }
    }
}

class AppointmentViewModelFactory(
    private val api: AppointmentApi,
    private val sessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(api, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
