package com.example.carenest.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
        val aiSummaryText: String
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
            runCatching { authApi.getMe() }
                .getOrNull()
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.data
                ?.let { user ->
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
                _dashboardState.value = DashboardState.Error(error.message ?: "Không thể tải dữ liệu trang chủ.")
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
                _dashboardState.value = DashboardState.Error(error.message ?: "Không thể tải gia đình đang hoạt động.")
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
                (member.profileId ?: member.id).toString() to member.profileId?.toLong()
            }

            val ownProfileId = resolveOwnProfileId(familyDetail)
            ownProfileId?.let { secureSessionManager.saveProfileIdSync(it) }

            val activeProfileId = resolveActiveProfileId(familyDetail, ownProfileId)
            secureSessionManager.saveActiveProfileId(activeProfileId)
            _currentProfileId.value = activeProfileId
            _selectedMemberId.value = memberProfileMap.entries
                .firstOrNull { it.value == activeProfileId }
                ?.key

            val dashboardResponse = runCatching {
                dashboardApi.getDashboard(activeFamilyId, activeProfileId)
            }.getOrNull()

            val backendData = dashboardResponse
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.data

            val mergedDashboard = (backendData ?: DashboardResponse()).copy(
                families = mappedFamilies,
                members = mappedMembers
            )

            val tasks = mergedDashboard.todayTasks.ifEmpty {
                buildFallbackTasks(familyDetail, activeProfileId)
            }

            _dashboardState.value = DashboardState.Success(
                data = mergedDashboard,
                tasks = tasks,
                unreadCount = mergedDashboard.unreadNotifications.toInt(),
                aiSummaryText = buildAiSummary(tasks)
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
        val currentId = _currentFamilyId.value?.toIntOrNull()
        return families.firstOrNull { it.id == currentId } ?: families.first()
    }

    private fun resolveOwnProfileId(familyDetail: FamilyDetailResponse): Long? {
        val currentUserId = _currentUser.value?.id ?: return null
        return familyDetail.members.firstOrNull { it.userId?.toLong() == currentUserId }?.profileId?.toLong()
    }

    private fun resolveActiveProfileId(
        familyDetail: FamilyDetailResponse,
        ownProfileId: Long?
    ): Long? {
        val savedProfileId = secureSessionManager.getActiveProfileId()
        val validProfileIds = familyDetail.members.mapNotNull { it.profileId?.toLong() }
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
            it.profileId?.toLong() == activeProfileId
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

    private fun buildAiSummary(tasks: List<DashboardTask>): String {
        return if (tasks.isEmpty()) {
            "Hôm nay chưa có cảnh báo lớn. Bạn có thể kiểm tra lịch thuốc, lịch khám và hỏi CareNest AI nếu cần tra cứu nhanh."
        } else {
            "Hôm nay có ${tasks.size} việc cần chú ý thực hiện. Hãy chuẩn bị đầy đủ để không bỏ sót."
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
