package com.example.carenest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.data.DataStoreManager
import com.example.carenest.model.DashboardResponse
import com.example.carenest.model.Family
import com.example.carenest.network.DashboardApi
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
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _currentFamilyId = MutableStateFlow<String?>(null)
    val currentFamilyId: StateFlow<String?> = _currentFamilyId.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.familyIdFlow.collect { id ->
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
                    DashboardState.Error(response.body()?.message ?: "Không thể tải dữ liệu Dashboard")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun switchFamily(family: Family) {
        viewModelScope.launch {
            dataStoreManager.saveFamilyId(family.id)
        }
    }
}

class DashboardViewModelFactory(
    private val dashboardApi: DashboardApi,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(dashboardApi, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
