package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.feature.medical.data.repository.AppointmentRepository

class AppointmentScheduleViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val dashboardApi: DashboardApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppointmentScheduleViewModel(appointmentRepository, dashboardApi) as T
    }
}

class AddAppointmentViewModelFactory(
    private val dashboardApi: DashboardApi,
    private val appointmentRepository: AppointmentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddAppointmentViewModel(dashboardApi, appointmentRepository) as T
    }
}
