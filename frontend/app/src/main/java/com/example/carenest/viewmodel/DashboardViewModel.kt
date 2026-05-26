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
                if (response.isSuccessful && response.body() != null) {
                    _dashboardState.value = DashboardState.Success(response.body()!!)
                } else {
                    _dashboardState.value = DashboardState.Error("Không thể tải dữ liệu Dashboard")
                }
            } catch (e: Exception) {
                // For demonstration, mock data if network fails
                _dashboardState.value = DashboardState.Success(getMockData())
                // Uncomment to show actual error
                // _dashboardState.value = DashboardState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun switchFamily(family: Family) {
        viewModelScope.launch {
            dataStoreManager.saveFamilyId(family.id)
            // fetchDashboard() is called automatically because we collect familyIdFlow
        }
    }

    private fun getMockData(): DashboardResponse {
        return DashboardResponse(
            families = listOf(Family("1", "Gia đình Nhỏ"), Family("2", "Nhà Nội")),
            members = listOf(
                com.example.carenest.model.Member("1", "Bố", null),
                com.example.carenest.model.Member("2", "Mẹ", null),
                com.example.carenest.model.Member("3", "Con Gái", null)
            ),
            medications = listOf(
                com.example.carenest.model.Medication("1", "Panadol", "08:00 AM", false),
                com.example.carenest.model.Medication("2", "Vitamin C", "12:00 PM", true)
            ),
            appointments = listOf(
                com.example.carenest.model.Appointment("1", "BS. Nguyễn Văn A", "20/10/2026", "Khám định kỳ")
            )
        )
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
