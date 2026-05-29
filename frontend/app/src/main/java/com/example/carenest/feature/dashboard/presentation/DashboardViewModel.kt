package com.example.carenest.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.auth.domain.model.UserInfo
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.feature.dashboard.domain.model.DashboardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.carenest.feature.dashboard.domain.model.DashboardTask

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val data: DashboardResponse,
        val tasks: List<DashboardTask>,
        val unreadCount: Int,
        val aiSummaryText: String
    ) : DashboardState()
    data class Error(val error: String) : DashboardState()
}

class DashboardViewModel(
    private val dashboardApi: DashboardApi,
    private val authApi: AuthApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _currentFamilyId = MutableStateFlow<String?>(null)
    val currentFamilyId: StateFlow<String?> = _currentFamilyId.asStateFlow()
    
    private val _selectedMemberId = MutableStateFlow<String?>(null)
    val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            secureSessionManager.familyIdFlow.collect { id ->
                _currentFamilyId.value = id
                fetchDashboard()
            }
        }
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authApi.getMe()
                if (response.isSuccessful) {
                    _currentUser.value = response.body()?.data
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun fetchDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            _dashboardState.value = DashboardState.Loading
            try {
                val familyId = _currentFamilyId.value
                val response = dashboardApi.getDashboard(familyId)
                val dashboard = response.body()?.data
                if (response.isSuccessful && dashboard != null) {
                    processDashboardResponse(dashboard)
                } else {
                    _dashboardState.value = DashboardState.Error(response.body()?.message ?: "Phiên đăng nhập hết hạn hoặc lỗi mạng, vui lòng đăng nhập lại.")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    private fun processDashboardResponse(dashboard: DashboardResponse) {
        val tasks = mutableListOf<DashboardTask>()
        
        dashboard.medications.forEach { med ->
            tasks.add(
                DashboardTask(
                    id = "med_${med.id}",
                    type = "MEDICATION",
                    title = med.name,
                    subtitle = med.time,
                    icon = "pill",
                    iconBgColor = 0xFFEFF6FF,
                    iconColor = 0xFF2563EB,
                    badge = if (med.isTaken) "ĐÃ UỐNG" else "Hôm nay"
                )
            )
        }
        
        dashboard.appointments.forEach { appt ->
            tasks.add(
                DashboardTask(
                    id = "appt_${appt.id}",
                    type = "APPOINTMENT",
                    title = appt.doctorName,
                    subtitle = "${appt.date} - ${appt.note ?: "Khám bệnh"}",
                    icon = "calendar_month",
                    iconBgColor = 0xFFF0FDF4,
                    iconColor = 0xFF16A34A,
                    badge = "Ngày mai"
                )
            )
        }
        
        dashboard.vaccines.forEach { vac ->
            tasks.add(
                DashboardTask(
                    id = "vac_${vac.id}",
                    type = "VACCINATION",
                    title = vac.name,
                    subtitle = vac.date,
                    icon = "syringe",
                    iconBgColor = 0xFFFFF7ED,
                    iconColor = 0xFFEA580C,
                    badge = if (vac.isCompleted) "HOÀN THÀNH" else "Ngày kia"
                )
            )
        }
        
        val aiText = if (tasks.isEmpty()) {
            "Hôm nay chưa có cảnh báo lớn. Bạn có thể kiểm tra lịch thuốc, lịch khám và hỏi CareNest AI nếu cần tra cứu nhanh."
        } else {
            "Hôm nay cả nhà có ${tasks.size} việc cần chú ý thực hiện. Hãy lưu ý chuẩn bị đầy đủ nhé!"
        }

        _dashboardState.value = DashboardState.Success(
            data = dashboard,
            tasks = tasks,
            unreadCount = tasks.size, // Mocking unread to task size
            aiSummaryText = aiText
        )
    }

    fun selectMember(memberId: String?) {
        _selectedMemberId.value = memberId
    }

    fun switchFamily(family: Family) {
        viewModelScope.launch {
            _selectedMemberId.value = null
            secureSessionManager.saveFamilyId(family.id)
        }
    }
}

class DashboardViewModelFactory(
    private val dashboardApi: DashboardApi,
    private val authApi: AuthApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(dashboardApi, authApi, secureSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

