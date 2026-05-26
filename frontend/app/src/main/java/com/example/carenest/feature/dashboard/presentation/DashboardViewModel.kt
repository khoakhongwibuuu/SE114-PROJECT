package com.example.carenest.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.dashboard.domain.model.DashboardResponse
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardResponse) : DashboardState()
    data class Error(val error: String) : DashboardState()
}

class DashboardViewModel(
    private val dashboardApi: DashboardApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _currentFamilyId = MutableStateFlow<String?>(null)
    val currentFamilyId: StateFlow<String?> = _currentFamilyId.asStateFlow()

    init {
        viewModelScope.launch {
            secureSessionManager.familyIdFlow.collect { id ->
                _currentFamilyId.value = id
                fetchDashboard()
            }
        }
    }

    fun fetchDashboard() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val response = dashboardApi.getDashboard()
                val dashboard = response.body()?.data
                _dashboardState.value = if (response.isSuccessful && dashboard != null) {
                    DashboardState.Success(dashboard)
                } else {
                    DashboardState.Error(response.body()?.message ?: "KhÃ´ng thá»ƒ táº£i dá»¯ liá»‡u Dashboard")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.localizedMessage ?: "Lá»—i káº¿t ná»‘i")
            }
        }
    }

    fun switchFamily(family: Family) {
        viewModelScope.launch {
            secureSessionManager.saveFamilyId(family.id)
        }
    }
}

class DashboardViewModelFactory(
    private val dashboardApi: DashboardApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(dashboardApi, secureSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
