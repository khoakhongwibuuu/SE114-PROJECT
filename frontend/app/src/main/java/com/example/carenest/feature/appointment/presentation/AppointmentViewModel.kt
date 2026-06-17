package com.example.carenest.feature.appointment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.appointment.data.remote.AppointmentApi
import com.example.carenest.feature.appointment.data.remote.CreateAppointmentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

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
    abstract val appointmentDate: String
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
        viewModelScope.launch(Dispatchers.IO) {
            if (profileId <= 0L) {
                _appointmentState.value = AppointmentState.Error("Chưa chọn hồ sơ sức khỏe hợp lệ")
                return@launch
            }

            _appointmentState.value = AppointmentState.Loading
            try {
                val response = api.getAppointments(profileId)
                val responses = if (response.isSuccessful) {
                    response.requireList("Không thể tải lịch hẹn")
                } else {
                    throw IllegalStateException(response.errorMessage("Không thể tải lịch hẹn"))
                }
                if (responses.isEmpty()) {
                    _appointmentState.value = AppointmentState.Empty
                    return@launch
                }

                val now = ZonedDateTime.now()
                val upcoming = mutableListOf<AppointmentItem.Upcoming>()
                val history = mutableListOf<AppointmentItem.History>()

                for (res in responses) {
                    val date = parseAppointmentDate(res.appointmentDate)
                    val status = res.status.uppercase()
                    val title = res.hospitalName?.takeIf { it.isNotBlank() } ?: "Khám bệnh"

                    if (date != null && date.isAfter(now) && status == "SCHEDULED") {
                        upcoming.add(
                            AppointmentItem.Upcoming(
                                id = res.id,
                                title = title,
                                doctorName = res.doctorName,
                                appointmentDate = res.appointmentDate,
                                status = status,
                                location = res.address,
                                dayOfWeek = mapDayOfWeek(date.dayOfWeek.value),
                                dayOfMonth = date.dayOfMonth.toString().padStart(2, '0')
                            )
                        )
                    } else if (date != null) {
                        history.add(
                            AppointmentItem.History(
                                id = res.id,
                                title = title,
                                doctorName = res.doctorName,
                                appointmentDate = res.appointmentDate,
                                status = status,
                                displayDate = "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
                            )
                        )
                    }
                }

                upcoming.sortBy { it.appointmentDate }
                history.sortByDescending { it.appointmentDate }

                _appointmentState.value = if (upcoming.isEmpty() && history.isEmpty()) {
                    AppointmentState.Empty
                } else {
                    AppointmentState.Success(
                        upcomingAppointments = upcoming,
                        appointmentHistory = history
                    )
                }
            } catch (e: Exception) {
                _appointmentState.value = AppointmentState.Error(e.userMessage("Không thể tải lịch hẹn"))
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
        viewModelScope.launch(Dispatchers.IO) {
            if (profileId <= 0L) {
                onError("Chưa chọn hồ sơ sức khỏe hợp lệ")
                return@launch
            }

            _isActionLoading.value = true
            try {
                val response = api.createAppointment(
                    CreateAppointmentRequest(
                        healthProfileId = profileId,
                        hospitalName = hospitalName,
                        doctorName = doctorName,
                        appointmentDate = isoDate,
                        address = address,
                        notes = notes
                    )
                )
                response.requireData("Không thể tạo lịch hẹn")
                onSuccess()
                fetchAppointments(profileId)
            } catch (e: Exception) {
                onError(e.userMessage("Không thể tạo lịch hẹn"))
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun cancelAppointment(appointmentId: Long, profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (profileId <= 0L) {
                _appointmentState.value = AppointmentState.Error("Chưa chọn hồ sơ sức khỏe hợp lệ")
                return@launch
            }

            try {
                val response = api.cancelAppointment(appointmentId)
                response.requireData("Không thể hủy lịch hẹn")
                if (!response.isSuccessful) {
                    _appointmentState.value = AppointmentState.Error(
                        response.errorMessage("Không thể hủy lịch hẹn")
                    )
                    return@launch
                }
                fetchAppointments(profileId)
            } catch (e: Exception) {
                _appointmentState.value = AppointmentState.Error(e.userMessage("Không thể hủy lịch hẹn"))
            }
        }
    }

    private fun parseAppointmentDate(value: String): ZonedDateTime? {
        return runCatching { ZonedDateTime.parse(value) }.getOrNull()
            ?: runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()) }.getOrNull()
    }

    private fun mapDayOfWeek(dayValue: Int): String {
        return when (dayValue) {
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
