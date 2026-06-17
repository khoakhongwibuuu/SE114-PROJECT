package com.example.carenest.feature.main.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.components.CareNestIcon
import com.example.carenest.core.presentation.navigation.ChatRoom
import com.example.carenest.core.presentation.navigation.GroupPostDetail
import com.example.carenest.core.presentation.navigation.isValidHealthProfileId
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.PrimaryFixed
import com.example.carenest.core.presentation.theme.SurfaceLowest
import com.example.carenest.feature.auth.presentation.AuthViewModel
import com.example.carenest.feature.community.presentation.CommunityScreen
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.family.presentation.FamilyFlowScreen
import com.example.carenest.feature.medical.presentation.MedicineViewModel
import com.example.carenest.feature.profile.presentation.ProfileScreen
import com.example.carenest.feature.profile.presentation.ProfileViewModel
import com.example.carenest.feature.profile.presentation.ProfileViewModelFactory

private const val TAB_HOME = 0
private const val TAB_FAMILY = 1
private const val TAB_COMMUNITY = 2
private const val TAB_CHAT = 3
private const val TAB_PROFILE = 4

enum class MainTabTarget {
    HOME,
    FAMILY,
    COMMUNITY,
    CHAT,
    PROFILE,
}

@Composable
fun MainScreen(
    onItemClick: (Any) -> Unit,
    onNavigateToAddMedicine: () -> Unit = {},
    onNavigateToMedicineSchedule: () -> Unit = {},
    onNavigateToAddMedicineSchedule: () -> Unit = {},
    onNavigateToOcrScanner: () -> Unit = {},
    onNavigateToAppointments: (Long) -> Unit = {},
    onNavigateToVaccinations: (Long) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDoctorVerification: () -> Unit = {},
    onNavigateToDoctorWorkspace: () -> Unit = {},
    onNavigateToPatientBookingCenter: () -> Unit = {},
    onNavigateToConsultationRoom: (Long) -> Unit = {},
    onNavigateToPolicy: () -> Unit = {},
    onNavigateToMedicalRecord: (Long) -> Unit = {},
    onNavigateToFamilyChat: (Long, String, Int) -> Unit = { _, _, _ -> },
    onNavigateToDoctorProfile: (Long) -> Unit = {},
    tabTarget: MainTabTarget? = null,
    onTabTargetHandled: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_HOME) }
    var homeRefreshTrigger by rememberSaveable { mutableIntStateOf(0) }
    var familyRefreshTrigger by rememberSaveable { mutableIntStateOf(0) }
    var communityRefreshTrigger by rememberSaveable { mutableIntStateOf(0) }
    var profileRefreshTrigger by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            application.authApi,
            application.secureSessionManager,
            application.familyRepository
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentProfileId by dashboardViewModel.currentProfileId.collectAsState()
    val currentUser by dashboardViewModel.currentUser.collectAsState()
    val authCurrentUser by authViewModel.currentUser.collectAsState()
    val currentRole by application.secureSessionManager.userRoleFlow.collectAsState()
    val normalizedRole = (authCurrentUser?.role ?: currentUser?.role ?: currentRole)?.normalizedRole()
    val canAccessDoctorUi = normalizedRole == "DOCTOR" || normalizedRole == "ADMIN"
    val canCreateGroupRequest = normalizedRole == "DOCTOR"

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
    }

    LaunchedEffect(tabTarget) {
        val target = tabTarget ?: return@LaunchedEffect
        selectedTab = when (target) {
            MainTabTarget.HOME -> TAB_HOME
            MainTabTarget.FAMILY -> TAB_FAMILY
            MainTabTarget.COMMUNITY -> TAB_COMMUNITY
            MainTabTarget.CHAT -> TAB_CHAT
            MainTabTarget.PROFILE -> TAB_PROFILE
        }
        when (target) {
            MainTabTarget.HOME -> homeRefreshTrigger++
            MainTabTarget.FAMILY -> familyRefreshTrigger++
            MainTabTarget.COMMUNITY -> communityRefreshTrigger++
            MainTabTarget.PROFILE -> profileRefreshTrigger++
            MainTabTarget.CHAT -> Unit
        }
        onTabTargetHandled()
    }

    DisposableEffect(lifecycleOwner, authViewModel, dashboardViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                authViewModel.refreshCurrentUser()
                dashboardViewModel.fetchCurrentUser()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun handleTabSelection(tabIndex: Int) {
        if (selectedTab == tabIndex) {
            when (tabIndex) {
                TAB_HOME -> homeRefreshTrigger++
                TAB_FAMILY -> familyRefreshTrigger++
                TAB_COMMUNITY -> communityRefreshTrigger++
                TAB_PROFILE -> profileRefreshTrigger++
            }
        } else {
            selectedTab = tabIndex
        }
    }

    fun resolveActiveProfileIdOrNotify(): Long? {
        val profileId = currentProfileId ?: application.secureSessionManager.getActiveProfileId()
        if (!profileId.isValidHealthProfileId()) {
            Toast.makeText(context, "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước", Toast.LENGTH_SHORT).show()
            selectedTab = TAB_FAMILY
            return null
        }
        return profileId
    }

    val hasActiveHealthProfile = (currentProfileId ?: application.secureSessionManager.getActiveProfileId())
        .isValidHealthProfileId()

    // When user is on a non-home tab and presses Android system back, return to Home tab
    // instead of propagating to NavDisplay (which would pop MainDashboard off the stack).
    BackHandler(enabled = selectedTab != TAB_HOME) {
        selectedTab = TAB_HOME
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground),
        containerColor = PageBackground,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceLowest,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(topStart = AppRadius.x2, topEnd = AppRadius.x2)),
            ) {
                NavigationBarItem(
                    selected = selectedTab == TAB_HOME,
                    onClick = { handleTabSelection(TAB_HOME) },
                    icon = { CareNestIcon(name = "home", contentDescription = "Trang chủ") },
                    label = { NavLabel("Trang chủ") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_FAMILY,
                    onClick = { handleTabSelection(TAB_FAMILY) },
                    icon = { CareNestIcon(name = "group", contentDescription = "Gia đình") },
                    label = { NavLabel("Gia đình") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_COMMUNITY,
                    onClick = { handleTabSelection(TAB_COMMUNITY) },
                    icon = { CareNestIcon(name = "globe", contentDescription = "Cộng đồng") },
                    label = { NavLabel("Cộng đồng") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_CHAT,
                    onClick = { handleTabSelection(TAB_CHAT) },
                    icon = { CareNestIcon(name = "chat-processing", contentDescription = "Tin nhắn") },
                    label = { NavLabel("Tin nhắn") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_PROFILE,
                    onClick = { handleTabSelection(TAB_PROFILE) },
                    icon = { CareNestIcon(name = "person", contentDescription = "Tôi") },
                    label = { NavLabel("Tôi") },
                    colors = navColors(),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(paddingValues),
        ) {
            when (selectedTab) {
                TAB_HOME -> HomeDashboardScreen(
                    viewModel = dashboardViewModel,
                    refreshTrigger = homeRefreshTrigger,
                    onNavigateToMedicine = onNavigateToMedicineSchedule,
                    onNavigateToAppointment = {
                        resolveActiveProfileIdOrNotify()?.let(onNavigateToAppointments)
                    },
                    onNavigateToVaccine = {
                        resolveActiveProfileIdOrNotify()?.let(onNavigateToVaccinations)
                    },
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToTask = { task ->
                        when (task.type?.uppercase()) {
                            "MEDICATION" -> onNavigateToMedicineSchedule()
                            "VACCINATION" -> {
                                val profileId = task.profileId?.takeIf { it > 0L } ?: resolveActiveProfileIdOrNotify()
                                profileId?.let(onNavigateToVaccinations)
                            }
                            "APPOINTMENT" -> {
                                val profileId = task.profileId?.takeIf { it > 0L } ?: resolveActiveProfileIdOrNotify()
                                profileId?.let(onNavigateToAppointments)
                            }
                        }
                    }
                )

                TAB_FAMILY -> FamilyFlowScreen(
                    dashboardViewModel = dashboardViewModel,
                    medicineViewModel = medicineViewModel,
                    refreshTrigger = familyRefreshTrigger,
                    onNavigateToAddMedicine = onNavigateToAddMedicine,
                    onNavigateToMedicineSchedule = onNavigateToMedicineSchedule,
                    onNavigateToAddSchedule = onNavigateToAddMedicineSchedule,
                    onNavigateToOcrScanner = onNavigateToOcrScanner,
                    onOpenFamilyChat = { family ->
                        onNavigateToFamilyChat(family.id, family.name, family.memberCount)
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                TAB_COMMUNITY -> CommunityScreen(
                    canCreateArticle = canAccessDoctorUi,
                    canCreateGroupRequest = canCreateGroupRequest,
                    refreshTrigger = communityRefreshTrigger,
                    onOpenGroup = { onItemClick(ChatRoom(it.id, it.name)) },
                    onOpenGroupPosts = { group -> onItemClick(GroupPostDetail(group.id, group.name)) },
                    onNavigateToCreateGroupRequest = { onItemClick(com.example.carenest.core.presentation.navigation.CreateGroupRequest) },
                    onNavigateToDoctorProfile = onNavigateToDoctorProfile
                )

                TAB_CHAT -> ChatHubScreen(
                    onNavigateToConsultationRoom = onNavigateToConsultationRoom
                )

                TAB_PROFILE -> ProfileScreen(
                    viewModel = profileViewModel,
                    refreshTrigger = profileRefreshTrigger,
                    hasActiveHealthProfile = hasActiveHealthProfile,
                    onLogout = onLogout,
                    onNavigateToMedicalRecord = {
                        resolveActiveProfileIdOrNotify()?.let(onNavigateToMedicalRecord)
                    },
                    onNavigateToFamilySetup = {
                        selectedTab = TAB_FAMILY
                        familyRefreshTrigger++
                    },
                    onNavigateToDoctorVerification = onNavigateToDoctorVerification,
                    onNavigateToDoctorWorkspace = onNavigateToDoctorWorkspace,
                    onNavigateToPatientBookingCenter = onNavigateToPatientBookingCenter,
                    onNavigateToPolicy = onNavigateToPolicy
                )
            }
        }
    }
}

private fun String.normalizedRole(): String = removePrefix("ROLE_").uppercase()

@Composable
private fun NavLabel(text: String) {
    Text(
        text = text,
        style = CareNestTextStyles.navLabel,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryBlue,
    selectedTextColor = PrimaryBlue,
    unselectedIconColor = Outline,
    unselectedTextColor = Outline,
    indicatorColor = PrimaryFixed,
)
