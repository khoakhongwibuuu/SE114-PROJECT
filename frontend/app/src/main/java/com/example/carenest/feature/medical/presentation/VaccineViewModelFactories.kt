package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.feature.medical.data.repository.VaccineRepository

class VaccineScheduleViewModelFactory(
    private val vaccineRepository: VaccineRepository,
    private val dashboardApi: DashboardApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VaccineScheduleViewModel(vaccineRepository, dashboardApi) as T
    }
}

class AddVaccineViewModelFactory(
    private val vaccineRepository: VaccineRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddVaccineViewModel(vaccineRepository) as T
    }
}
