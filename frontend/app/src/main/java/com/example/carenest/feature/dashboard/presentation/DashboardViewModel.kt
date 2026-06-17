package com.example.carenest.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.auth.domain.model.UserInfo
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.feature.dashboard.domain.model.DashboardResponse
import com.example.carenest.feature.dashboard.domain.model.DashboardTask
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.domain.model.Member
import com.example.carenest.feature.family.data.repository.FamilyRepository
import com.example.carenest.feature.family.domain.model.FamilyDetailResponse
import com.example.carenest.feature.family.domain.model.FamilySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    data object Loading : DashboardState()
    data class Success(
        val data: DashboardResponse,
        val tasks: List<DashboardTask>,
        val unreadCount: Int,
        val aiSummaryText: String,
        val warning: String? = null
    ) : DashboardState()

    data class Error(val error: String) : DashboardState()
}

class DashboardViewModel(
    private val dashboardApi: DashboardApi,
    private val authApi: AuthApi,
    private val familyRepository: FamilyRepository,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _currentFamilyId = MutableStateFlow<String?>(secureSessionManager.getFamilyId())
    val currentFamilyId: StateFlow<String?> = _currentFamilyId.asStateFlow()

    private val _selectedMemberId = MutableStateFlow<String?>(null)
    val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

    private val _currentProfileId = MutableStateFlow<Long?>(secureSessionManager.getActiveProfileId())
    val currentProfileId: StateFlow<Long?> = _currentProfileId.asStateFlow()

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private var memberProfileMap: Map<String, Long?> = emptyMap()

    init {
        fetchCurrentUser()
        observeSessionState()
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            secureSessionManager.familyIdFlow.collect { familyId ->
                _currentFamilyId.value = familyId
                fetchDashboard()
            }
        }
        viewModelScope.launch {
            secureSessionManager.activeProfileIdFlow.collect { profileId ->
                _currentProfileId.value = profileId
                _selectedMemberId.value = memberProfileMap.entries
                    .firstOrNull { it.value == profileId }
                    ?.key
            }
        }
    }

    fun fetchCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { authApi.getMe().requireData("Không thể tải thông tin tài khoản") }
                .getOrNull()
                ?.let { user ->
                    secureSessionManager.saveUserIdSync(user.id)
                    secureSessionManager.saveUserRoleSync(user.role)
                    _currentUser.value = user
                    fetchDashboard()
                }
        }
    }

    fun fetchDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            _dashboardState.value = DashboardState.Loading

            val familiesResult = familyRepository.getMyFamilyList()
            val families = familiesResult.getOrElse { error ->
                _dashboardState.value = DashboardState.Error(error.userMessage("Không thể tải dữ liệu trang chủ."))
                return@launch
            }

            if (families.isEmpty()) {
                memberProfileMap = emptyMap()
                _selectedMemberId.value = null
                _currentProfileId.value = null
                secureSessionManager.saveActiveProfileId(null)
                _dashboardState.value = DashboardState.Success(
                    data = DashboardResponse(),
                    tasks = emptyList(),
                    unreadCount = 0,
                    aiSummaryText = "Bạn chưa có gia đình nào. Hãy tạo hoặc tham gia gia đình để bắt đầu."
                )
                return@launch
            }

            val activeFamily = resolveActiveFamily(families)
            val activeFamilyId = activeFamily.id.toString()
            if (_currentFamilyId.value != activeFamilyId) {
                _currentFamilyId.value = activeFamilyId
                secureSessionManager.saveFamilyId(activeFamilyId)
            }

            val detailResult = familyRepository.getFamilyById(activeFamily.id)
            val familyDetail = detailResult.getOrElse { error ->
                _dashboardState.value = DashboardState.Error(error.userMessage("Không thể tải gia đình đang hoạt động."))
                return@launch
            }

            val mappedFamilies = families.map { summary ->
                Family(
                    id = summary.id.toString(),
                    name = summary.name,
                    role = roleLabel(summary.myRole),
                    memberCount = summary.memberCount
                )
            }
            val mappedMembers = familyDetail.members.map { member ->
                Member(
                    id = (member.profileId ?: member.id).toString(),
                    name = member.fullName,
                    avatarUrl = member.avatarUrl
                )
            }
            memberProfileMap = familyDetail.members.associate { member ->
                (member.profileId ?: member.id).toString() to member.profileId
            }

            val ownProfileId = resolveOwnProfileId(familyDetail)
            ownProfileId?.let { secureSessionManager.saveProfileIdSync(it) }

            val activeProfileId = resolveActiveProfileId(familyDetail, ownProfileId)
            secureSessionManager.saveActiveProfileId(activeProfileId)
            _currentProfileId.value = activeProfileId
            _selectedMemberId.value = memberProfileMap.entries
                .firstOrNull { it.value == activeProfileId }
                ?.key

            val dashboardResult = runCatching {
                dashboardApi.getDashboard(activeFamilyId, activeProfileId)
                    .requireData("Không thể tải dữ liệu trang chủ")
            }
            val backendData = dashboardResult.getOrNull()
            val dashboardWarning = dashboardResult.exceptionOrNull()?.userMessage("Không thể tải dữ liệu tổng quan mới nhất")

            val mergedDashboard = (backendData ?: DashboardResponse()).copy(
                families = mappedFamilies,
                members = mappedMembers
            )

            val tasks = mergedDashboard.todayTasks.ifEmpty {
                buildFallbackTasks(familyDetail, activeProfileId)
            }.map { decorateDashboardTask(it) }

            _dashboardState.value = DashboardState.Success(
                data = mergedDashboard,
                tasks = tasks,
                unreadCount = mergedDashboard.unreadNotifications.toInt(),
                aiSummaryText = buildHealthSummary(tasks),
                warning = dashboardWarning
            )
        }
    }

    fun selectMember(memberId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedMemberId.value = memberId
            val profileId = memberId?.let { memberProfileMap[it] }
            secureSessionManager.saveActiveProfileId(profileId)
            _currentProfileId.value = profileId
            fetchDashboard()
        }
    }

    fun switchFamily(family: Family) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedMemberId.value = null
            _currentProfileId.value = null
            secureSessionManager.saveActiveProfileId(null)
            secureSessionManager.saveFamilyId(family.id)
        }
    }

    private fun resolveActiveFamily(families: List<FamilySummary>): FamilySummary {
        val currentId = _currentFamilyId.value?.toLongOrNull()
        return families.firstOrNull { it.id == currentId } ?: families.first()
    }

    private fun resolveOwnProfileId(familyDetail: FamilyDetailResponse): Long? {
        val currentUserId = _currentUser.value?.id ?: return null
        return familyDetail.members.firstOrNull { it.userId == currentUserId }?.profileId
    }

    private fun resolveActiveProfileId(
        familyDetail: FamilyDetailResponse,
        ownProfileId: Long?
    ): Long? {
        val savedProfileId = secureSessionManager.getActiveProfileId()
        val validProfileIds = familyDetail.members.mapNotNull { it.profileId }
        return when {
            savedProfileId != null && validProfileIds.contains(savedProfileId) -> savedProfileId
            ownProfileId != null -> ownProfileId
            else -> validProfileIds.firstOrNull()
        }
    }

    private fun buildFallbackTasks(
        familyDetail: FamilyDetailResponse,
        activeProfileId: Long?
    ): List<DashboardTask> {
        val targetMember = familyDetail.members.firstOrNull {
            it.profileId == activeProfileId
        }
        val targetName = targetMember?.fullName

        return listOf(
            DashboardTask(
                id = "family_overview",
                type = "FAMILY",
                title = "Gia đình đang hoạt động",
                subtitle = familyDetail.name,
                memberName = targetName,
                badge = if (targetName != null) "Đang theo dõi" else "Cả nhà"
            )
        )
    }

    private fun decorateDashboardTask(task: DashboardTask): DashboardTask {
        val normalizedType = task.type?.uppercase()
        val fallbackSubtitle = listOfNotNull(task.memberName, task.description).joinToString(" • ")
        return when (normalizedType) {
            "MEDICATION" -> task.copy(
                subtitle = task.subtitle.ifBlank { fallbackSubtitle.ifBlank { "Lịch uống thuốc hôm nay" } },
                icon = "pill",
                iconBgColor = 0xFFE0F2FE,
                iconColor = 0xFF0EA5E9,
                badge = task.badge ?: "Thuốc"
            )

            "VACCINATION" -> task.copy(
                subtitle = task.subtitle.ifBlank { fallbackSubtitle.ifBlank { "Lịch tiêm chủng sắp tới" } },
                icon = "syringe",
                iconBgColor = 0xFFF3E8FF,
                iconColor = 0xFF8B5CF6,
                badge = task.badge ?: task.subtitle.ifBlank { "Tiêm" }
            )

            "APPOINTMENT" -> task.copy(
                subtitle = task.subtitle.ifBlank { fallbackSubtitle.ifBlank { "Lịch khám hôm nay" } },
                icon = "calendar_month",
                iconBgColor = 0xFFE0F7FA,
                iconColor = 0xFF0097A7,
                badge = task.badge ?: "Khám"
            )

            else -> task.copy(
                subtitle = task.subtitle.ifBlank { fallbackSubtitle.ifBlank { "Việc cần theo dõi" } },
                icon = task.icon.ifBlank { "check_circle" },
                iconBgColor = if (task.iconBgColor == 0xFFFFFFFF) 0xFFF1F5F9 else task.iconBgColor,
                iconColor = if (task.iconColor == 0xFF000000) 0xFF64748B else task.iconColor
            )
        }
    }

    private fun buildHealthSummary(tasks: List<DashboardTask>): String {
        return if (tasks.isEmpty()) {
            "Hôm nay chưa có việc sức khỏe cần xử lý. Bạn vẫn nên kiểm tra lịch thuốc, lịch khám và tiêm chủng định kỳ."
        } else {
            val medicationCount = tasks.count { it.type?.uppercase() == "MEDICATION" }
            val vaccineCount = tasks.count { it.type?.uppercase() == "VACCINATION" }
            val appointmentCount = tasks.count { it.type?.uppercase() == "APPOINTMENT" }
            val parts = buildList {
                if (medicationCount > 0) add("$medicationCount lịch thuốc")
                if (vaccineCount > 0) add("$vaccineCount lịch tiêm")
                if (appointmentCount > 0) add("$appointmentCount lịch khám")
            }
            if (parts.isEmpty()) {
                "Hôm nay gia đình có ${tasks.size} việc cần theo dõi. Hãy xử lý từng việc để không bỏ sót."
            } else {
                "Hôm nay gia đình có ${parts.joinToString(", ")} cần theo dõi. Hãy xử lý từng việc để không bỏ sót."
            }
        }
    }

    private fun roleLabel(role: String): String {
        return when (role.uppercase()) {
            "OWNER" -> "Chủ hộ"
            "FATHER" -> "Bố"
            "MOTHER" -> "Mẹ"
            "OLDER_BROTHER" -> "Anh"
            "OLDER_SISTER" -> "Chị"
            "YOUNGER" -> "Em"
            else -> "Thành viên"
        }
    }
}

class DashboardViewModelFactory(
    private val dashboardApi: DashboardApi,
    private val authApi: AuthApi,
    private val familyRepository: FamilyRepository,
    private val secureSessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                dashboardApi = dashboardApi,
                authApi = authApi,
                familyRepository = familyRepository,
                secureSessionManager = secureSessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
