package com.example.carenest.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.requireData
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
        val tomorrowTasks: List<DashboardTask>,
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
    private var hasInitializedProfileSelection = false

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
                _dashboardState.value = DashboardState.Error(
                    error.message ?: "Không thể tải dữ liệu trang chủ."
                )
                return@launch
            }

            if (families.isEmpty()) {
                val myProfileResult = familyRepository.getMyHealthProfile()
                val personalProfileId = myProfileResult.getOrNull()?.id
                
                memberProfileMap = emptyMap()
                _selectedMemberId.value = null
                
                if (personalProfileId != null) {
                    _currentProfileId.value = personalProfileId
                    secureSessionManager.saveActiveProfileId(personalProfileId)
                    
                    val dashboardResult = runCatching {
                        dashboardApi.getDashboard(null, personalProfileId)
                            .requireData("Không thể tải dữ liệu trang chủ")
                    }
                    val backendData = dashboardResult.getOrNull() ?: DashboardResponse()
                    val tasks = backendData.todayTasks.map(::decorateDashboardTask)
                    val tomorrowTasks = backendData.tomorrowTasks.map(::decorateDashboardTask)
                    
                    val personalProfileName = myProfileResult.getOrNull()?.name ?: "Hồ sơ cá nhân"
                    val personalMember = Member(memberId = -1L, profileId = personalProfileId, name = personalProfileName, avatarUrl = myProfileResult.getOrNull()?.avatarUrl)
                    val mergedDashboard = backendData.copy(
                        members = listOf(personalMember)
                    )

                    _dashboardState.value = DashboardState.Success(
                        data = mergedDashboard,
                        tasks = tasks,
                        tomorrowTasks = tomorrowTasks,
                        unreadCount = mergedDashboard.unreadNotifications.toInt(),
                        aiSummaryText = buildDashboardHealthSummary(tasks),
                        warning = null
                    )
                } else {
                    _currentProfileId.value = null
                    secureSessionManager.saveActiveProfileId(null)
                    _dashboardState.value = DashboardState.Success(
                        data = DashboardResponse(),
                        tasks = emptyList(),
                        tomorrowTasks = emptyList(),
                        unreadCount = 0,
                        aiSummaryText = "Hãy cập nhật hồ sơ sức khỏe cá nhân để bắt đầu."
                    )
                }
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
                _dashboardState.value = DashboardState.Error(
                    error.message ?: "Không thể tải gia đình đang hoạt động."
                )
                return@launch
            }

            val mappedFamilies = families.map { summary ->
                Family(
                    id = summary.id.toString(),
                    name = summary.name,
                    role = dashboardRoleLabel(summary.myRole),
                    memberCount = summary.memberCount
                )
            }
            val mappedMembers = familyDetail.members.map { member ->
                Member(
                    memberId = member.userId ?: -1L,
                    profileId = member.profileId,
                    name = member.fullName,
                    avatarUrl = member.avatarUrl
                )
            }
            memberProfileMap = familyDetail.members.associate { member ->
                (member.userId?.toString() ?: "") to member.profileId
            }

            val ownProfileId = resolveOwnProfileId(familyDetail)
            ownProfileId?.let { secureSessionManager.saveProfileIdSync(it) }

            val activeProfileId = if (!hasInitializedProfileSelection) {
                hasInitializedProfileSelection = true
                val resolved = resolveActiveProfileId(familyDetail, ownProfileId)
                secureSessionManager.saveActiveProfileId(resolved)
                _currentProfileId.value = resolved
                _selectedMemberId.value = memberProfileMap.entries
                    .firstOrNull { it.value == resolved }
                    ?.key
                resolved
            } else {
                _currentProfileId.value
            }

            val dashboardResult = runCatching {
                dashboardApi.getDashboard(activeFamilyId, activeProfileId)
                    .requireData("Không thể tải dữ liệu trang chủ")
            }
            val backendData = dashboardResult.getOrNull()
            val dashboardWarning = dashboardResult.exceptionOrNull()?.message

            val mergedDashboard = (backendData ?: DashboardResponse()).copy(
                families = mappedFamilies,
                members = mappedMembers
            )

            val tasks = mergedDashboard.todayTasks.map(::decorateDashboardTask)
            val tomorrowTasks = mergedDashboard.tomorrowTasks.map(::decorateDashboardTask)

            _dashboardState.value = DashboardState.Success(
                data = mergedDashboard,
                tasks = tasks,
                tomorrowTasks = tomorrowTasks,
                unreadCount = mergedDashboard.unreadNotifications.toInt(),
                aiSummaryText = buildDashboardHealthSummary(tasks),
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
            hasInitializedProfileSelection = false
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
            else -> null
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
