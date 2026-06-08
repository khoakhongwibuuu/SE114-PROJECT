package com.example.carenest.feature.main.presentation

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.components.CareNestIcon
import com.example.carenest.core.presentation.navigation.ChatRoom
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.PrimaryFixed
import com.example.carenest.core.presentation.theme.SurfaceLowest
import com.example.carenest.feature.auth.presentation.AuthViewModel
import com.example.carenest.feature.chat.presentation.AiChatViewModel
import com.example.carenest.feature.chat.presentation.AiChatViewModelFactory
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

@Composable
fun MainScreen(
    onItemClick: (Any) -> Unit,
    onNavigateToAddMedicine: () -> Unit = {},
    onNavigateToMedicineSchedule: () -> Unit = {},
    onNavigateToAddMedicineSchedule: () -> Unit = {},
    onNavigateToAppointment: () -> Unit = {},
    onNavigateToVaccine: () -> Unit = {},
    onNavigateToOcrScanner: () -> Unit = {},
    onNavigateToAppointments: (Long) -> Unit = {},
    onNavigateToVaccinations: (Long) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDoctorVerification: () -> Unit = {},
    onNavigateToPolicy: () -> Unit = {},
    onNavigateToMedicalRecord: (Long) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
) {
    var selectedTab by remember { mutableIntStateOf(TAB_HOME) }
    var homeRefreshTrigger by remember { mutableIntStateOf(0) }
    var familyRefreshTrigger by remember { mutableIntStateOf(0) }
    var communityRefreshTrigger by remember { mutableIntStateOf(0) }
    var profileRefreshTrigger by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val aiChatViewModel: AiChatViewModel = viewModel(
        factory = AiChatViewModelFactory(application.aiChatApi)
    )
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
    val canAccessDoctorUi = (authCurrentUser?.role ?: currentUser?.role ?: currentRole)
        ?.let { it == "DOCTOR" || it == "ADMIN" }
        ?: false

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
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
                        val profileId = currentProfileId ?: application.secureSessionManager.getProfileId() ?: 0L
                        onNavigateToAppointments(profileId)
                    },
                    onNavigateToVaccine = {
                        val profileId = currentProfileId ?: application.secureSessionManager.getProfileId() ?: 0L
                        onNavigateToVaccinations(profileId)
                    },
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToTask = {}
                )

                TAB_FAMILY -> FamilyFlowScreen(
                    dashboardViewModel = dashboardViewModel,
                    medicineViewModel = medicineViewModel,
                    refreshTrigger = familyRefreshTrigger,
                    onNavigateToAddMedicine = onNavigateToAddMedicine,
                    onNavigateToMedicineSchedule = onNavigateToMedicineSchedule,
                    onNavigateToAddSchedule = onNavigateToAddMedicineSchedule,
                    onNavigateToOcrScanner = onNavigateToOcrScanner,
                    modifier = Modifier.fillMaxSize(),
                )

                TAB_COMMUNITY -> CommunityScreen(
                    canCreateArticle = canAccessDoctorUi,
                    refreshTrigger = communityRefreshTrigger,
                    onOpenGroup = { onItemClick(ChatRoom(it.id, it.name)) }
                )

                TAB_CHAT -> ChatHubScreen(
                    aiChatViewModel = aiChatViewModel,
                    onNavigateToAppointments = {
                        val profileId = currentProfileId ?: application.secureSessionManager.getProfileId() ?: 0L
                        onNavigateToAppointments(profileId)
                    }
                )

                TAB_PROFILE -> ProfileScreen(
                    viewModel = profileViewModel,
                    refreshTrigger = profileRefreshTrigger,
                    onLogout = onLogout,
                    onNavigateToMedicalRecord = {
                        val profileId = currentProfileId ?: application.secureSessionManager.getProfileId() ?: 0L
                        onNavigateToMedicalRecord(profileId)
                    },
                    onNavigateToDoctorVerification = onNavigateToDoctorVerification,
                    onNavigateToPolicy = onNavigateToPolicy
                )
            }
        }
    }
}

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
