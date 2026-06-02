package com.example.carenest.feature.main.presentation

import android.widget.Toast
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
import com.example.carenest.feature.medical.presentation.MedicineScreen
import com.example.carenest.feature.medical.presentation.MedicineViewModel
import com.example.carenest.feature.profile.presentation.ProfileScreen
import com.example.carenest.feature.profile.presentation.ProfileViewModel
import com.example.carenest.feature.profile.presentation.ProfileViewModelFactory

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
    onNavigateToAdminVerification: () -> Unit = {},
    onNavigateToPolicy: () -> Unit = {},
    onNavigateToMedicalRecord: (Long) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { CareNestIcon(name = "home", contentDescription = "Trang chủ") },
                    label = { NavLabel("Trang chủ") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { CareNestIcon(name = "group", contentDescription = "Gia đình") },
                    label = { NavLabel("Gia đình") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { CareNestIcon(name = "medication", contentDescription = "Thuốc") },
                    label = { NavLabel("Thuốc") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { CareNestIcon(name = "chat-processing", contentDescription = "Tin nhắn") },
                    label = { NavLabel("Tin nhắn") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { CareNestIcon(name = "globe", contentDescription = "Cộng đồng") },
                    label = { NavLabel("Cộng đồng") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
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
                0 -> HomeDashboardScreen(
                    viewModel = dashboardViewModel,
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

                1 -> FamilyFlowScreen(
                    viewModel = dashboardViewModel,
                    modifier = Modifier.fillMaxSize(),
                )

                2 -> MedicineScreen(
                    viewModel = medicineViewModel,
                    onAddMedicineClick = onNavigateToAddMedicine,
                    onScheduleClick = onNavigateToMedicineSchedule,
                    onAddScheduleClick = onNavigateToAddMedicineSchedule,
                    onOcrClick = onNavigateToOcrScanner,
                )

                3 -> ChatHubScreen(
                    dashboardViewModel = dashboardViewModel,
                    aiChatViewModel = aiChatViewModel,
                    onNavigateToChatRoom = { _, _ ->
                        Toast.makeText(
                            context,
                            "Tính năng trò chuyện nhóm gia đình đang được ổn định lại.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                4 -> CommunityScreen(
                    canCreateArticle = canAccessDoctorUi,
                    onOpenGroup = { onItemClick(ChatRoom(it.id, it.name)) }
                )

                5 -> ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = onLogout,
                    onNavigateToMedicalRecord = {
                        val profileId = currentProfileId ?: application.secureSessionManager.getProfileId() ?: 0L
                        onNavigateToMedicalRecord(profileId)
                    },
                    onNavigateToDoctorVerification = onNavigateToDoctorVerification,
                    onNavigateToAdminVerification = onNavigateToAdminVerification,
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
